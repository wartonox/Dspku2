package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DynamicsConfig
import com.example.ui.components.*
import com.example.ui.theme.*

@Composable
fun DynamicsTab(
    config: DynamicsConfig,
    gainReductionDb: Float,
    onThresholdChange: (Float) -> Unit,
    onRatioChange: (Float) -> Unit,
    onAttackChange: (Float) -> Unit,
    onReleaseChange: (Float) -> Unit,
    onMakeupChange: (Float) -> Unit,
    onLimiterCeilingChange: (Float) -> Unit,
    onToggleCompressor: () -> Unit,
    onToggleLimiter: () -> Unit,
    onReset: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(12.dp)
            .testTag("dynamics_tab"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Multi-Band Studio Compressor Module
        RackModuleCard(
            title = "Studio Dynamic Compressor",
            subtitle = "VCA ENVELOPE DETECTOR // GAIN REDUCTION METER",
            isEnabled = config.compressorEnabled,
            onToggleEnabled = onToggleCompressor,
            onReset = onReset,
            accentColor = NeonGreen,
            testTag = "compressor_rack_card"
        ) {
            // Live Gain Reduction Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAIN REDUCTION: -%.1f dB".format(gainReductionDb),
                    color = if (gainReductionDb > 0.1f) NeonAmber else TextMuted,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "RMS DETECTION",
                    color = TextSecondary,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Primary Controls: Threshold, Ratio, Makeup Gain
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = config.thresholdDb,
                    onValueChange = onThresholdChange,
                    range = -60f..0f,
                    defaultValue = -18f,
                    label = "THRESHOLD",
                    valueFormatter = { "%.1f dB".format(it) },
                    accentColor = NeonGreen,
                    size = 66.dp,
                    testTag = "knob_comp_threshold"
                )

                RotaryKnob(
                    value = config.ratio,
                    onValueChange = onRatioChange,
                    range = 1f..20f,
                    defaultValue = 3.5f,
                    label = "RATIO",
                    valueFormatter = { "%.1f:1".format(it) },
                    accentColor = NeonGreen,
                    size = 66.dp,
                    testTag = "knob_comp_ratio"
                )

                RotaryKnob(
                    value = config.makeupGainDb,
                    onValueChange = onMakeupChange,
                    range = 0f..24f,
                    defaultValue = 3f,
                    label = "MAKEUP GAIN",
                    valueFormatter = { "+%.1f dB".format(it) },
                    accentColor = NeonGreen,
                    size = 66.dp,
                    testTag = "knob_comp_makeup"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Envelope Time Constants: Attack & Release
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = config.attackMs,
                    onValueChange = onAttackChange,
                    range = 1f..200f,
                    defaultValue = 15f,
                    label = "ATTACK TIME",
                    valueFormatter = { "%.0f ms".format(it) },
                    accentColor = NeonGreen,
                    size = 60.dp,
                    testTag = "knob_comp_attack"
                )

                RotaryKnob(
                    value = config.releaseMs,
                    onValueChange = onReleaseChange,
                    range = 10f..1000f,
                    defaultValue = 120f,
                    label = "RELEASE TIME",
                    valueFormatter = { "%.0f ms".format(it) },
                    accentColor = NeonGreen,
                    size = 60.dp,
                    testTag = "knob_comp_release"
                )
            }
        }

        // Brickwall Studio Limiter Module
        RackModuleCard(
            title = "Peak Brickwall Limiter",
            subtitle = "LOOKAHEAD PEAK LIMITER // 0dBFS ANTI-CLIPPING",
            isEnabled = config.limiterEnabled,
            onToggleEnabled = onToggleLimiter,
            accentColor = NeonRed,
            testTag = "limiter_rack_card"
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RotaryKnob(
                    value = config.limiterCeilingDb,
                    onValueChange = onLimiterCeilingChange,
                    range = -12f..0f,
                    defaultValue = -0.5f,
                    label = "CEILING PEAK",
                    valueFormatter = { "%.1f dBFS".format(it) },
                    accentColor = NeonRed,
                    size = 68.dp,
                    testTag = "knob_limiter_ceiling"
                )

                Column(
                    modifier = Modifier.width(180.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "SAFETY LOOKAHEAD: 5.0 ms",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "CHARACTER: TRANSPARENT HARD",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "INTER-SAMPLE PEAK: GUARDED",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}
