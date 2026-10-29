#ifndef LOCALDREAM_HTP_PROBE_HPP
#define LOCALDREAM_HTP_PROBE_HPP

#include <HTP/QnnHtpDevice.h>
#include <QnnDevice.h>
#include <QnnInterface.h>
#include <dlfcn.h>
#include <cstdint>
#include <iostream>
#include <string>
#include "json.hpp"

// Query the backend instead of guessing an NPU generation from the Android
// marketing name. This is diagnostic only: it does not create a QNN model
// context or force a particular HTP arch on the driver.
inline int probeHtpPlatform(const std::string& libDir) {
  auto fail = [](const std::string& why) {
    std::cout << "HTP_PROBE=" << nlohmann::json{
      {"ok", false}, {"error", why}
    }.dump() << std::endl;
    return EXIT_FAILURE;
  };
  if (libDir.empty()) return fail("missing --lib_dir");

  const std::string backendPath = libDir + "/libQnnHtp.so";
  void* handle = dlopen(backendPath.c_str(), RTLD_NOW | RTLD_LOCAL);
  if (!handle) return fail(std::string("dlopen HTP failed: ") + dlerror());

  using GetProvidersFn = Qnn_ErrorHandle_t (*)(
      const QnnInterface_t***, uint32_t*);
  auto getProviders = reinterpret_cast<GetProvidersFn>(
      dlsym(handle, "QnnInterface_getProviders"));
  if (!getProviders) {
    dlclose(handle);
    return fail("QnnInterface_getProviders unavailable");
  }
  const QnnInterface_t** providers = nullptr;
  uint32_t count = 0;
  if (getProviders(&providers, &count) != QNN_SUCCESS || !providers || count == 0) {
    dlclose(handle);
    return fail("QNN HTP returned no providers");
  }

  const QnnInterface_t* selected = nullptr;
  for (uint32_t i = 0; i < count; ++i) {
    if (providers[i] && providers[i]->apiVersion.coreApiVersion.major ==
                            QNN_API_VERSION_MAJOR) {
      selected = providers[i];
      break;
    }
  }
  if (!selected) {
    dlclose(handle);
    return fail("QNN HTP ABI major version mismatch");
  }

  const auto& api = selected->QNN_INTERFACE_VER_NAME;
  if (!api.deviceGetPlatformInfo || !api.deviceFreePlatformInfo) {
    dlclose(handle);
    return fail("QNN platform information API unavailable");
  }

  const QnnDevice_PlatformInfo_t* info = nullptr;
  auto status = api.deviceGetPlatformInfo(nullptr, &info);
  if (status != QNN_SUCCESS || !info) {
    dlclose(handle);
    return fail("QNN HTP platform query failed: " +
                std::to_string(static_cast<int>(status)));
  }

  nlohmann::json devices = nlohmann::json::array();
  if (info->version == QNN_DEVICE_PLATFORM_INFO_VERSION_1) {
    for (uint32_t i = 0; i < info->v1.numHwDevices; ++i) {
      const auto& hw = info->v1.hwDevices[i];
      if (hw.version != QNN_DEVICE_HARDWARE_DEVICE_INFO_VERSION_1) continue;
      auto* ext = reinterpret_cast<const QnnHtpDevice_DeviceInfoExtension_t*>(
          hw.v1.deviceInfoExtension);
      if (!ext || ext->devType != QNN_HTP_DEVICE_TYPE_ON_CHIP) continue;
      devices.push_back({
          {"soc_id", ext->onChipDevice.socModel},
          {"htp_arch", static_cast<int>(ext->onChipDevice.arch)},
          {"vtcm_mb", ext->onChipDevice.vtcmSize}
      });
    }
  }

  // Free platform data while the HTP provider library is still loaded.
  api.deviceFreePlatformInfo(nullptr, info);
  dlclose(handle);
  if (devices.empty()) return fail("no on-chip HTP device reported");
  std::cout << "HTP_PROBE=" << nlohmann::json{
    {"ok", true}, {"devices", devices}
  }.dump() << std::endl;
  return EXIT_SUCCESS;
}

#endif  // LOCALDREAM_HTP_PROBE_HPP
