package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun ColorationTab(
    spatialConfig: SpatialConfig,
    harmonicConfig: HarmonicConfig,
    onVirtualizerChange: (Float) -> Unit,
    onStereoWidthChange: (Float) -> Unit,
    onBalanceChange: (Float) -> Unit,
    onPhaseModeChange: (PhaseMode) -> Unit,
    onToggleVirtualizer: () -> Unit,
    onResetSpatial: () -> Unit,
    onBassStrengthChange: (Float) -> Unit,
    onBassFreqChange: (Int) -> Unit,
    onTubeWarmthChange: (Float) -> Unit,
    onTapeSatChange: (Float) -> Unit,
    onAuralExciterChange: (Float) -> Unit,
    onDeEsserChange: (Float) -> Unit,
    onLoudnessChange: (Float) -> Unit,
    onToggleBassBoost: () -> Unit,
    onResetHarmonics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("coloration_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Module 1: 3D Spatial Audio & Stereo Imager
        RackModuleCard(
            title = "Spatial Audio Imager",
            subtitle = "3D BINAURAL VIRTUALIZER // STEREO FIELD PROCESSOR",
            isEnabled = spatialConfig.virtualizerEnabled,
            onToggleEnabled = onToggleVirtualizer,
            onReset = onResetSpatial,
            accentColor = NeonPurple,
            testTag = "spatial_rack_card"
        ) {
            // Stereo Field Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = spatialConfig.virtualizerStrength,
                    onValueChange = onVirtualizerChange,
                    range = 0f..1f,
                    defaultValue = 0.45f,
                    label = "3D VIRTUALIZER",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonPurple,
                    size = 66.dp,
                    testTag = "knob_virtualizer"
                )

                RotaryKnob(
                    value = spatialConfig.stereoWidthPercent,
                    onValueChange = onStereoWidthChange,
                    range = 0f..200f,
                    defaultValue = 120f,
                    label = "STEREO WIDTH",
                    valueFormatter = { "%.0f %%".format(it) },
                    accentColor = NeonPurple,
                    size = 66.dp,
                    testTag = "knob_stereo_width"
                )

                RotaryKnob(
                    value = spatialConfig.balance,
                    onValueChange = onBalanceChange,
                    range = -1f..1f,
                    defaultValue = 0f,
                    label = "L / R BALANCE",
                    valueFormatter = {
                        when {
                            it < -0.05f -> "L %.0f%%".format(-it * 100f)
                            it > 0.05f -> "R %.0f%%".format(it * 100f)
                            else -> "CENTER"
                        }
                    },
                    accentColor = NeonPurple,
                    size = 66.dp,
                    testTag = "knob_balance"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Channel Phase Alignment Selection
            Text(
                text = "CHANNEL PHASE POLARITY",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PhaseMode.values().forEach { mode ->
                    val isSelected = spatialConfig.phaseInvertMode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onPhaseModeChange(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonPurple.copy(alpha = 0.2f),
                            selectedLabelColor = NeonPurple
                        ),
                        modifier = Modifier.testTag("phase_chip_${mode.name}")
                    )
                }
            }
        }

        // Module 2: Harmonic Coloration & Bass Enhancement
        RackModuleCard(
            title = "Analog Coloration & Bass",
            subtitle = "TUBE WARMTH // SUB-HARMONICS // AURAL EXCITER",
            isEnabled = harmonicConfig.bassBoostEnabled,
            onToggleEnabled = onToggleBassBoost,
            onReset = onResetHarmonics,
            accentColor = NeonOrange,
            testTag = "harmonic_rack_card"
        ) {
            // Bass Cutoff Frequency selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BASS HARMONIC CUTOFF",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(50, 80, 120, 160).forEach { freq ->
                        val isSel = harmonicConfig.bassCenterFreqHz == freq
                        FilterChip(
                            selected = isSel,
                            onClick = { onBassFreqChange(freq) },
                            label = { Text("$freq Hz", fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonOrange.copy(alpha = 0.25f),
                                selectedLabelColor = NeonOrange
                            ),
                            modifier = Modifier.testTag("bass_freq_$freq")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bass Boost & Tube Warmth Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = harmonicConfig.bassBoostStrength,
                    onValueChange = onBassStrengthChange,
                    range = 0f..1f,
                    defaultValue = 0.40f,
                    label = "SUB-BASS BOOST",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonOrange,
                    size = 64.dp,
                    testTag = "knob_bass_boost"
                )

                RotaryKnob(
                    value = harmonicConfig.tubeWarmthDrive,
                    onValueChange = onTubeWarmthChange,
                    range = 0f..1f,
                    defaultValue = 0.25f,
                    label = "TUBE WARMTH",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonOrange,
                    size = 64.dp,
                    testTag = "knob_tube_drive"
                )

                RotaryKnob(
                    value = harmonicConfig.tapeSaturation,
                    onValueChange = onTapeSatChange,
                    range = 0f..1f,
                    defaultValue = 0.15f,
                    label = "TAPE SATURATION",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonOrange,
                    size = 64.dp,
                    testTag = "knob_tape_sat"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Aural Exciter, De-Esser & Maximizer Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = harmonicConfig.auralExciter,
                    onValueChange = onAuralExciterChange,
                    range = 0f..1f,
                    defaultValue = 0.30f,
                    label = "AIR EXCITER",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonOrange,
                    size = 58.dp,
                    testTag = "knob_exciter"
                )

                RotaryKnob(
                    value = harmonicConfig.deEsserStrength,
                    onValueChange = onDeEsserChange,
                    range = 0f..1f,
                    defaultValue = 0.20f,
                    label = "DE-ESSER",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonOrange,
                    size = 58.dp,
                    testTag = "knob_deesser"
                )

                RotaryKnob(
                    value = harmonicConfig.loudnessEnhancerGainDb,
                    onValueChange = onLoudnessChange,
                    range = 0f..10f,
                    defaultValue = 2.0f,
                    label = "MAXIMIZER GAIN",
                    valueFormatter = { "+%.1f dB".format(it) },
                    accentColor = NeonOrange,
                    size = 58.dp,
                    testTag = "knob_loudness"
                )
            }
        }
    }
}
