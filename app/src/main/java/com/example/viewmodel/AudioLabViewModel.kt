package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioEngine
import com.example.data.PresetRepository
import com.example.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class LabTab(val label: String, val shortLabel: String) {
    EQUALIZER("Equalizer", "EQ"),
    DYNAMICS("DSP Dynamics", "DYN"),
    REVERB("Reverb & Space", "REV"),
    COLORATION("Harmonics & 3D", "FX"),
    GENERATOR("Lab Generator", "GEN"),
    TELEMETRY("Scopes & Meters", "METER")
}

class AudioLabViewModel : ViewModel() {

    private val audioEngine = AudioEngine()

    private val _activeTab = MutableStateFlow(LabTab.EQUALIZER)
    val activeTab: StateFlow<LabTab> = _activeTab.asStateFlow()

    private val _equalizerConfig = MutableStateFlow(audioEngine.eqConfig)
    val equalizerConfig: StateFlow<EqualizerConfig> = _equalizerConfig.asStateFlow()

    private val _reverbConfig = MutableStateFlow(audioEngine.reverbConfig)
    val reverbConfig: StateFlow<ReverbConfig> = _reverbConfig.asStateFlow()

    private val _dynamicsConfig = MutableStateFlow(audioEngine.dynamicsConfig)
    val dynamicsConfig: StateFlow<DynamicsConfig> = _dynamicsConfig.asStateFlow()

    private val _spatialConfig = MutableStateFlow(audioEngine.spatialConfig)
    val spatialConfig: StateFlow<SpatialConfig> = _spatialConfig.asStateFlow()

    private val _harmonicConfig = MutableStateFlow(audioEngine.harmonicConfig)
    val harmonicConfig: StateFlow<HarmonicConfig> = _harmonicConfig.asStateFlow()

    private val _signalGenConfig = MutableStateFlow(audioEngine.signalGenConfig)
    val signalGenConfig: StateFlow<SignalGeneratorConfig> = _signalGenConfig.asStateFlow()

    val telemetry: StateFlow<TelemetryData> = audioEngine.telemetry

    private val _currentPreset = MutableStateFlow<LabPreset?>(PresetRepository.factoryPresets.firstOrNull())
    val currentPreset: StateFlow<LabPreset?> = _currentPreset.asStateFlow()

    private val _allPresets = MutableStateFlow(PresetRepository.factoryPresets)
    val allPresets: StateFlow<List<LabPreset>> = _allPresets.asStateFlow()

    init {
        audioEngine.start()
        applyPreset(PresetRepository.factoryPresets[1]) // Start with Audiophile Mastering
    }

    override fun onCleared() {
        super.onCleared()
        audioEngine.stop()
    }

    fun selectTab(tab: LabTab) {
        _activeTab.value = tab
    }

    // ==========================================
    // EQUALIZER MODULE
    // ==========================================
    fun setBandGain(bandIndex: Int, gainDb: Float) {
        val currentBands = _equalizerConfig.value.bands.toMutableList()
        if (bandIndex in currentBands.indices) {
            currentBands[bandIndex] = currentBands[bandIndex].copy(gainDb = gainDb)
            val updated = _equalizerConfig.value.copy(bands = currentBands)
            _equalizerConfig.value = updated
            audioEngine.eqConfig = updated
            audioEngine.updateBiquadCoefficients()
        }
    }

    fun setPreGain(gainDb: Float) {
        val updated = _equalizerConfig.value.copy(preGainDb = gainDb)
        _equalizerConfig.value = updated
        audioEngine.eqConfig = updated
    }

    fun setHpfCutoff(freqHz: Float) {
        val updated = _equalizerConfig.value.copy(hpfCutoffHz = freqHz)
        _equalizerConfig.value = updated
        audioEngine.eqConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun setLpfCutoff(freqHz: Float) {
        val updated = _equalizerConfig.value.copy(lpfCutoffHz = freqHz)
        _equalizerConfig.value = updated
        audioEngine.eqConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun toggleEqEnabled() {
        val updated = _equalizerConfig.value.copy(enabled = !_equalizerConfig.value.enabled)
        _equalizerConfig.value = updated
        audioEngine.eqConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun resetEq() {
        val resetBands = _equalizerConfig.value.bands.map { it.copy(gainDb = 0f) }
        val updated = _equalizerConfig.value.copy(
            preGainDb = 0f,
            bands = resetBands,
            hpfCutoffHz = 20f,
            lpfCutoffHz = 20000f
        )
        _equalizerConfig.value = updated
        audioEngine.eqConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    // ==========================================
    // REVERB MODULE
    // ==========================================
    fun toggleReverbEnabled() {
        val updated = _reverbConfig.value.copy(enabled = !_reverbConfig.value.enabled)
        _reverbConfig.value = updated
        audioEngine.reverbConfig = updated
    }

    fun updateReverb(
        roomSize: Float? = null,
        decayTime: Float? = null,
        wetDry: Float? = null,
        damping: Float? = null,
        preDelay: Float? = null,
        diffusion: Float? = null,
        presetName: String? = null
    ) {
        val current = _reverbConfig.value
        val updated = current.copy(
            roomSize = roomSize ?: current.roomSize,
            decayTimeSec = decayTime ?: current.decayTimeSec,
            wetDryMix = wetDry ?: current.wetDryMix,
            hfDamping = damping ?: current.hfDamping,
            preDelayMs = preDelay ?: current.preDelayMs,
            diffusion = diffusion ?: current.diffusion,
            presetName = presetName ?: current.presetName
        )
        _reverbConfig.value = updated
        audioEngine.reverbConfig = updated
    }

    fun resetReverb() {
        val updated = ReverbConfig()
        _reverbConfig.value = updated
        audioEngine.reverbConfig = updated
    }

    // ==========================================
    // DSP DYNAMICS MODULE (Compressor / Limiter)
    // ==========================================
    fun toggleCompressorEnabled() {
        val updated = _dynamicsConfig.value.copy(compressorEnabled = !_dynamicsConfig.value.compressorEnabled)
        _dynamicsConfig.value = updated
        audioEngine.dynamicsConfig = updated
    }

    fun toggleLimiterEnabled() {
        val updated = _dynamicsConfig.value.copy(limiterEnabled = !_dynamicsConfig.value.limiterEnabled)
        _dynamicsConfig.value = updated
        audioEngine.dynamicsConfig = updated
    }

    fun updateDynamics(
        thresholdDb: Float? = null,
        ratio: Float? = null,
        attackMs: Float? = null,
        releaseMs: Float? = null,
        makeupGainDb: Float? = null,
        limiterCeilingDb: Float? = null
    ) {
        val current = _dynamicsConfig.value
        val updated = current.copy(
            thresholdDb = thresholdDb ?: current.thresholdDb,
            ratio = ratio ?: current.ratio,
            attackMs = attackMs ?: current.attackMs,
            releaseMs = releaseMs ?: current.releaseMs,
            makeupGainDb = makeupGainDb ?: current.makeupGainDb,
            limiterCeilingDb = limiterCeilingDb ?: current.limiterCeilingDb
        )
        _dynamicsConfig.value = updated
        audioEngine.dynamicsConfig = updated
    }

    fun resetDynamics() {
        val updated = DynamicsConfig()
        _dynamicsConfig.value = updated
        audioEngine.dynamicsConfig = updated
    }

    // ==========================================
    // SPATIAL MODULE (Virtualizer, Width, Phase)
    // ==========================================
    fun toggleVirtualizerEnabled() {
        val updated = _spatialConfig.value.copy(virtualizerEnabled = !_spatialConfig.value.virtualizerEnabled)
        _spatialConfig.value = updated
        audioEngine.spatialConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun updateSpatial(
        virtualizerStrength: Float? = null,
        stereoWidthPercent: Float? = null,
        balance: Float? = null,
        phaseMode: PhaseMode? = null
    ) {
        val current = _spatialConfig.value
        val updated = current.copy(
            virtualizerStrength = virtualizerStrength ?: current.virtualizerStrength,
            stereoWidthPercent = stereoWidthPercent ?: current.stereoWidthPercent,
            balance = balance ?: current.balance,
            phaseInvertMode = phaseMode ?: current.phaseInvertMode
        )
        _spatialConfig.value = updated
        audioEngine.spatialConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun resetSpatial() {
        val updated = SpatialConfig()
        _spatialConfig.value = updated
        audioEngine.spatialConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    // ==========================================
    // HARMONICS & BASS MODULE
    // ==========================================
    fun toggleBassBoostEnabled() {
        val updated = _harmonicConfig.value.copy(bassBoostEnabled = !_harmonicConfig.value.bassBoostEnabled)
        _harmonicConfig.value = updated
        audioEngine.harmonicConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun updateHarmonics(
        bassStrength: Float? = null,
        bassCenterFreq: Int? = null,
        tubeDrive: Float? = null,
        tapeSaturation: Float? = null,
        auralExciter: Float? = null,
        deEsser: Float? = null,
        loudnessGain: Float? = null
    ) {
        val current = _harmonicConfig.value
        val updated = current.copy(
            bassBoostStrength = bassStrength ?: current.bassBoostStrength,
            bassCenterFreqHz = bassCenterFreq ?: current.bassCenterFreqHz,
            tubeWarmthDrive = tubeDrive ?: current.tubeWarmthDrive,
            tapeSaturation = tapeSaturation ?: current.tapeSaturation,
            auralExciter = auralExciter ?: current.auralExciter,
            deEsserStrength = deEsser ?: current.deEsserStrength,
            loudnessEnhancerGainDb = loudnessGain ?: current.loudnessEnhancerGainDb
        )
        _harmonicConfig.value = updated
        audioEngine.harmonicConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    fun resetHarmonics() {
        val updated = HarmonicConfig()
        _harmonicConfig.value = updated
        audioEngine.harmonicConfig = updated
        audioEngine.updateBiquadCoefficients()
    }

    // ==========================================
    // LABORATORY SIGNAL GENERATOR
    // ==========================================
    fun setSignalMode(mode: LabSignalMode) {
        val updated = _signalGenConfig.value.copy(mode = mode)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun setWaveform(waveform: WaveformType) {
        val updated = _signalGenConfig.value.copy(waveform = waveform)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun setToneFrequency(freqHz: Float) {
        val updated = _signalGenConfig.value.copy(frequencyHz = freqHz)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun setNoiseType(type: NoiseType) {
        val updated = _signalGenConfig.value.copy(noiseType = type)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun setBinauralBeat(beatHz: Float) {
        val updated = _signalGenConfig.value.copy(binauralBeatHz = beatHz)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun togglePlayback() {
        val updated = _signalGenConfig.value.copy(isPlaying = !_signalGenConfig.value.isPlaying)
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    fun setMasterVolume(vol: Float) {
        val updated = _signalGenConfig.value.copy(volume = vol.coerceIn(0f, 1f))
        _signalGenConfig.value = updated
        audioEngine.signalGenConfig = updated
    }

    // ==========================================
    // PRESETS MANAGEMENT
    // ==========================================
    fun applyPreset(preset: LabPreset) {
        _currentPreset.value = preset
        _equalizerConfig.value = preset.eq
        _reverbConfig.value = preset.reverb
        _dynamicsConfig.value = preset.dynamics
        _spatialConfig.value = preset.spatial
        _harmonicConfig.value = preset.harmonic

        audioEngine.eqConfig = preset.eq
        audioEngine.reverbConfig = preset.reverb
        audioEngine.dynamicsConfig = preset.dynamics
        audioEngine.spatialConfig = preset.spatial
        audioEngine.harmonicConfig = preset.harmonic
        audioEngine.updateBiquadCoefficients()
    }

    fun saveCustomPreset(name: String, description: String) {
        val newPreset = LabPreset(
            id = "custom_${System.currentTimeMillis()}",
            name = name,
            category = "Custom User",
            description = description.ifEmpty { "User-defined laboratory rack calibration" },
            eq = _equalizerConfig.value,
            reverb = _reverbConfig.value,
            dynamics = _dynamicsConfig.value,
            spatial = _spatialConfig.value,
            harmonic = _harmonicConfig.value
        )
        val list = _allPresets.value.toMutableList()
        list.add(newPreset)
        _allPresets.value = list
        _currentPreset.value = newPreset
    }
}
