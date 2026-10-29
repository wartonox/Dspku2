package com.example.model

/**
 * Frequency band for 10-band Graphic Equalizer
 */
data class EqBand(
    val index: Int,
    val centerFreqHz: Int,
    val label: String,
    val gainDb: Float = 0f // -15f to +15f dB
)

/**
 * 10-band Graphic and Parametric Equalizer configuration
 */
data class EqualizerConfig(
    val enabled: Boolean = true,
    val preGainDb: Float = 0f, // -12f to +12f dB
    val bands: List<EqBand> = listOf(
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
    // Parametric filters
    val hpfCutoffHz: Float = 20f, // High-pass filter (20 - 400 Hz)
    val lpfCutoffHz: Float = 20000f, // Low-pass filter (4000 - 20000 Hz)
    val parametricMidFreqHz: Float = 1000f,
    val parametricMidGainDb: Float = 0f,
    val parametricMidQ: Float = 1.0f // 0.5 to 8.0
)

/**
 * Reverb & Room Acoustics parameters
 */
data class ReverbConfig(
    val enabled: Boolean = true,
    val presetName: String = "Studio Live",
    val roomSize: Float = 0.55f, // 0f to 1f
    val decayTimeSec: Float = 1.8f, // 0.2s to 8.0s
    val preDelayMs: Float = 20f, // 0ms to 120ms
    val hfDamping: Float = 0.40f, // 0f to 1f
    val diffusion: Float = 0.70f, // 0f to 1f
    val earlyReflections: Float = 0.65f, // 0f to 1f
    val wetDryMix: Float = 0.35f, // 0f (100% dry) to 1f (100% wet)
    val stereoSpread: Float = 0.85f // 0f to 1f
)

/**
 * Studio Dynamic Processing (Compressor, Limiter, Noise Gate)
 */
data class DynamicsConfig(
    val compressorEnabled: Boolean = true,
    val thresholdDb: Float = -18f, // -60 to 0 dB
    val ratio: Float = 3.5f, // 1:1 to 20:1
    val attackMs: Float = 15f, // 1 to 200 ms
    val releaseMs: Float = 120f, // 10 to 1000 ms
    val kneeDb: Float = 6f, // 0 to 20 dB
    val makeupGainDb: Float = 3f, // 0 to 24 dB
    val limiterEnabled: Boolean = true,
    val limiterCeilingDb: Float = -0.5f, // -12 to 0 dBFS
    val noiseGateEnabled: Boolean = false,
    val noiseGateThresholdDb: Float = -50f // -80 to -20 dB
)

/**
 * Spatial Audio & Stereo Image Control
 */
data class SpatialConfig(
    val virtualizerEnabled: Boolean = true,
    val virtualizerStrength: Float = 0.45f, // 0 to 1f
    val stereoWidthPercent: Float = 120f, // 0% (Mono) to 200% (Super-Wide)
    val balance: Float = 0f, // -1f (Left) to +1f (Right)
    val phaseInvertMode: PhaseMode = PhaseMode.NORMAL,
    val haasDelayMs: Float = 0f // 0 to 25 ms
)

enum class PhaseMode(val label: String) {
    NORMAL("Normal 0°"),
    LEFT_INVERT("L Invert 180°"),
    RIGHT_INVERT("R Invert 180°"),
    STEREO_OUT("Out of Phase")
}

/**
 * Harmonic Coloration, Bass Boost & Psychoacoustics
 */
data class HarmonicConfig(
    val bassBoostEnabled: Boolean = true,
    val bassBoostStrength: Float = 0.40f, // 0 to 1f
    val bassCenterFreqHz: Int = 80, // 50, 80, 120, 160 Hz
    val tubeWarmthDrive: Float = 0.25f, // 0 to 1f
    val tapeSaturation: Float = 0.15f, // 0 to 1f
    val auralExciter: Float = 0.30f, // 0 to 1f
    val deEsserStrength: Float = 0.20f, // 0 to 1f
    val loudnessEnhancerGainDb: Float = 2.0f // 0 to 10 dB
)

/**
 * Laboratory Signal Generator
 */
enum class LabSignalMode(val label: String) {
    DEMO_TRACK("Studio Demo Beat"),
    PURE_TONE("Precision Waveform"),
    NOISE("Noise Generator"),
    FREQUENCY_SWEEP("Log Sweep 20-20k"),
    CALIBRATION_1K("1kHz 0dB Reference"),
    BINAURAL_BEAT("Binaural Waves"),
    MIC_RTA("Acoustic Mic Input")
}

enum class WaveformType(val label: String) {
    SINE("Sine (Pure)"),
    SQUARE("Square (Odd Harmonics)"),
    TRIANGLE("Triangle (Soft)"),
    SAWTOOTH("Sawtooth (Full Spectrum)")
}

enum class NoiseType(val label: String) {
    PINK("Pink (1/f Lab Standard)"),
    WHITE("White (Flat Power)"),
    BROWN("Brown / Red (Sub Rumble)")
}

data class SignalGeneratorConfig(
    val mode: LabSignalMode = LabSignalMode.DEMO_TRACK,
    val isPlaying: Boolean = true,
    val volume: Float = 0.75f,
    val waveform: WaveformType = WaveformType.SINE,
    val frequencyHz: Float = 440f, // 20 - 20000 Hz
    val noiseType: NoiseType = NoiseType.PINK,
    val sweepDurationSec: Float = 3.0f,
    val binauralBaseHz: Float = 216f,
    val binauralBeatHz: Float = 10f // Alpha wave (10 Hz)
)

/**
 * Real-time Lab Telemetry
 */
data class TelemetryData(
    val sampleRateHz: Int = 48000,
    val bitDepth: Int = 16,
    val bufferSize: Int = 2048,
    val latencyMs: Float = 12.5f,
    val peakDbL: Float = -14f,
    val peakDbR: Float = -15f,
    val rmsDbL: Float = -22f,
    val rmsDbR: Float = -23f,
    val gainReductionDb: Float = 0f,
    val phaseCorrelation: Float = 0.92f, // -1.0 to +1.0
    val isClipping: Boolean = false,
    val estimatedThdPercent: Float = 0.04f,
    val waveformSamples: FloatArray = FloatArray(128) { 0f },
    val spectrumBands: FloatArray = FloatArray(64) { 0.05f }
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TelemetryData
        return isClipping == other.isClipping &&
                peakDbL == other.peakDbL &&
                peakDbR == other.peakDbR &&
                gainReductionDb == other.gainReductionDb
    }

    override fun hashCode(): Int {
        var result = peakDbL.hashCode()
        result = 31 * result + peakDbR.hashCode()
        result = 31 * result + isClipping.hashCode()
        return result
    }
}

/**
 * Complete Audio Lab Preset
 */
data class LabPreset(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val eq: EqualizerConfig,
    val reverb: ReverbConfig,
    val dynamics: DynamicsConfig,
    val spatial: SpatialConfig,
    val harmonic: HarmonicConfig
)
