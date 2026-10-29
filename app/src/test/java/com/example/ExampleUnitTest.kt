package com.example

import com.example.data.PresetRepository
import com.example.model.*
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testFactoryPresetsIntegrity() {
        val presets = PresetRepository.factoryPresets
        assertTrue("Factory presets must not be empty", presets.isNotEmpty())

        val flat = presets.firstOrNull { it.id == "studio_ref_flat" }
        assertNotNull("Studio Reference Flat preset should exist", flat)
        assertEquals(10, flat!!.eq.bands.size)
    }

    @Test
    fun testEqBandsCountAndFrequencies() {
        val defaultEq = EqualizerConfig()
        assertEquals(10, defaultEq.bands.size)
        assertEquals(31, defaultEq.bands[0].centerFreqHz)
        assertEquals(1000, defaultEq.bands[5].centerFreqHz)
        assertEquals(16000, defaultEq.bands[9].centerFreqHz)
    }

    @Test
    fun testDynamicsDefaultParams() {
        val dyn = DynamicsConfig()
        assertTrue(dyn.compressorEnabled)
        assertTrue(dyn.limiterEnabled)
        assertTrue(dyn.thresholdDb < 0f)
        assertTrue(dyn.ratio >= 1.0f)
    }

    @Test
    fun testReverbConfigRange() {
        val rev = ReverbConfig()
        assertTrue(rev.roomSize in 0f..1f)
        assertTrue(rev.wetDryMix in 0f..1f)
        assertTrue(rev.decayTimeSec > 0f)
    }
}
