package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Dual Stereo Studio VU Meter with Segmented LED Bars and Clip Indicator
 */
@Composable
fun VuMeter(
    peakL: Float,
    peakR: Float,
    isClipping: Boolean,
    gainReductionDb: Float = 0f,
    modifier: Modifier = Modifier,
    testTag: String = "vu_meter"
) {
    Column(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B12))
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        // Meter Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "MASTER STEREO VU (dBFS)",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )

            // Clip LED
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "CLIP",
                    color = if (isClipping) NeonRed else TextMuted,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isClipping) NeonRed else LedOff)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Left Channel Bar
        ChannelBar(channelName = "L", peakDb = peakL)

        Spacer(modifier = Modifier.height(4.dp))

        // Right Channel Bar
        ChannelBar(channelName = "R", peakDb = peakR)

        // Gain Reduction (GR) Bar if compression is active
        if (gainReductionDb > 0.1f) {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GR",
                    color = NeonAmber,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.width(16.dp)
                )
                GainReductionBar(grDb = gainReductionDb, modifier = Modifier.weight(1f))
                Text(
                    text = "-%.1f dB".format(gainReductionDb),
                    color = NeonAmber,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(50.dp)
                )
            }
        }
    }
}

@Composable
private fun ChannelBar(
    channelName: String,
    peakDb: Float,
    modifier: Modifier = Modifier
) {
    val norm = ((peakDb - (-60f)) / 60f).coerceIn(0f, 1f)

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = channelName,
            color = TextSecondary,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(16.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF0F172A))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val totalWidth = size.width
                val activeWidth = norm * totalWidth
                val numSegments = 30
                val segWidth = totalWidth / numSegments
                val gap = 1.dp.toPx()

                for (s in 0 until numSegments) {
                    val segNorm = (s.toFloat() + 0.5f) / numSegments
                    val x = s * segWidth

                    val segColor = when {
                        segNorm > 0.92f -> MeterRed
                        segNorm > 0.70f -> MeterYellow
                        else -> MeterGreen
                    }

                    val isLit = (x + segWidth) <= activeWidth
                    val drawColor = if (isLit) segColor else segColor.copy(alpha = 0.15f)

                    drawRoundRect(
                        color = drawColor,
                        topLeft = Offset(x, 0f),
                        size = Size(segWidth - gap, size.height),
                        cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "%+.1f dB".format(peakDb),
            color = if (peakDb > -1f) NeonRed else if (peakDb > -12f) NeonGreen else TextMuted,
            fontSize = 9.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(50.dp)
        )
    }
}

@Composable
private fun GainReductionBar(
    grDb: Float,
    modifier: Modifier = Modifier
) {
    val norm = (grDb / 24f).coerceIn(0f, 1f)
    Box(
        modifier = modifier
            .height(8.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color(0xFF0F172A))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val totalWidth = size.width
            val grWidth = norm * totalWidth
            // GR draws from right to left
            drawRoundRect(
                color = NeonAmber,
                topLeft = Offset(totalWidth - grWidth, 0f),
                size = Size(grWidth, size.height),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
        }
    }
}
