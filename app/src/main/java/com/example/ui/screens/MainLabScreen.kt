package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
import com.example.viewmodel.AudioLabViewModel
import com.example.viewmodel.LabTab

@Composable
fun MainLabScreen(
    viewModel: AudioLabViewModel,
    modifier: Modifier = Modifier
) {
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val eqConfig by viewModel.equalizerConfig.collectAsStateWithLifecycle()
    val revConfig by viewModel.reverbConfig.collectAsStateWithLifecycle()
    val dynConfig by viewModel.dynamicsConfig.collectAsStateWithLifecycle()
    val spatialConfig by viewModel.spatialConfig.collectAsStateWithLifecycle()
    val harmConfig by viewModel.harmonicConfig.collectAsStateWithLifecycle()
    val genConfig by viewModel.signalGenConfig.collectAsStateWithLifecycle()
    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val presets by viewModel.allPresets.collectAsStateWithLifecycle()
    val currentPreset by viewModel.currentPreset.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("main_lab_screen"),
        containerColor = StudioDarkBg,
        topBar = {
            StudioTopBar(
                isPlaying = genConfig.isPlaying,
                onTogglePlay = { viewModel.togglePlayback() },
                masterVolume = genConfig.volume,
                onVolumeChange = { viewModel.setMasterVolume(it) },
                peakL = telemetry.peakDbL,
                peakR = telemetry.peakDbR,
                isClipping = telemetry.isClipping,
                currentPresetName = currentPreset?.name ?: "Custom"
            )
        },
        bottomBar = {
            StudioBottomNav(
                currentTab = activeTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (activeTab) {
                LabTab.EQUALIZER -> EqualizerTab(
                    config = eqConfig,
                    onBandGainChange = { band, gain -> viewModel.setBandGain(band, gain) },
                    onPreGainChange = { viewModel.setPreGain(it) },
                    onHpfChange = { viewModel.setHpfCutoff(it) },
                    onLpfChange = { viewModel.setLpfCutoff(it) },
                    onToggleEnabled = { viewModel.toggleEqEnabled() },
                    onReset = { viewModel.resetEq() }
                )

                LabTab.DYNAMICS -> DynamicsTab(
                    config = dynConfig,
                    gainReductionDb = telemetry.gainReductionDb,
                    onThresholdChange = { viewModel.updateDynamics(thresholdDb = it) },
                    onRatioChange = { viewModel.updateDynamics(ratio = it) },
                    onAttackChange = { viewModel.updateDynamics(attackMs = it) },
                    onReleaseChange = { viewModel.updateDynamics(releaseMs = it) },
                    onMakeupChange = { viewModel.updateDynamics(makeupGainDb = it) },
                    onLimiterCeilingChange = { viewModel.updateDynamics(limiterCeilingDb = it) },
                    onToggleCompressor = { viewModel.toggleCompressorEnabled() },
                    onToggleLimiter = { viewModel.toggleLimiterEnabled() },
                    onReset = { viewModel.resetDynamics() }
                )

                LabTab.REVERB -> ReverbTab(
                    config = revConfig,
                    onRoomSizeChange = { viewModel.updateReverb(roomSize = it) },
                    onDecayTimeChange = { viewModel.updateReverb(decayTime = it) },
                    onWetDryChange = { viewModel.updateReverb(wetDry = it) },
                    onDampingChange = { viewModel.updateReverb(damping = it) },
                    onPreDelayChange = { viewModel.updateReverb(preDelay = it) },
                    onDiffusionChange = { viewModel.updateReverb(diffusion = it) },
                    onSelectPreset = { name, room, decay, wet, damp ->
                        viewModel.updateReverb(
                            roomSize = room,
                            decayTime = decay,
                            wetDry = wet,
                            damping = damp,
                            presetName = name
                        )
                    },
                    onToggleEnabled = { viewModel.toggleReverbEnabled() },
                    onReset = { viewModel.resetReverb() }
                )

                LabTab.COLORATION -> ColorationTab(
                    spatialConfig = spatialConfig,
                    harmonicConfig = harmConfig,
                    onVirtualizerChange = { viewModel.updateSpatial(virtualizerStrength = it) },
                    onStereoWidthChange = { viewModel.updateSpatial(stereoWidthPercent = it) },
                    onBalanceChange = { viewModel.updateSpatial(balance = it) },
                    onPhaseModeChange = { viewModel.updateSpatial(phaseMode = it) },
                    onToggleVirtualizer = { viewModel.toggleVirtualizerEnabled() },
                    onResetSpatial = { viewModel.resetSpatial() },
                    onBassStrengthChange = { viewModel.updateHarmonics(bassStrength = it) },
                    onBassFreqChange = { viewModel.updateHarmonics(bassCenterFreq = it) },
                    onTubeWarmthChange = { viewModel.updateHarmonics(tubeDrive = it) },
                    onTapeSatChange = { viewModel.updateHarmonics(tapeSaturation = it) },
                    onAuralExciterChange = { viewModel.updateHarmonics(auralExciter = it) },
                    onDeEsserChange = { viewModel.updateHarmonics(deEsser = it) },
                    onLoudnessChange = { viewModel.updateHarmonics(loudnessGain = it) },
                    onToggleBassBoost = { viewModel.toggleBassBoostEnabled() },
                    onResetHarmonics = { viewModel.resetHarmonics() }
                )

                LabTab.GENERATOR -> GeneratorTab(
                    config = genConfig,
                    onSelectMode = { viewModel.setSignalMode(it) },
                    onSelectWaveform = { viewModel.setWaveform(it) },
                    onToneFreqChange = { viewModel.setToneFrequency(it) },
                    onSelectNoiseType = { viewModel.setNoiseType(it) },
                    onBinauralBeatChange = { viewModel.setBinauralBeat(it) },
                    onTogglePlay = { viewModel.togglePlayback() },
                    onVolumeChange = { viewModel.setMasterVolume(it) }
                )

                LabTab.TELEMETRY -> TelemetryTab(
                    telemetry = telemetry,
                    presets = presets,
                    currentPreset = currentPreset,
                    onSelectPreset = { viewModel.applyPreset(it) },
                    onSaveCustomPreset = { name, desc -> viewModel.saveCustomPreset(name, desc) }
                )
            }
        }
    }
}

@Composable
private fun StudioTopBar(
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    masterVolume: Float,
    onVolumeChange: (Float) -> Unit,
    peakL: Float,
    peakR: Float,
    isClipping: Boolean,
    currentPresetName: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(StudioSurface)
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .border(width = 0.5.dp, color = StudioRackBorder, shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding & Laboratory Status
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) NeonGreen else LedOff)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "DSP LAB PRO",
                        color = NeonCyan,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "PRESET: ${currentPresetName.uppercase()}",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Transport Play/Mute & Volume
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Mini Peak Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "L: %+.0f dB".format(peakL),
                        color = if (peakL > -1f) NeonRed else TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "R: %+.0f dB".format(peakR),
                        color = if (peakR > -1f) NeonRed else TextSecondary,
                        fontSize = 8.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // Master Transport Button
                IconButton(
                    onClick = onTogglePlay,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isPlaying) NeonCyan.copy(alpha = 0.2f) else Color(0xFF1E293B))
                        .testTag("top_transport_btn")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = if (isPlaying) NeonCyan else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioBottomNav(
    currentTab: LabTab,
    onSelectTab: (LabTab) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .navigationBarsPadding()
            .testTag("studio_bottom_nav"),
        containerColor = StudioSurface,
        tonalElevation = 8.dp
    ) {
        LabTab.values().forEach { tab ->
            val isSelected = currentTab == tab
            val icon = when (tab) {
                LabTab.EQUALIZER -> Icons.Default.GraphicEq
                LabTab.DYNAMICS -> Icons.Default.Compress
                LabTab.REVERB -> Icons.Default.SurroundSound
                LabTab.COLORATION -> Icons.Default.Tune
                LabTab.GENERATOR -> Icons.Default.VolumeUp
                LabTab.TELEMETRY -> Icons.Default.QueryStats
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(tab) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = tab.label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = tab.shortLabel,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = StudioDarkBg,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_tab_${tab.name}")
            )
        }
    }
}
