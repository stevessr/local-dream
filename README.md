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

- Device selection recognizes `SXR2230P` / `SXR2250P` as XR2 Gen 2
  parts. If Android reports a generic SoC string, a positively identified
  PICO 4 Ultra (`A9210` / `A92Y0` / product name) uses the XR2 Gen 2
  profile. Other PICO models are not overridden.
- SD 1.5 NPU model downloads select the existing `8gen2` package family.
  The QNN APK build already bundles the Hexagon V73 stub/skeleton runtime,
  but compatibility of a **precompiled QNN model binary** with PICO firmware
  must still be validated on an actual headset.
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
```

Successful APK installation and catalog visibility are **not** proof of HTP
inference: validate at least one full generation and inspect backend logs.
See [PICO's official device specifications](https://developer.picoxr.com/zh/document/discover/xr-devices/)
and [Qualcomm's XR2 Gen 2 platform information](https://www.qualcomm.com/xr-vr-ar/device-finder/pico-4-ultra).

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
