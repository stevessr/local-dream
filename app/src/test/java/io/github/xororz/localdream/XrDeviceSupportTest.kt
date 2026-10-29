package io.github.xororz.localdream

import io.github.xororz.localdream.data.Model
import io.github.xororz.localdream.data.XrDeviceSupport
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class XrDeviceSupportTest {
    @Test
    fun xr2Gen2SelectsV69PackageCandidateNotV73() {
        assertEquals("8gen1", Model.getChipsetSuffix("SXR2230P"))
        assertEquals("8gen1", Model.getChipsetSuffix(" sxr2230p "))
        // Unknown XR part number: never assume it uses an SXR2230P/V69 binary.
        assertNull(Model.getChipsetSuffix("SXR2250P"))
    }

    @Test
    fun pico4UltraA9210FallsBackWhenSocModelIsGeneric() {
        assertEquals(
            "SXR2230P",
            XrDeviceSupport.resolveSocModel("unknown", "Pico", "A9210", "Pico A92Y0", "A9210"),
        )
        assertEquals(
            "SXR2230P",
            XrDeviceSupport.resolveSocModel("kalama", "PICO", "PICO 4 Ultra", "unknown", ""),
        )
    }

    @Test
    fun enterpriseHeadsetUsesTheSameXr2Gen2Family() {
        assertEquals(
            "SXR2230P",
            XrDeviceSupport.resolveSocModel("", "Pico", "PICO 4 Ultra Enterprise", "", ""),
        )
    }

    @Test
    fun reportedQualcommPartNumberIsNotOverridden() {
        assertEquals(
            "SXR2250P",
            XrDeviceSupport.resolveSocModel(" sxr2250p ", "Pico", "A9210", "A92Y0", "A9210"),
        )
        assertEquals(
            "SM8550",
            XrDeviceSupport.resolveSocModel("SM8550", "Pico", "A9210", "A92Y0", "A9210"),
        )
    }

    @Test
    fun olderPicoAndNonPicoHardwareAreNotMistakenForUltra() {
        assertEquals(
            "unknown",
            XrDeviceSupport.resolveSocModel("unknown", "Pico", "PICO 4", "PICO 4", "PICO 4"),
        )
        assertEquals(
            "unknown",
            XrDeviceSupport.resolveSocModel("unknown", "Other", "A9210", "A92Y0", "A9210"),
        )
        assertNull(Model.getChipsetSuffix("SXR2130"))
    }

    @Test
    fun unknownXrPartIsNeverReplacedByPicoIdentityFallback() {
        assertEquals(
            "SXR2250P",
            XrDeviceSupport.resolveSocModel("SXR2250P", "Pico", "A9210", "A92Y0", "A9210"),
        )
    }

    @Test
    fun qnnHardwareProbeMustMatchBothSocAndArchitecture() {
        org.junit.Assert.assertTrue(XrDeviceSupport.isSxr2230pHtp(53, 69))
        org.junit.Assert.assertFalse(XrDeviceSupport.isSxr2230pHtp(53, 73))
        org.junit.Assert.assertFalse(XrDeviceSupport.isSxr2230pHtp(36, 69))
        org.junit.Assert.assertFalse(XrDeviceSupport.isSxr2230pHtp(43, 73))
    }

    @Test
    fun picoIdentificationDoesNotMatchOlderPico4() {
        org.junit.Assert.assertTrue(
            XrDeviceSupport.isPico4Ultra("PICO", "A9210", "PICO 4 Ultra", "A9210"),
        )
        org.junit.Assert.assertFalse(
            XrDeviceSupport.isPico4Ultra("PICO", "PICO 4", "PICO 4", "PICO 4"),
        )
    }

    @Test
    fun existingPhoneChipsetMappingsRemainIntact() {
        assertEquals("8gen2", Model.getChipsetSuffix("SM8550"))
        assertEquals("8gen1", Model.getChipsetSuffix("SM8450"))
        assertEquals("min", Model.getChipsetSuffix("SM9999"))
    }
}
