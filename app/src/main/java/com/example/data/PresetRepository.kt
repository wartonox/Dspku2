package com.example.data

import com.example.model.*

object PresetRepository {

    val factoryPresets: List<LabPreset> = listOf(
        LabPreset(
            id = "studio_ref_flat",
            name = "Studio Reference Flat",
            category = "Laboratory",
            description = "Neutral flat response for acoustic calibration, testing, and accurate monitoring.",
            eq = EqualizerConfig(
                enabled = true,
                preGainDb = 0f,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 0f),
                    EqBand(1, 62, "62 Hz", 0f),
                    EqBand(2, 125, "125 Hz", 0f),
                    EqBand(3, 250, "250 Hz", 0f),
                    EqBand(4, 500, "500 Hz", 0f),
                    EqBand(5, 1000, "1 kHz", 0f),
                    EqBand(6, 2000, "2 kHz", 0f),
                    EqBand(7, 4000, "4 kHz", 0f),
                    EqBand(8, 8000, "8 kHz", 0f),
                    EqBand(9, 16000, "16 kHz", 0f)
                ),
                hpfCutoffHz = 20f,
                lpfCutoffHz = 20000f
            ),
            reverb = ReverbConfig(enabled = false, wetDryMix = 0f),
            dynamics = DynamicsConfig(compressorEnabled = false, limiterEnabled = true, limiterCeilingDb = -0.2f),
            spatial = SpatialConfig(virtualizerEnabled = false, stereoWidthPercent = 100f),
            harmonic = HarmonicConfig(bassBoostEnabled = false, tubeWarmthDrive = 0f, tapeSaturation = 0f)
        ),

        LabPreset(
            id = "audiophile_mastering",
            name = "Audiophile Mastering",
            category = "Mastering",
            description = "Crisp silky highs, controlled punchy sub-bass, transparent dynamic compression, and open soundstage.",
            eq = EqualizerConfig(
                enabled = true,
                preGainDb = 0.5f,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 2.5f),
                    EqBand(1, 62, "62 Hz", 2.0f),
                    EqBand(2, 125, "125 Hz", 0.5f),
                    EqBand(3, 250, "250 Hz", -1.0f),
                    EqBand(4, 500, "500 Hz", -0.5f),
                    EqBand(5, 1000, "1 kHz", 0.5f),
                    EqBand(6, 2000, "2 kHz", 1.5f),
                    EqBand(7, 4000, "4 kHz", 2.0f),
                    EqBand(8, 8000, "8 kHz", 3.0f),
                    EqBand(9, 16000, "16 kHz", 4.0f)
                ),
                hpfCutoffHz = 25f,
                lpfCutoffHz = 20000f
            ),
            reverb = ReverbConfig(
                enabled = true,
                presetName = "Studio Room",
                roomSize = 0.35f,
                decayTimeSec = 1.2f,
                wetDryMix = 0.20f,
                earlyReflections = 0.6f
            ),
            dynamics = DynamicsConfig(
                compressorEnabled = true,
                thresholdDb = -16f,
                ratio = 2.5f,
                attackMs = 30f,
                releaseMs = 150f,
                makeupGainDb = 2.0f,
                limiterEnabled = true,
                limiterCeilingDb = -0.3f
            ),
            spatial = SpatialConfig(
                virtualizerEnabled = true,
                virtualizerStrength = 0.40f,
                stereoWidthPercent = 135f
            ),
            harmonic = HarmonicConfig(
                bassBoostEnabled = true,
                bassBoostStrength = 0.35f,
                bassCenterFreqHz = 80,
                tubeWarmthDrive = 0.20f,
                tapeSaturation = 0.15f,
                auralExciter = 0.40f,
                deEsserStrength = 0.25f,
                loudnessEnhancerGainDb = 2.5f
            )
        ),

        LabPreset(
            id = "club_sub_slam",
            name = "Club Sub-Bass Slam",
            category = "Bass Heavy",
            description = "Subwoofer optimization with deep harmonic resonance, hard punch, and wide club soundstage.",
            eq = EqualizerConfig(
                enabled = true,
                preGainDb = 1.0f,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 7.0f),
                    EqBand(1, 62, "62 Hz", 6.0f),
                    EqBand(2, 125, "125 Hz", 4.0f),
                    EqBand(3, 250, "250 Hz", 1.0f),
                    EqBand(4, 500, "500 Hz", -1.5f),
                    EqBand(5, 1000, "1 kHz", 0.0f),
                    EqBand(6, 2000, "2 kHz", 1.0f),
                    EqBand(7, 4000, "4 kHz", 2.5f),
                    EqBand(8, 8000, "8 kHz", 4.5f),
                    EqBand(9, 16000, "16 kHz", 5.5f)
                ),
                hpfCutoffHz = 20f,
                lpfCutoffHz = 19000f
            ),
            reverb = ReverbConfig(
                enabled = true,
                presetName = "Club Arena",
                roomSize = 0.65f,
                decayTimeSec = 2.2f,
                wetDryMix = 0.30f
            ),
            dynamics = DynamicsConfig(
                compressorEnabled = true,
                thresholdDb = -14f,
                ratio = 5.0f,
                attackMs = 10f,
                releaseMs = 90f,
                makeupGainDb = 3.5f,
                limiterEnabled = true,
                limiterCeilingDb = -0.1f
            ),
            spatial = SpatialConfig(
                virtualizerEnabled = true,
                virtualizerStrength = 0.60f,
                stereoWidthPercent = 140f
            ),
            harmonic = HarmonicConfig(
                bassBoostEnabled = true,
                bassBoostStrength = 0.85f,
                bassCenterFreqHz = 50,
                tubeWarmthDrive = 0.35f,
                tapeSaturation = 0.20f,
                auralExciter = 0.30f,
                loudnessEnhancerGainDb = 4.0f
            )
        ),

        LabPreset(
            id = "cathedral_reverb",
            name = "Cathedral Acoustics",
            category = "Reverb Lab",
            description = "Immense 4.5s decay time, lush high frequency diffusion, and expansive spatial envelopment.",
            eq = EqualizerConfig(
                enabled = true,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 1.0f),
                    EqBand(1, 62, "62 Hz", 2.0f),
                    EqBand(2, 125, "125 Hz", 1.5f),
                    EqBand(3, 250, "250 Hz", 0.0f),
                    EqBand(4, 500, "500 Hz", 0.5f),
                    EqBand(5, 1000, "1 kHz", 1.0f),
                    EqBand(6, 2000, "2 kHz", 2.0f),
                    EqBand(7, 4000, "4 kHz", 3.0f),
                    EqBand(8, 8000, "8 kHz", 2.0f),
                    EqBand(9, 16000, "16 kHz", 1.0f)
                ),
                hpfCutoffHz = 35f,
                lpfCutoffHz = 16000f
            ),
            reverb = ReverbConfig(
                enabled = true,
                presetName = "Cathedral Vault",
                roomSize = 0.95f,
                decayTimeSec = 4.5f,
                preDelayMs = 45f,
                hfDamping = 0.50f,
                diffusion = 0.90f,
                earlyReflections = 0.80f,
                wetDryMix = 0.55f,
                stereoSpread = 1.0f
            ),
            dynamics = DynamicsConfig(
                compressorEnabled = true,
                thresholdDb = -20f,
                ratio = 2.0f,
                attackMs = 40f,
                releaseMs = 300f
            ),
            spatial = SpatialConfig(
                virtualizerEnabled = true,
                virtualizerStrength = 0.80f,
                stereoWidthPercent = 160f
            ),
            harmonic = HarmonicConfig(
                bassBoostEnabled = false,
                tubeWarmthDrive = 0.15f,
                auralExciter = 0.20f
            )
        ),

        LabPreset(
            id = "vocal_studio_polish",
            name = "Vocal Studio Polish",
            category = "Studio Voice",
            description = "De-esser sibilance reduction, 3kHz vocal presence boost, and polished plate reverb reflection.",
            eq = EqualizerConfig(
                enabled = true,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", -4.0f),
                    EqBand(1, 62, "62 Hz", -2.0f),
                    EqBand(2, 125, "125 Hz", 0.0f),
                    EqBand(3, 250, "250 Hz", 1.0f),
                    EqBand(4, 500, "500 Hz", -1.0f),
                    EqBand(5, 1000, "1 kHz", 2.0f),
                    EqBand(6, 2000, "2 kHz", 3.5f),
                    EqBand(7, 4000, "4 kHz", 3.0f),
                    EqBand(8, 8000, "8 kHz", 2.0f),
                    EqBand(9, 16000, "16 kHz", 3.0f)
                ),
                hpfCutoffHz = 80f,
                lpfCutoffHz = 18000f
            ),
            reverb = ReverbConfig(
                enabled = true,
                presetName = "Vocal Plate",
                roomSize = 0.40f,
                decayTimeSec = 1.5f,
                preDelayMs = 30f,
                wetDryMix = 0.28f
            ),
            dynamics = DynamicsConfig(
                compressorEnabled = true,
                thresholdDb = -20f,
                ratio = 4.0f,
                attackMs = 8f,
                releaseMs = 80f,
                makeupGainDb = 3.0f,
                limiterEnabled = true
            ),
            spatial = SpatialConfig(virtualizerEnabled = false, stereoWidthPercent = 110f),
            harmonic = HarmonicConfig(
                tubeWarmthDrive = 0.30f,
                auralExciter = 0.50f,
                deEsserStrength = 0.70f,
                loudnessEnhancerGainDb = 2.0f
            )
        ),

        LabPreset(
            id = "vintage_tube_console",
            name = "Vintage Tube Console",
            category = "Analog Tone",
            description = "Even-order harmonic saturation, analog transformer warmth, and soft tape compression knee.",
            eq = EqualizerConfig(
                enabled = true,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 1.5f),
                    EqBand(1, 62, "62 Hz", 3.0f),
                    EqBand(2, 125, "125 Hz", 2.5f),
                    EqBand(3, 250, "250 Hz", 1.5f),
                    EqBand(4, 500, "500 Hz", 0.5f),
                    EqBand(5, 1000, "1 kHz", 0.0f),
                    EqBand(6, 2000, "2 kHz", 1.0f),
                    EqBand(7, 4000, "4 kHz", 2.0f),
                    EqBand(8, 8000, "8 kHz", 1.5f),
                    EqBand(9, 16000, "16 kHz", 0.5f)
                )
            ),
            reverb = ReverbConfig(
                enabled = true,
                presetName = "Warm Chamber",
                decayTimeSec = 1.1f,
                wetDryMix = 0.18f
            ),
            dynamics = DynamicsConfig(
                compressorEnabled = true,
                thresholdDb = -15f,
                ratio = 3.0f,
                kneeDb = 12f,
                makeupGainDb = 2.0f
            ),
            spatial = SpatialConfig(stereoWidthPercent = 105f),
            harmonic = HarmonicConfig(
                tubeWarmthDrive = 0.75f,
                tapeSaturation = 0.60f,
                bassCenterFreqHz = 120,
                bassBoostStrength = 0.40f
            )
        ),

        LabPreset(
            id = "subwoofer_calibration",
            name = "Subwoofer Calibration",
            category = "Laboratory",
            description = "Strict low-pass band for isolated subwoofer alignment, phase checking, and room mode testing.",
            eq = EqualizerConfig(
                enabled = true,
                bands = listOf(
                    EqBand(0, 31, "31 Hz", 6.0f),
                    EqBand(1, 62, "62 Hz", 5.0f),
                    EqBand(2, 125, "125 Hz", 2.0f),
                    EqBand(3, 250, "250 Hz", -15.0f),
                    EqBand(4, 500, "500 Hz", -15.0f),
                    EqBand(5, 1000, "1 kHz", -15.0f),
                    EqBand(6, 2000, "2 kHz", -15.0f),
                    EqBand(7, 4000, "4 kHz", -15.0f),
                    EqBand(8, 8000, "8 kHz", -15.0f),
                    EqBand(9, 16000, "16 kHz", -15.0f)
                ),
                lpfCutoffHz = 120f
            ),
            reverb = ReverbConfig(enabled = false, wetDryMix = 0f),
            dynamics = DynamicsConfig(limiterEnabled = true, limiterCeilingDb = -0.5f),
            spatial = SpatialConfig(stereoWidthPercent = 0f), // Mono sub
            harmonic = HarmonicConfig(bassBoostStrength = 0.60f, bassCenterFreqHz = 50)
        )
    )
}
