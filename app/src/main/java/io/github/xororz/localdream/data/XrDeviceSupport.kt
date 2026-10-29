package io.github.xororz.localdream.data

import java.util.Locale

/**
 * Resolves Android's sometimes generic SOC_MODEL on PICO 4 Ultra headsets.
 *
 * A valid Qualcomm part number always wins. Only a positively identified
 * PICO 4 Ultra may fall back to the known XR2 Gen 2 family, which prevents
 * accidentally treating older PICO 4 (XR2 Gen 1) devices as V69 hardware.
 *
 * This is device/package selection, not a QNN HTP compatibility probe:
 * loading a precompiled QNN context still depends on the device firmware.
 */
internal object XrDeviceSupport {
    private const val XR2_GEN2_FALLBACK_SOC = "SXR2230P"
    private val qualcommPartNumber = Regex("^(SM|SXR|QCS|QCM)\\d")
    private val pico4UltraIdentifiers = setOf(
        "PICO 4 ULTRA",
        "PICO 4 ULTRA ENTERPRISE",
        "PICO4ULTRA",
        "A9210",
        "A92Y0",
    )

    fun resolveSocModel(
        rawSoc: String?,
        manufacturer: String?,
        product: String?,
        model: String?,
        device: String?,
    ): String {
        val soc = rawSoc.orEmpty().trim().uppercase(Locale.ROOT)
        if (qualcommPartNumber.containsMatchIn(soc)) return soc

        return if (isPico4Ultra(manufacturer, product, model, device)) {
            XR2_GEN2_FALLBACK_SOC
        } else {
            soc
        }
    }
    // The signed-off Qualcomm QNN target for XR2 Gen 2 (SXR2230P).
    // Check both fields: an HTP architecture match does not imply that a
    // context binary compiled for a different SoC can be loaded safely.
    fun isSxr2230pHtp(socId: Int, htpArch: Int): Boolean = socId == 53 && htpArch == 69

    fun isPico4Ultra(
        manufacturer: String?,
        product: String?,
        model: String?,
        device: String?,
    ): Boolean {
        if (manufacturer?.contains("pico", ignoreCase = true) != true) return false
        return sequenceOf(product, model, device)
            .filterNotNull()
            .map { it.trim().uppercase(Locale.ROOT).replace(Regex("\\s+"), " ") }
            .any { it in pico4UltraIdentifiers || it.removePrefix("PICO ") in pico4UltraIdentifiers }
    }
}

