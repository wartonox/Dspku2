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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * Real-time Laboratory Oscilloscope Canvas with CRT reticle and glow
 */
@Composable
fun OscilloscopeCanvas(
    waveform: FloatArray,
    modifier: Modifier = Modifier,
    testTag: String = "oscilloscope_canvas"
) {
    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF040A0F))
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 4.dp)) {
            val w = size.width
            val h = size.height

            // 1. Draw Oscilloscope Reticle Grid
            val cols = 8
            val rows = 6
            for (c in 1 until cols) {
                val x = w * (c.toFloat() / cols)
                drawLine(
                    color = Color(0xFF0F262E),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
            for (r in 1 until rows) {
                val y = h * (r.toFloat() / rows)
                drawLine(
                    color = Color(0xFF0F262E),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            // Center crosshairs
            drawLine(
                color = Color(0xFF1E3A45),
                start = Offset(w / 2f, 0f),
                end = Offset(w / 2f, h),
                strokeWidth = 1.2.dp.toPx()
            )
            drawLine(
                color = Color(0xFF1E3A45),
                start = Offset(0f, h / 2f),
                end = Offset(w, h / 2f),
                strokeWidth = 1.2.dp.toPx()
            )

            // 2. Draw Real-time Waveform Trace
            if (waveform.isNotEmpty()) {
                val path = Path()
                val stepX = w / (waveform.size - 1)
                val centerY = h / 2f
                val amplitudeScale = (h / 2.2f)

                for (i in waveform.indices) {
                    val x = i * stepX
                    val y = centerY - (waveform[i] * amplitudeScale)
                    if (i == 0) {
                        path.moveTo(x, y)
                    } else {
                        path.lineTo(x, y)
                    }
                }

                // Outer Glow Stroke
                drawPath(
                    path = path,
                    color = NeonGreen.copy(alpha = 0.35f),
                    style = Stroke(
                        width = 4.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Core Phosphor Beam
                drawPath(
                    path = path,
                    color = NeonGreen,
                    style = Stroke(
                        width = 1.8.dp.toPx(),
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
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
                text = "OSCILLOSCOPE CH1 (TIME: 2ms/DIV)",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "TRIG: AUTO",
                color = NeonGreen,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
