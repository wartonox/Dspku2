package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.model.EqualizerConfig
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun EqualizerTab(
    config: EqualizerConfig,
    onBandGainChange: (Int, Float) -> Unit,
    onPreGainChange: (Float) -> Unit,
    onHpfChange: (Float) -> Unit,
    onLpfChange: (Float) -> Unit,
    onToggleEnabled: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val fadersScrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("equalizer_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Main Equalizer Rack Module
        RackModuleCard(
            title = "Parametric Equalizer",
            subtitle = "10-BAND ISO GRAPHIC & RTA CURVE // MODEL EQ-10X",
            isEnabled = config.enabled,
            onToggleEnabled = onToggleEnabled,
            onReset = onReset,
            accentColor = NeonCyan,
            testTag = "eq_rack_card"
        ) {
            // Interactive Frequency Response Curve
            ParametricEqGraph(
                config = config,
                onBandGainChange = onBandGainChange,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Pre-Amp & Filter Cutoffs Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // High-Pass Filter (Low Cut)
                RotaryKnob(
                    value = config.hpfCutoffHz,
                    onValueChange = onHpfChange,
                    range = 20f..400f,
                    defaultValue = 20f,
                    label = "HPF LOW CUT",
                    valueFormatter = { "%.0f Hz".format(it) },
                    accentColor = NeonCyan,
                    size = 62.dp,
                    testTag = "knob_hpf"
                )

                // Master Pre-Amp Gain
                RotaryKnob(
                    value = config.preGainDb,
                    onValueChange = onPreGainChange,
                    range = -12f..12f,
                    defaultValue = 0f,
                    label = "PRE-AMP GAIN",
                    valueFormatter = { "%+.1f dB".format(it) },
                    accentColor = NeonCyan,
                    size = 68.dp,
                    testTag = "knob_pre_gain"
                )

                // Low-Pass Filter (High Cut)
                RotaryKnob(
                    value = config.lpfCutoffHz,
                    onValueChange = onLpfChange,
                    range = 4000f..20000f,
                    defaultValue = 20000f,
                    label = "LPF HIGH CUT",
                    valueFormatter = {
                        if (it >= 1000f) "%.1fk Hz".format(it / 1000f) else "%.0f Hz".format(it)
                    },
                    accentColor = NeonCyan,
                    size = 62.dp,
                    testTag = "knob_lpf"
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 10-Band ISO Console Faders Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ISO FREQUENCY BANDS (±15 dB)",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "DOUBLE TAP TO ZERO",
                    color = TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Horizontal Scrolling Fader Rack
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(fadersScrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                config.bands.forEachIndexed { index, band ->
                    StudioFader(
                        gainDb = band.gainDb,
                        onGainChange = { newGain -> onBandGainChange(index, newGain) },
                        frequencyLabel = band.label,
                        accentColor = NeonCyan,
                        testTag = "fader_band_$index"
                    )
                }
            }
        }

        // Quick Equalizer Profiles
        RackModuleCard(
            title = "Acoustic Target Curves",
            subtitle = "INSTANT LABORATORY EQ ALIGNMENT",
            isEnabled = config.enabled,
            onToggleEnabled = onToggleEnabled,
            accentColor = NeonCyanDim,
            testTag = "eq_targets_card"
        ) {
            val targets = listOf(
                "FLAT" to listOf(0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f),
                "BASS PUNCH" to listOf(6f, 5f, 3.5f, 1f, -0.5f, 0f, 1f, 2f, 3f, 4f),
                "HARMAN TARGET" to listOf(5f, 4f, 2f, 0f, -0.5f, 1f, 3f, 2.5f, 1.5f, 1f),
                "VOCAL PRESENCE" to listOf(-2f, -1f, 0f, 1f, -0.5f, 2f, 3.5f, 2.5f, 1f, 1.5f),
                "AIR & BRIGHT" to listOf(0f, 0f, 0f, 0f, 0.5f, 1f, 2f, 3.5f, 5f, 6.5f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                targets.forEach { (name, curve) ->
                    OutlinedButton(
                        onClick = {
                            curve.forEachIndexed { i, g -> onBandGainChange(i, g) }
                        },
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = NeonCyan
                        ),
                        modifier = Modifier.testTag("target_btn_$name")
                    ) {
                        Text(
                            text = name,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
