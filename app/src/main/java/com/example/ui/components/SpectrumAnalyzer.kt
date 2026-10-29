package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Real-time 64-Band FFT Spectrum Analyzer with Peak Hold
 */
@Composable
fun SpectrumAnalyzer(
    bands: FloatArray,
    peakDb: Float,
    modifier: Modifier = Modifier,
    testTag: String = "spectrum_analyzer"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B12))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 6.dp, vertical = 6.dp)) {
            val w = size.width
            val h = size.height
            val numBands = bands.size
            if (numBands == 0) return@Canvas

            val barSpacing = 1.5.dp.toPx()
            val totalSpacing = barSpacing * (numBands - 1)
            val barWidth = ((w - totalSpacing) / numBands).coerceAtLeast(1f)

            // Draw dB horizontal grid lines
            for (step in 1..4) {
                val y = h * (step / 5f)
                drawLine(
                    color = StudioRackBorder.copy(alpha = 0.35f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            // Draw spectrum bars
            for (i in 0 until numBands) {
                val energy = bands[i].coerceIn(0.01f, 1f)
                val barHeight = energy * h
                val x = i * (barWidth + barSpacing)
                val y = h - barHeight

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonRed, NeonAmber, NeonCyan),
                        startY = 0f,
                        endY = h
                    ),
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
        }

        // Header info
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RTA 64-BAND FFT SPECTRUM",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "PEAK: %.1f dBFS".format(peakDb),
                color = if (peakDb > -1f) NeonRed else NeonCyan,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        // Frequency axis labels at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val freqLabels = listOf("31", "63", "125", "250", "500", "1k", "2k", "4k", "8k", "16k")
            for (lbl in freqLabels) {
                Text(
                    text = lbl,
                    color = TextMuted,
                    fontSize = 8.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
    }
}
