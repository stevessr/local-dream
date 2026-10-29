<div align="center">

# Local Dream <img src="./assets/icon.png" width="32" alt="Local Dream">

**Android Stable Diffusion with Snapdragon NPU acceleration**  
_Also supports CPU/GPU inference_

<img src="./assets/demo1.jpg" alt="App Demo" width="800">

</div>

## About this Repo

This project is **now open sourced and completely free**. Hope you enjoy it!

If you like it, please consider [sponsor](#-support-this-project) this project.

> [!NOTE]
> Currently focused on SD1.5 and SDXL models. SD2.1 is no longer maintained due to poor quality and limited popularity.
>
> **NPU support:**
>
> - SD1.5 models are supported on Snapdragon NPUs with Hexagon V68 architecture or newer.
> - SDXL models are supported on Snapdragon 8 Gen 3 and newer devices.
>
> CPU/GPU inference supports SD1.5 and imported SDXL MNN packages. Large SDXL
> models require substantially more memory than SD1.5.
>
> You can join our [telegram group](https://t.me/local_dream) for discussion or help with testing.

## PICO 4 Ultra (Snapdragon XR2 Gen 2)

The regular `arm64-v8a` Android APK can be sideloaded on **PICO 4 Ultra** and
**PICO 4 Ultra Enterprise** as a **2D Android app** (not a native OpenXR app).

- Qualcomm lists **SXR2230P as SoC ID 53 / HTP V69**, not V73.
  A PICO 4 Ultra (`A9210` / `A92Y0` / product name) whose Android SoC
  identifier is generic falls back to SXR2230P. A different explicit XR
  part number (including SXR2250P) is preserved but not guessed to be V69.
- SD1.5 NPU downloads use the existing `8gen1` **V69 candidate** archive
  rather than the incompatible `8gen2` V73 archive. QNN already packages
  `libQnnHtpV69Stub.so` / `libQnnHtpV69Skel.so`, selected by its backend.
  **Matching HTP architecture does not guarantee a precompiled context works
  on a different SoC.** The published archives target phone chips, not
  SXR2230P (SoC 53). If rejected by firmware, an SXR2230P-specific
  `soc_id=53, dsp_arch=v69` QNN export is required.
- Backend start runs a best-effort `--probe_htp` check using
  `QnnDevice_getPlatformInfo` to report the real NPU SoC/HTP architecture.
  If the headset reports an unexpected architecture, avoid forcing a
  mismatched model or DSP stub; capture the probe output before troubleshooting.
- SD 1.5 CPU/MNN models remain available if QNN model loading is rejected.
  Do **not** use SDXL NPU models compiled specifically for Snapdragon 8 Gen 3,
  or the SM8750+ DiT engine, as XR2 Gen 2 compatibility is not established.
- VR thermal/power limits and the headset's graphics workload may reduce
  inference speed. Start with 512×512 SD 1.5 and only scale up after testing.

To diagnose the SoC identity and QNN initialization on the headset:

```sh
adb shell getprop ro.soc.model
adb shell getprop ro.product.manufacturer
adb shell getprop ro.product.model
adb shell getprop ro.product.name
adb logcat -s BackendService:I
# The service prints HTP_PROBE={...} from QNN when available
```

Successful APK installation and catalog visibility are **not** proof of HTP
inference: validate at least one full generation and inspect backend logs.
See [PICO's official device specifications](https://developer.picoxr.com/zh/document/discover/xr-devices/)
and [Qualcomm's SoC/HTP architecture matrix](https://docs.qualcomm.com/nav/home/QNN_general_overview.html?product=924033590759186372).

## GitHub Actions: build Android APKs

The [Android APK / QNN workflow](.github/workflows/android-apk.yml)
runs on pull requests to `master`, pushes to `master` and feature branches,
or manually after this workflow has been merged into the default branch.

The workflow checks out native submodules, runs Kotlin unit tests, downloads
the public **QAIRT 2.51.0.260929** SDK directly from Qualcomm, extracts only
the files used for compilation, cross-compiles the `arm64-v8a` C++ QNN/MNN
core, and builds both **basicDebug** and **filterDebug** APKs. It checks
that the APK includes the native core and **V69** HTP stub/skeleton.

The finished APKs are attached to the successful Actions run as a
`LocalDream-PICO4Ultra-arm64-debug-*` artifact. They use the standard
Android **debug signing key**, not a production release key.

**Build scope:** These PICO/SD1.5 artifacts do **not** build or bundle the
SM8750+ DiT engine (`libdit_engine.so`). That optional native module
requires the separate Hexagon SDK and targets newer SoCs. QNN SDK files
must be used under Qualcomm's terms and are not committed to this repository.

## User Guide

For certain reasons, all guides and documentation have been moved to [Guide Site](https://ld-guide.chino.icu).

## Credits & Acknowledgments

Local Dream is built on top of many excellent open-source projects. Sincere thanks to all the authors and contributors whose work made this project possible.

### C++ Libraries

- **[Qualcomm QNN SDK](https://www.qualcomm.com/developer/software/qualcomm-ai-engine-direct-sdk)** - NPU model execution
- **[alibaba/MNN](https://github.com/alibaba/MNN/)** - CPU model execution
- **[happyyzy/stable-diffusion.cpp](https://github.com/happyyzy/stable-diffusion.cpp)** - Deep performance optimizations for on-device DiT inference
- **[xtensor-stack](https://github.com/xtensor-stack)** - Tensor operations & scheduling
- **[mlc-ai/tokenizers-cpp](https://github.com/mlc-ai/tokenizers-cpp)** - Text tokenization
- **[yhirose/cpp-httplib](https://github.com/yhirose/cpp-httplib)** - HTTP server
- **[nothings/stb](https://github.com/nothings/stb)** - Image processing
- **[facebook/zstd](https://github.com/facebook/zstd)** - Model compression
- **[nlohmann/json](https://github.com/nlohmann/json)** - JSON processing

### Android Libraries

- **[square/okhttp](https://github.com/square/okhttp)** - HTTP client
- **[coil-kt/coil](https://github.com/coil-kt/coil)** - Image loading & processing
- **[MoyuruAizawa/Cropify](https://github.com/MoyuruAizawa/Cropify)** - Image cropping
- **AOSP, Material Design, Jetpack Compose** - UI framework

### Models

- **[CompVis/stable-diffusion](https://github.com/CompVis/stable-diffusion)** and all other model creators
- **[xinntao/Real-ESRGAN](https://github.com/xinntao/Real-ESRGAN)** - Image upscaling
- **[Kim2091/UltraSharpV2](https://huggingface.co/Kim2091/UltraSharpV2)** - Image upscaling
- **[bhky/opennsfw2](https://github.com/bhky/opennsfw2)** - NSFW content filtering

---

## 💖 Support This Project

If you find Local Dream useful, please consider supporting its development:

### What Your Support Helps With:

- **Additional Models** - More AI model integrations
- **New Features** - Enhanced functionality and capabilities
- **Bug Fixes** - Continuous improvement and maintenance

<a href="https://ko-fi.com/xororz">
    <img height="36" style="border:0px;height:36px;" src="https://storage.ko-fi.com/cdn/kofi2.png?v=3" border="0" alt="Buy Me a Coffee at ko-fi.com" />
</a>
<a href="https://afdian.com/a/xororz">
    <img height="36" style="border-radius:12px;height:36px;" src="https://pic1.afdiancdn.com/static/img/welcome/button-sponsorme.jpg" alt="在爱发电支持我" />
</a>

Your sponsorship helps maintain and improve Local Dream for everyone!
