package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
fun GeneratorTab(
    config: SignalGeneratorConfig,
    onSelectMode: (LabSignalMode) -> Unit,
    onSelectWaveform: (WaveformType) -> Unit,
    onToneFreqChange: (Float) -> Unit,
    onSelectNoiseType: (NoiseType) -> Unit,
    onBinauralBeatChange: (Float) -> Unit,
    onTogglePlay: () -> Unit,
    onVolumeChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("generator_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Generator Module Card
        RackModuleCard(
            title = "Laboratory Signal Generator",
            subtitle = "PRECISION SYNTHESIS & ACOUSTIC CALIBRATION ENGINE",
            isEnabled = config.isPlaying,
            onToggleEnabled = onTogglePlay,
            accentColor = NeonCyan,
            testTag = "gen_rack_card"
        ) {
            // Signal Mode Selector Chips
            Text(
                text = "SYNTHESIS MODE",
                color = TextSecondary,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LabSignalMode.values().forEach { mode ->
                    val isSelected = config.mode == mode
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectMode(mode) },
                        label = {
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                            selectedLabelColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("gen_mode_${mode.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Mode-specific Parameter Controls
            when (config.mode) {
                LabSignalMode.DEMO_TRACK -> {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "PLAYING MULTI-GENRE STUDIO BEAT",
                            color = NeonGreen,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Kick 55Hz, Snare 180Hz, Hi-hats 10kHz, Sub Bass & Chords",
                            color = TextSecondary,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Test EQ bands, reverb room size, and compressor dynamics in real-time!",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                LabSignalMode.PURE_TONE -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Waveform selection
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            WaveformType.values().forEach { wave ->
                                val isSelected = config.waveform == wave
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectWaveform(wave) },
                                    label = {
                                        Text(
                                            text = wave.label,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("wave_${wave.name}")
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Frequency Knob & Stepper Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RotaryKnob(
                                value = config.frequencyHz,
                                onValueChange = onToneFreqChange,
                                range = 20f..20000f,
                                defaultValue = 440f,
                                label = "OSCILLATOR FREQ",
                                valueFormatter = {
                                    if (it >= 1000f) "%.2f kHz".format(it / 1000f) else "%.1f Hz".format(it)
                                },
                                accentColor = NeonCyan,
                                size = 72.dp,
                                testTag = "knob_tone_freq"
                            )

                            // Frequency Steppers
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FreqStepButton("-100Hz") { onToneFreqChange((config.frequencyHz - 100f).coerceAtLeast(20f)) }
                                    FreqStepButton("+100Hz") { onToneFreqChange((config.frequencyHz + 100f).coerceAtMost(20000f)) }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    FreqStepButton("A4 440") { onToneFreqChange(440f) }
                                    FreqStepButton("1 kHz") { onToneFreqChange(1000f) }
                                }
                            }
                        }
                    }
                }

                LabSignalMode.NOISE -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            NoiseType.values().forEach { noise ->
                                val isSelected = config.noiseType == noise
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectNoiseType(noise) },
                                    label = {
                                        Text(
                                            text = noise.label,
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = NeonCyan
                                    ),
                                    modifier = Modifier.testTag("noise_${noise.name}")
                                )
                            }
                        }
                    }
                }

                LabSignalMode.FREQUENCY_SWEEP -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "LOGARITHMIC CHIRP SWEEP (20 Hz -> 20,000 Hz)",
                            color = NeonAmber,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Ideal for identifying acoustic speaker rattles and room resonance nodes.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                LabSignalMode.CALIBRATION_1K -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "1,000.0 Hz STANDARD CALIBRATION TONE",
                            color = NeonCyan,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Amplitude fixed at -0.1 dBFS for DAC zero-alignment and amplifier gain calibration.",
                            color = TextSecondary,
                            fontSize = 10.sp
                        )
                    }
                }

                LabSignalMode.BINAURAL_BEAT -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "BINAURAL BRAINWAVE ENTRAINMENT (HEADPHONES REQUIRED)",
                            color = NeonPurple,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        val brainwaves = listOf(
                            "Delta (2 Hz)" to 2f,
                            "Theta (6 Hz)" to 6f,
                            "Alpha (10 Hz)" to 10f,
                            "Beta (20 Hz)" to 20f,
                            "Gamma (40 Hz)" to 40f
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            brainwaves.forEach { (lbl, hz) ->
                                val isSel = config.binauralBeatHz == hz
                                FilterChip(
                                    selected = isSel,
                                    onClick = { onBinauralBeatChange(hz) },
                                    label = { Text(lbl, fontSize = 10.sp, fontFamily = FontFamily.Monospace) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonPurple.copy(alpha = 0.25f),
                                        selectedLabelColor = NeonPurple
                                    ),
                                    modifier = Modifier.testTag("binaural_$lbl")
                                )
                            }
                        }
                    }
                }

                LabSignalMode.MIC_RTA -> {
                    Text(
                        text = "Real-time acoustic analysis of microphone input.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Master Generator Transport & Output Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play / Pause Button
                Button(
                    onClick = onTogglePlay,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (config.isPlaying) NeonRed.copy(alpha = 0.25f) else NeonGreen.copy(alpha = 0.25f),
                        contentColor = if (config.isPlaying) NeonRed else NeonGreen
                    ),
                    modifier = Modifier.testTag("gen_play_toggle_btn")
                ) {
                    Icon(
                        imageVector = if (config.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (config.isPlaying) "Mute Generator" else "Start Generator",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (config.isPlaying) "MUTE / STOP" else "GENERATE AUDIO",
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Volume Knob
                RotaryKnob(
                    value = config.volume,
                    onValueChange = onVolumeChange,
                    range = 0f..1f,
                    defaultValue = 0.75f,
                    label = "OUTPUT VOL",
                    valueFormatter = { "%.0f %%".format(it * 100f) },
                    accentColor = NeonCyan,
                    size = 60.dp,
                    testTag = "knob_gen_vol"
                )
            }
        }
    }
}

@Composable
private fun FreqStepButton(label: String, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = NeonCyan),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(text = label, fontSize = 9.sp, fontFamily = FontFamily.Monospace)
    }
}
