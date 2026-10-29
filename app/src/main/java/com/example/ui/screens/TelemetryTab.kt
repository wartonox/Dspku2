package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import com.example.model.LabPreset
import com.example.model.TelemetryData
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun TelemetryTab(
    telemetry: TelemetryData,
    presets: List<LabPreset>,
    currentPreset: LabPreset?,
    onSelectPreset: (LabPreset) -> Unit,
    onSaveCustomPreset: (name: String, desc: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showSaveDialog by remember { mutableStateOf(false) }
    var presetNameInput by remember { mutableStateOf("") }
    var presetDescInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("telemetry_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Master Audio VU Meter Module
        VuMeter(
            peakL = telemetry.peakDbL,
            peakR = telemetry.peakDbR,
            isClipping = telemetry.isClipping,
            gainReductionDb = telemetry.gainReductionDb,
            modifier = Modifier.fillMaxWidth()
        )

        // Real-Time 64-Band Spectrum Analyzer
        SpectrumAnalyzer(
            bands = telemetry.spectrumBands,
            peakDb = maxOf(telemetry.peakDbL, telemetry.peakDbR),
            modifier = Modifier.fillMaxWidth()
        )

        // Real-Time Laboratory Oscilloscope
        OscilloscopeCanvas(
            waveform = telemetry.waveformSamples,
            modifier = Modifier.fillMaxWidth()
        )

        // Laboratory Telemetry & Stream Diagnostics
        RackModuleCard(
            title = "Hardware Stream Telemetry",
            subtitle = "DAC AUDIO SUBSYSTEM // SAMPLE ACCURATE DIAGNOSTICS",
            isEnabled = true,
            onToggleEnabled = {},
            accentColor = NeonGreen,
            testTag = "telemetry_rack_card"
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "SAMPLE RATE", value = "${telemetry.sampleRateHz} Hz")
                    TelemetryItem(label = "BIT DEPTH", value = "${telemetry.bitDepth} BIT PCM")
                    TelemetryItem(label = "BUFFER SIZE", value = "${telemetry.bufferSize} SMP")
                }
                Divider(color = StudioRackBorder, thickness = 0.5.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TelemetryItem(label = "OUTPUT LATENCY", value = "%.1f ms".format(telemetry.latencyMs))
                    TelemetryItem(label = "ESTIMATED THD", value = "%.2f %%".format(telemetry.estimatedThdPercent))
                    TelemetryItem(label = "PHASE CORRELATION", value = "%+.2f".format(telemetry.phaseCorrelation))
                }
            }
        }

        // Studio Preset Library Module
        RackModuleCard(
            title = "Laboratory Preset Library",
            subtitle = "CALIBRATION CURVES & SOUND DESIGN PROFILES",
            isEnabled = true,
            onToggleEnabled = {},
            accentColor = NeonAmber,
            testTag = "presets_rack_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ACTIVE: ${currentPreset?.name ?: "Custom"}",
                    color = NeonAmber,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { showSaveDialog = true },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonAmber.copy(alpha = 0.2f),
                        contentColor = NeonAmber
                    ),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("save_preset_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Save Profile", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "SAVE PROFILE", fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                presets.forEach { preset ->
                    val isSelected = currentPreset?.id == preset.id
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) StudioRackPanel else Color(0xFF0C1322))
                            .border(1.dp, if (isSelected) NeonAmber else StudioRackBorder, RoundedCornerShape(6.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = if (isSelected) NeonAmber else TextMuted,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = preset.name,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "[${preset.category}]",
                                    color = TextMuted,
                                    fontSize = 9.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = preset.description,
                                color = TextSecondary,
                                fontSize = 10.sp,
                                maxLines = 2
                            )
                        }

                        Button(
                            onClick = { onSelectPreset(preset) },
                            shape = RoundedCornerShape(6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSelected) NeonAmber else Color(0xFF1E293B),
                                contentColor = if (isSelected) StudioDarkBg else TextPrimary
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("apply_preset_${preset.id}")
                        ) {
                            Text(
                                text = if (isSelected) "ENGAGED" else "LOAD",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }

    // Save Preset Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text(
                    text = "SAVE LAB PROFILE",
                    color = NeonAmber,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Store all current Equalizer, DSP, Reverb, Spatial, and Harmonic parameters into a new laboratory memory bank.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    OutlinedTextField(
                        value = presetNameInput,
                        onValueChange = { presetNameInput = it },
                        label = { Text("Profile Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_name_input")
                    )
                    OutlinedTextField(
                        value = presetDescInput,
                        onValueChange = { presetDescInput = it },
                        label = { Text("Description (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("preset_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (presetNameInput.isNotBlank()) {
                            onSaveCustomPreset(presetNameInput.trim(), presetDescInput.trim())
                            showSaveDialog = false
                            presetNameInput = ""
                            presetDescInput = ""
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonAmber, contentColor = StudioDarkBg),
                    modifier = Modifier.testTag("save_dialog_confirm_btn")
                ) {
                    Text("SAVE", fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("CANCEL", color = TextMuted, fontFamily = FontFamily.Monospace)
                }
            },
            containerColor = StudioCard
        )
    }
}

@Composable
private fun TelemetryItem(label: String, value: String) {
    Column {
        Text(text = label, color = TextMuted, fontSize = 8.sp, fontFamily = FontFamily.Monospace)
        Text(text = value, color = NeonGreen, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
    }
}
