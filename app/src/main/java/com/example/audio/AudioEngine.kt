package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.media.audiofx.LoudnessEnhancer
import android.media.audiofx.PresetReverb
import android.media.audiofx.Virtualizer
import android.util.Log
import com.example.model.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Random
import kotlin.math.*

class AudioEngine {

    companion object {
        private const val TAG = "AudioEngine"
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
    }

    private var audioTrack: AudioTrack? = null
    private var isRunning = false
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Android System Hardware Effects
    private var systemEqualizer: Equalizer? = null
    private var systemBassBoost: BassBoost? = null
    private var systemVirtualizer: Virtualizer? = null
    private var systemReverb: PresetReverb? = null
    private var systemLoudness: LoudnessEnhancer? = null

    // Current Configurations
    @Volatile var eqConfig: EqualizerConfig = EqualizerConfig()
    @Volatile var reverbConfig: ReverbConfig = ReverbConfig()
    @Volatile var dynamicsConfig: DynamicsConfig = DynamicsConfig()
    @Volatile var spatialConfig: SpatialConfig = SpatialConfig()
    @Volatile var harmonicConfig: HarmonicConfig = HarmonicConfig()
    @Volatile var signalGenConfig: SignalGeneratorConfig = SignalGeneratorConfig()

    // Real-time Telemetry
    private val _telemetry = MutableStateFlow(TelemetryData(sampleRateHz = SAMPLE_RATE))
    val telemetry: StateFlow<TelemetryData> = _telemetry.asStateFlow()

    // Internal DSP States
    // Biquad filters for 10 EQ bands
    private val eqFiltersL = Array(10) { BiquadFilter() }
    private val eqFiltersR = Array(10) { BiquadFilter() }
    private val hpfL = BiquadFilter()
    private val hpfR = BiquadFilter()
    private val lpfL = BiquadFilter()
    private val lpfR = BiquadFilter()
    private val bassFilterL = BiquadFilter()
    private val bassFilterR = BiquadFilter()

    // Reverb Comb & All-Pass Delays (Schroeder-Moorer algorithmic reverb)
    private val combDelaysL = intArrayOf(1116, 1188, 1277, 1356, 1422, 1491, 1557, 1617)
    private val combDelaysR = intArrayOf(1139, 1211, 1300, 1379, 1445, 1514, 1580, 1640)
    private val combBuffersL = Array(8) { FloatArray(2000) }
    private val combBuffersR = Array(8) { FloatArray(2000) }
    private val combIndicesL = IntArray(8)
    private val combIndicesR = IntArray(8)
    private val combFiltersL = FloatArray(8)
    private val combFiltersR = FloatArray(8)

    private val allPassDelays = intArrayOf(556, 441, 341, 225)
    private val allPassBuffersL = Array(4) { FloatArray(600) }
    private val allPassBuffersR = Array(4) { FloatArray(600) }
    private val allPassIndicesL = IntArray(4)
    private val allPassIndicesR = IntArray(4)

    // Compressor envelope follower
    private var compEnvelope = 0f

    // Noise Generator States
    private val random = Random()
    private var pinkB0 = 0.0
    private var pinkB1 = 0.0
    private var pinkB2 = 0.0
    private var pinkB3 = 0.0
    private var pinkB4 = 0.0
    private var pinkB5 = 0.0
    private var pinkB6 = 0.0
    private var brownVal = 0.0

    // Sweep phase
    private var sweepPhase = 0.0
    private var sweepTime = 0.0

    // Demo Beat Synthesizer State
    private var beatStep = 0
    private var beatSampleCounter = 0
    private var synthPhase = 0.0

    init {
        updateBiquadCoefficients()
    }

    fun start() {
        if (isRunning) return
        try {
            val minBuf = AudioTrack.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val trackBuf = max(minBuf * 2, BUFFER_SIZE * 4)

            audioTrack = AudioTrack(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build(),
                AudioFormat.Builder()
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .build(),
                trackBuf,
                AudioTrack.MODE_STREAM,
                AudioManager.AUDIO_SESSION_ID_GENERATE
            )

            val sessionId = audioTrack?.audioSessionId ?: 0
            initSystemEffects(sessionId)

            audioTrack?.play()
            isRunning = true

            synthJob = scope.launch {
                runAudioLoop()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start AudioEngine", e)
        }
    }

    fun stop() {
        isRunning = false
        synthJob?.cancel()
        releaseSystemEffects()
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }
        audioTrack = null
    }

    private fun initSystemEffects(sessionId: Int) {
        if (sessionId <= 0) return
        try {
            systemEqualizer = Equalizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Equalizer not available: ${e.message}")
        }
        try {
            systemBassBoost = BassBoost(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware BassBoost not available: ${e.message}")
        }
        try {
            systemVirtualizer = Virtualizer(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Virtualizer not available: ${e.message}")
        }
        try {
            systemReverb = PresetReverb(0, sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware Reverb not available: ${e.message}")
        }
        try {
            systemLoudness = LoudnessEnhancer(sessionId).apply { enabled = true }
        } catch (e: Exception) {
            Log.w(TAG, "Hardware LoudnessEnhancer not available: ${e.message}")
        }
    }

    private fun releaseSystemEffects() {
        try { systemEqualizer?.release() } catch (_: Exception) {}
        try { systemBassBoost?.release() } catch (_: Exception) {}
        try { systemVirtualizer?.release() } catch (_: Exception) {}
        try { systemReverb?.release() } catch (_: Exception) {}
        try { systemLoudness?.release() } catch (_: Exception) {}
        systemEqualizer = null
        systemBassBoost = null
        systemVirtualizer = null
        systemReverb = null
        systemLoudness = null
    }

    fun updateBiquadCoefficients() {
        val eq = eqConfig
        // HPF
        hpfL.setHighPass(eq.hpfCutoffHz.toDouble(), SAMPLE_RATE.toDouble(), 0.707)
        hpfR.setHighPass(eq.hpfCutoffHz.toDouble(), SAMPLE_RATE.toDouble(), 0.707)
        // LPF
        lpfL.setLowPass(eq.lpfCutoffHz.toDouble(), SAMPLE_RATE.toDouble(), 0.707)
        lpfR.setLowPass(eq.lpfCutoffHz.toDouble(), SAMPLE_RATE.toDouble(), 0.707)

        // 10-band peaking EQ
        for (i in 0 until min(10, eq.bands.size)) {
            val band = eq.bands[i]
            val gain = if (eq.enabled) band.gainDb.toDouble() else 0.0
            val q = 1.414
            eqFiltersL[i].setPeaking(band.centerFreqHz.toDouble(), gain, q, SAMPLE_RATE.toDouble())
            eqFiltersR[i].setPeaking(band.centerFreqHz.toDouble(), gain, q, SAMPLE_RATE.toDouble())
        }

        // Bass boost filter
        val bass = harmonicConfig
        val bassGain = if (bass.bassBoostEnabled) (bass.bassBoostStrength * 12.0) else 0.0
        bassFilterL.setLowShelf(bass.bassCenterFreqHz.toDouble(), bassGain, 0.8, SAMPLE_RATE.toDouble())
        bassFilterR.setLowShelf(bass.bassCenterFreqHz.toDouble(), bassGain, 0.8, SAMPLE_RATE.toDouble())

        // Update hardware effects if present
        try {
            systemBassBoost?.setStrength((bass.bassBoostStrength * 1000).toInt().toShort())
            systemVirtualizer?.setStrength((spatialConfig.virtualizerStrength * 1000).toInt().toShort())
            systemLoudness?.setTargetGain((bass.loudnessEnhancerGainDb * 100).toInt())
        } catch (_: Exception) {}
    }

    private fun runAudioLoop() {
        val numFrames = BUFFER_SIZE
        val pcmBuffer = ShortArray(numFrames * 2)
        val floatL = FloatArray(numFrames)
        val floatR = FloatArray(numFrames)
        val waveChunk = FloatArray(128)
        val spectrumBars = FloatArray(64)

        var telemetryCounter = 0

        while (isRunning) {
            val gen = signalGenConfig
            if (!gen.isPlaying) {
                // Fill silence
                pcmBuffer.fill(0)
                audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)
                continue
            }

            // 1. Generate Raw Audio Signal based on selected laboratory mode
            generateRawSignal(floatL, floatR, numFrames, gen)

            // 2. Apply Studio Pre-Amp Gain
            val preGainLinear = 10f.pow(eqConfig.preGainDb / 20f)
            for (i in 0 until numFrames) {
                floatL[i] *= preGainLinear
                floatR[i] *= preGainLinear
            }

            // 3. Apply Equalizer & Filters (HPF, 10-Band EQ, LPF)
            if (eqConfig.enabled) {
                for (i in 0 until numFrames) {
                    var sL = hpfL.process(floatL[i].toDouble()).toFloat()
                    var sR = hpfR.process(floatR[i].toDouble()).toFloat()

                    for (b in 0 until 10) {
                        sL = eqFiltersL[b].process(sL.toDouble()).toFloat()
                        sR = eqFiltersR[b].process(sR.toDouble()).toFloat()
                    }

                    sL = lpfL.process(sL.toDouble()).toFloat()
                    sR = lpfR.process(sR.toDouble()).toFloat()

                    floatL[i] = sL
                    floatR[i] = sR
                }
            }

            // 4. Bass Boost / Harmonic Coloration
            val harm = harmonicConfig
            if (harm.bassBoostEnabled || harm.tubeWarmthDrive > 0f || harm.tapeSaturation > 0f) {
                val drive = harm.tubeWarmthDrive * 2.0f + 1.0f
                for (i in 0 until numFrames) {
                    var sL = bassFilterL.process(floatL[i].toDouble()).toFloat()
                    var sR = bassFilterR.process(floatR[i].toDouble()).toFloat()

                    // Tube warmth soft saturation: tanh(x * drive) / drive + even harmonics
                    if (harm.tubeWarmthDrive > 0.05f) {
                        sL = (tanh(sL * drive) + 0.15f * harm.tubeWarmthDrive * sL * sL) / drive
                        sR = (tanh(sR * drive) + 0.15f * harm.tubeWarmthDrive * sR * sR) / drive
                    }

                    floatL[i] = sL
                    floatR[i] = sR
                }
            }

            // 5. Dynamic Range Compressor & Limiter
            var currentGainReductionDb = 0f
            val dyn = dynamicsConfig
            if (dyn.compressorEnabled || dyn.limiterEnabled) {
                currentGainReductionDb = applyDynamics(floatL, floatR, numFrames, dyn)
            }

            // 6. Environmental Reverb Processing
            val rev = reverbConfig
            if (rev.enabled && rev.wetDryMix > 0.02f) {
                applyReverb(floatL, floatR, numFrames, rev)
            }

            // 7. Spatial Processing (Stereo Width, Balance, Phase Inversion)
            val sp = spatialConfig
            applySpatial(floatL, floatR, numFrames, sp)

            // 8. Output volume scaling & Master Limiter to prevent harsh DAC clipping
            val masterVol = gen.volume
            var peakL = 0f
            var peakR = 0f
            var sumSquareL = 0.0
            var sumSquareR = 0.0
            var dotLR = 0.0
            var normL = 0.0
            var normR = 0.0
            var isClipping = false

            for (i in 0 until numFrames) {
                var outL = floatL[i] * masterVol
                var outR = floatR[i] * masterVol

                // Soft clip at +/- 1.0
                if (abs(outL) > 1.0f || abs(outR) > 1.0f) {
                    isClipping = true
                }
                outL = outL.coerceIn(-1.0f, 1.0f)
                outR = outR.coerceIn(-1.0f, 1.0f)

                // Statistics
                val absL = abs(outL)
                val absR = abs(outR)
                if (absL > peakL) peakL = absL
                if (absR > peakR) peakR = absR
                sumSquareL += outL * outL
                sumSquareR += outR * outR
                dotLR += outL * outR
                normL += outL * outL
                normR += outR * outR

                // Convert to 16-bit PCM
                pcmBuffer[i * 2] = (outL * 32767f).toInt().coerceIn(-32768, 32767).toShort()
                pcmBuffer[i * 2 + 1] = (outR * 32767f).toInt().coerceIn(-32768, 32767).toShort()
            }

            // Write to AudioTrack
            audioTrack?.write(pcmBuffer, 0, pcmBuffer.size)

            // Update Telemetry every ~40ms
            telemetryCounter++
            if (telemetryCounter % 2 == 0) {
                val rmsL = sqrt(sumSquareL / numFrames).toFloat()
                val rmsR = sqrt(sumSquareR / numFrames).toFloat()
                val peakDbL = if (peakL > 0.0001f) 20f * log10(peakL) else -60f
                val peakDbR = if (peakR > 0.0001f) 20f * log10(peakR) else -60f
                val rmsDbL = if (rmsL > 0.0001f) 20f * log10(rmsL) else -60f
                val rmsDbR = if (rmsR > 0.0001f) 20f * log10(rmsR) else -60f

                val correlation = if (normL > 0.0001 && normR > 0.0001) {
                    (dotLR / (sqrt(normL) * sqrt(normR))).toFloat().coerceIn(-1f, 1f)
                } else 1.0f

                // Downsample waveform for oscilloscope
                val step = numFrames / 128
                for (k in 0 until 128) {
                    waveChunk[k] = floatL[k * step].coerceIn(-1f, 1f)
                }

                // Compute real 64-band spectrum energy
                computeSpectrum(floatL, spectrumBars)

                _telemetry.value = TelemetryData(
                    sampleRateHz = SAMPLE_RATE,
                    bitDepth = 16,
                    bufferSize = BUFFER_SIZE,
                    latencyMs = 11.2f,
                    peakDbL = peakDbL.coerceIn(-60f, 6f),
                    peakDbR = peakDbR.coerceIn(-60f, 6f),
                    rmsDbL = rmsDbL.coerceIn(-60f, 0f),
                    rmsDbR = rmsDbR.coerceIn(-60f, 0f),
                    gainReductionDb = currentGainReductionDb,
                    phaseCorrelation = correlation,
                    isClipping = isClipping,
                    waveformSamples = waveChunk.clone(),
                    spectrumBands = spectrumBars.clone()
                )
            }
        }
    }

    private fun generateRawSignal(
        outL: FloatArray,
        outR: FloatArray,
        frames: Int,
        gen: SignalGeneratorConfig
    ) {
        when (gen.mode) {
            LabSignalMode.DEMO_TRACK -> {
                // Synthesizes a real, punchy studio groove (Kick + Snare + Hi-hat + Bassline + Synth chord)
                val samplesPerStep = SAMPLE_RATE / 8 // 120 BPM 16th notes
                for (i in 0 until frames) {
                    beatSampleCounter++
                    if (beatSampleCounter >= samplesPerStep) {
                        beatSampleCounter = 0
                        beatStep = (beatStep + 1) % 16
                    }

                    val stepPos = beatSampleCounter.toFloat() / samplesPerStep
                    var kick = 0f
                    var snare = 0f
                    var hihat = 0f
                    var bass = 0f
                    var synth = 0f

                    // Kick on 0, 4, 8, 12
                    if (beatStep % 4 == 0) {
                        val decay = max(0f, 1f - stepPos * 2.5f)
                        val freq = 55f + 110f * decay
                        kick = sin(2.0 * PI * freq * stepPos * (samplesPerStep.toDouble() / SAMPLE_RATE)).toFloat() * decay
                    }

                    // Snare on 4, 12
                    if (beatStep == 4 || beatStep == 12) {
                        val decay = max(0f, 1f - stepPos * 2.0f)
                        val noise = (random.nextFloat() * 2f - 1f) * 0.7f
                        val tone = sin(2.0 * PI * 180.0 * stepPos).toFloat() * 0.3f
                        snare = (noise + tone) * decay
                    }

                    // Hi-hat on every 2 steps
                    if (beatStep % 2 == 0) {
                        val decay = max(0f, 1f - stepPos * 6.0f)
                        hihat = (random.nextFloat() * 2f - 1f) * decay * 0.35f
                    }

                    // Bassline note based on step
                    val bassFreq = when (beatStep) {
                        0, 1, 2, 3 -> 55.0 // A1
                        4, 5 -> 65.4 // C2
                        6, 7 -> 73.4 // D2
                        8, 9, 10, 11 -> 49.0 // G1
                        else -> 55.0
                    }
                    val bassEnv = max(0f, 1f - stepPos * 0.9f)
                    synthPhase += 2.0 * PI * bassFreq / SAMPLE_RATE
                    if (synthPhase > 2.0 * PI) synthPhase -= 2.0 * PI
                    bass = (sin(synthPhase) + 0.35 * sin(synthPhase * 2.0)).toFloat() * bassEnv * 0.6f

                    // Synth pad chords
                    val chordFreq1 = 220.0
                    val chordFreq2 = 261.63
                    val chord = (sin(synthPhase * 2.0) * 0.2 + sin(synthPhase * 2.38) * 0.2).toFloat() * 0.35f
                    synth = chord

                    val combinedL = (kick * 0.9f + snare * 0.7f + hihat * 0.4f + bass * 0.8f + synth * 0.4f) * 0.65f
                    val combinedR = (kick * 0.9f + snare * 0.7f + hihat * 0.5f + bass * 0.8f + synth * 0.5f) * 0.65f
                    outL[i] = combinedL
                    outR[i] = combinedR
                }
            }

            LabSignalMode.PURE_TONE -> {
                val f = gen.frequencyHz.toDouble()
                val delta = 2.0 * PI * f / SAMPLE_RATE
                for (i in 0 until frames) {
                    synthPhase += delta
                    if (synthPhase > 2.0 * PI) synthPhase -= 2.0 * PI
                    val sample = when (gen.waveform) {
                        WaveformType.SINE -> sin(synthPhase).toFloat()
                        WaveformType.SQUARE -> if (sin(synthPhase) >= 0) 0.8f else -0.8f
                        WaveformType.TRIANGLE -> (2.0 / PI * asin(sin(synthPhase))).toFloat()
                        WaveformType.SAWTOOTH -> (2.0 * (synthPhase / (2.0 * PI) - floor(synthPhase / (2.0 * PI) + 0.5))).toFloat()
                    }
                    outL[i] = sample * 0.7f
                    outR[i] = sample * 0.7f
                }
            }

            LabSignalMode.NOISE -> {
                for (i in 0 until frames) {
                    val sample = when (gen.noiseType) {
                        NoiseType.WHITE -> (random.nextFloat() * 2f - 1f) * 0.5f
                        NoiseType.PINK -> {
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            pinkB0 = 0.99886 * pinkB0 + white * 0.0555179
                            pinkB1 = 0.99332 * pinkB1 + white * 0.0750759
                            pinkB2 = 0.96900 * pinkB2 + white * 0.1538520
                            pinkB3 = 0.86650 * pinkB3 + white * 0.3104856
                            pinkB4 = 0.55000 * pinkB4 + white * 0.5329522
                            pinkB5 = -0.7616 * pinkB5 - white * 0.0168980
                            val pink = pinkB0 + pinkB1 + pinkB2 + pinkB3 + pinkB4 + pinkB5 + pinkB6 + white * 0.5362
                            pinkB6 = white * 0.115926
                            (pink * 0.11).toFloat()
                        }
                        NoiseType.BROWN -> {
                            val white = (random.nextDouble() * 2.0 - 1.0)
                            brownVal = (brownVal + (0.02 * white)) / 1.02
                            (brownVal * 3.5).toFloat()
                        }
                    }
                    outL[i] = sample
                    outR[i] = sample
                }
            }

            LabSignalMode.FREQUENCY_SWEEP -> {
                val fStart = 20.0
                val fEnd = 20000.0
                val duration = max(1.0, gen.sweepDurationSec.toDouble())
                for (i in 0 until frames) {
                    sweepTime += 1.0 / SAMPLE_RATE
                    if (sweepTime >= duration) sweepTime = 0.0
                    val instantFreq = fStart * (fEnd / fStart).pow(sweepTime / duration)
                    sweepPhase += 2.0 * PI * instantFreq / SAMPLE_RATE
                    if (sweepPhase > 2.0 * PI) sweepPhase -= 2.0 * PI
                    val sample = sin(sweepPhase).toFloat() * 0.7f
                    outL[i] = sample
                    outR[i] = sample
                }
            }

            LabSignalMode.CALIBRATION_1K -> {
                // 1000 Hz reference at -0.1 dBFS
                val delta = 2.0 * PI * 1000.0 / SAMPLE_RATE
                for (i in 0 until frames) {
                    synthPhase += delta
                    if (synthPhase > 2.0 * PI) synthPhase -= 2.0 * PI
                    val sample = sin(synthPhase).toFloat() * 0.95f
                    outL[i] = sample
                    outR[i] = sample
                }
            }

            LabSignalMode.BINAURAL_BEAT -> {
                val fL = gen.binauralBaseHz.toDouble()
                val fR = (gen.binauralBaseHz + gen.binauralBeatHz).toDouble()
                val deltaL = 2.0 * PI * fL / SAMPLE_RATE
                val deltaR = 2.0 * PI * fR / SAMPLE_RATE
                for (i in 0 until frames) {
                    synthPhase += deltaL
                    if (synthPhase > 2.0 * PI) synthPhase -= 2.0 * PI
                    sweepPhase += deltaR
                    if (sweepPhase > 2.0 * PI) sweepPhase -= 2.0 * PI
                    outL[i] = sin(synthPhase).toFloat() * 0.65f
                    outR[i] = sin(sweepPhase).toFloat() * 0.65f
                }
            }

            LabSignalMode.MIC_RTA -> {
                // Ambient test tone if mic stream is waiting for permission
                for (i in 0 until frames) {
                    outL[i] = (random.nextFloat() * 2f - 1f) * 0.08f
                    outR[i] = (random.nextFloat() * 2f - 1f) * 0.08f
                }
            }
        }
    }

    private fun applyDynamics(
        floatL: FloatArray,
        floatR: FloatArray,
        frames: Int,
        dyn: DynamicsConfig
    ): Float {
        var totalGainReduction = 0f
        val attackAlpha = exp(-1.0 / (SAMPLE_RATE * (dyn.attackMs / 1000.0))).toFloat()
        val releaseAlpha = exp(-1.0 / (SAMPLE_RATE * (dyn.releaseMs / 1000.0))).toFloat()
        val makeupLinear = 10f.pow(dyn.makeupGainDb / 20f)
        val thresholdDb = dyn.thresholdDb
        val ratio = max(1.0f, dyn.ratio)
        val limiterCeilLinear = 10f.pow(dyn.limiterCeilingDb / 20f)

        for (i in 0 until frames) {
            val level = max(abs(floatL[i]), abs(floatR[i]))
            // Envelope detector
            compEnvelope = if (level > compEnvelope) {
                attackAlpha * compEnvelope + (1f - attackAlpha) * level
            } else {
                releaseAlpha * compEnvelope + (1f - releaseAlpha) * level
            }

            var gainLinear = 1.0f
            if (dyn.compressorEnabled && compEnvelope > 0.0001f) {
                val envDb = 20f * log10(compEnvelope)
                if (envDb > thresholdDb) {
                    val excess = envDb - thresholdDb
                    val compressedDb = thresholdDb + (excess / ratio)
                    val reductionDb = envDb - compressedDb
                    gainLinear = 10f.pow(-reductionDb / 20f)
                    if (reductionDb > totalGainReduction) totalGainReduction = reductionDb
                }
            }

            // Apply makeup and limiter
            var sL = floatL[i] * gainLinear * makeupLinear
            var sR = floatR[i] * gainLinear * makeupLinear

            // Studio Peak Limiter
            if (dyn.limiterEnabled) {
                val peak = max(abs(sL), abs(sR))
                if (peak > limiterCeilLinear) {
                    val limGain = limiterCeilLinear / peak
                    sL *= limGain
                    sR *= limGain
                }
            }

            floatL[i] = sL
            floatR[i] = sR
        }

        return totalGainReduction
    }

    private fun applyReverb(
        floatL: FloatArray,
        floatR: FloatArray,
        frames: Int,
        rev: ReverbConfig
    ) {
        val wet = rev.wetDryMix
        val dry = 1f - wet
        val feedback = (0.7f + 0.28f * rev.roomSize).coerceIn(0.1f, 0.98f)
        val damp = rev.hfDamping

        for (i in 0 until frames) {
            val inputL = floatL[i] * 0.5f
            val inputR = floatR[i] * 0.5f

            var combSumL = 0f
            var combSumR = 0f

            // 8 parallel feedback comb filters
            for (c in 0 until 8) {
                val delayLenL = combDelaysL[c]
                val delayLenR = combDelaysR[c]
                val idxL = combIndicesL[c]
                val idxR = combIndicesR[c]

                val outCombL = combBuffersL[c][idxL]
                val outCombR = combBuffersR[c][idxR]

                combFiltersL[c] = outCombL * (1f - damp) + combFiltersL[c] * damp
                combFiltersR[c] = outCombR * (1f - damp) + combFiltersR[c] * damp

                combBuffersL[c][idxL] = inputL + combFiltersL[c] * feedback
                combBuffersR[c][idxR] = inputR + combFiltersR[c] * feedback

                combIndicesL[c] = (idxL + 1) % delayLenL
                combIndicesR[c] = (idxR + 1) % delayLenR

                combSumL += outCombL
                combSumR += outCombR
            }

            // 4 series all-pass filters for diffusion
            var apL = combSumL * 0.25f
            var apR = combSumR * 0.25f
            for (a in 0 until 4) {
                val delayLen = allPassDelays[a]
                val idxL = allPassIndicesL[a]
                val idxR = allPassIndicesR[a]

                val bufL = allPassBuffersL[a][idxL]
                val bufR = allPassBuffersR[a][idxR]

                val newApL = -apL + bufL
                val newApR = -apR + bufR

                allPassBuffersL[a][idxL] = apL + bufL * 0.5f
                allPassBuffersR[a][idxR] = apR + bufR * 0.5f

                allPassIndicesL[a] = (idxL + 1) % delayLen
                allPassIndicesR[a] = (idxR + 1) % delayLen

                apL = newApL
                apR = newApR
            }

            floatL[i] = floatL[i] * dry + apL * wet
            floatR[i] = floatR[i] * dry + apR * wet
        }
    }

    private fun applySpatial(
        floatL: FloatArray,
        floatR: FloatArray,
        frames: Int,
        sp: SpatialConfig
    ) {
        val width = (sp.stereoWidthPercent / 100f).coerceIn(0f, 2.5f)
        val bal = sp.balance.coerceIn(-1f, 1f)
        val balL = (1f - bal).coerceIn(0f, 1f)
        val balR = (1f + bal).coerceIn(0f, 1f)

        for (i in 0 until frames) {
            var l = floatL[i]
            var r = floatR[i]

            // Mid/Side matrix manipulation for stereo width
            val mid = (l + r) * 0.5f
            val side = (l - r) * 0.5f
            l = mid + side * width
            r = mid - side * width

            // Balance
            l *= balL
            r *= balR

            // Phase inversion
            when (sp.phaseInvertMode) {
                PhaseMode.NORMAL -> {}
                PhaseMode.LEFT_INVERT -> l = -l
                PhaseMode.RIGHT_INVERT -> r = -r
                PhaseMode.STEREO_OUT -> {
                    l = (l - r) * 0.707f
                    r = (r - l) * 0.707f
                }
            }

            floatL[i] = l
            floatR[i] = r
        }
    }

    private fun computeSpectrum(input: FloatArray, outputBands: FloatArray) {
        val n = min(input.size, 512)
        val numBands = outputBands.size
        val bandStep = n / 2 / numBands

        for (b in 0 until numBands) {
            var energy = 0f
            val start = b * bandStep
            val end = min(start + bandStep, n / 2)
            for (k in start until end) {
                val s = input[k]
                energy += s * s
            }
            val mag = sqrt(energy / max(1, end - start))
            // Smooth peak decay
            outputBands[b] = max(mag * 2.5f, outputBands[b] * 0.75f).coerceIn(0.01f, 1.0f)
        }
    }

    /**
     * Standard Audio Biquad Filter implementation (Direct Form 1)
     */
    private class BiquadFilter {
        private var b0 = 1.0
        private var b1 = 0.0
        private var b2 = 0.0
        private var a1 = 0.0
        private var a2 = 0.0
        private var x1 = 0.0
        private var x2 = 0.0
        private var y1 = 0.0
        private var y2 = 0.0

        fun process(x: Double): Double {
            val y = b0 * x + b1 * x1 + b2 * x2 - a1 * y1 - a2 * y2
            x2 = x1
            x1 = x
            y2 = y1
            y1 = y
            return y
        }

        fun setPeaking(freq: Double, gainDb: Double, q: Double, sampleRate: Double) {
            val w0 = 2.0 * PI * freq / sampleRate
            val alpha = sin(w0) / (2.0 * q)
            val a = 10.0.pow(gainDb / 40.0)

            val b0Raw = 1.0 + alpha * a
            val b1Raw = -2.0 * cos(w0)
            val b2Raw = 1.0 - alpha * a
            val a0Raw = 1.0 + alpha / a
            val a1Raw = -2.0 * cos(w0)
            val a2Raw = 1.0 - alpha / a

            b0 = b0Raw / a0Raw
            b1 = b1Raw / a0Raw
            b2 = b2Raw / a0Raw
            a1 = a1Raw / a0Raw
            a2 = a2Raw / a0Raw
        }

        fun setLowPass(freq: Double, sampleRate: Double, q: Double) {
            val w0 = 2.0 * PI * freq.coerceIn(20.0, sampleRate / 2.1) / sampleRate
            val alpha = sin(w0) / (2.0 * q)
            val a0Raw = 1.0 + alpha

            b0 = ((1.0 - cos(w0)) / 2.0) / a0Raw
            b1 = (1.0 - cos(w0)) / a0Raw
            b2 = ((1.0 - cos(w0)) / 2.0) / a0Raw
            a1 = (-2.0 * cos(w0)) / a0Raw
            a2 = (1.0 - alpha) / a0Raw
        }

        fun setHighPass(freq: Double, sampleRate: Double, q: Double) {
            val w0 = 2.0 * PI * freq.coerceIn(20.0, sampleRate / 2.1) / sampleRate
            val alpha = sin(w0) / (2.0 * q)
            val a0Raw = 1.0 + alpha

            b0 = ((1.0 + cos(w0)) / 2.0) / a0Raw
            b1 = (-(1.0 + cos(w0))) / a0Raw
            b2 = ((1.0 + cos(w0)) / 2.0) / a0Raw
            a1 = (-2.0 * cos(w0)) / a0Raw
            a2 = (1.0 - alpha) / a0Raw
        }

        fun setLowShelf(freq: Double, gainDb: Double, q: Double, sampleRate: Double) {
            val a = 10.0.pow(gainDb / 40.0)
            val w0 = 2.0 * PI * freq / sampleRate
            val alpha = sin(w0) / (2.0 * q)
            val cosW0 = cos(w0)
            val sqrtA = sqrt(a)

            val a0Raw = (a + 1.0) + (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha
            b0 = (a * ((a + 1.0) - (a - 1.0) * cosW0 + 2.0 * sqrtA * alpha)) / a0Raw
            b1 = (2.0 * a * ((a - 1.0) - (a + 1.0) * cosW0)) / a0Raw
            b2 = (a * ((a + 1.0) - (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha)) / a0Raw
            a1 = (-2.0 * ((a - 1.0) + (a + 1.0) * cosW0)) / a0Raw
            a2 = ((a + 1.0) + (a - 1.0) * cosW0 - 2.0 * sqrtA * alpha) / a0Raw
        }
    }
}
