package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Precision Studio Console Fader for Equalizer Bands
 */
@Composable
fun StudioFader(
    gainDb: Float,
    onGainChange: (Float) -> Unit,
    frequencyLabel: String,
    modifier: Modifier = Modifier,
    minGain: Float = -15f,
    maxGain: Float = 15f,
    accentColor: Color = NeonCyan,
    testTag: String = "studio_fader"
) {
    val normGain = ((gainDb - minGain) / (maxGain - minGain)).coerceIn(0f, 1f)

    Column(
        modifier = modifier
            .testTag(testTag)
            .width(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Gain Readout
        val formattedGain = if (gainDb > 0.05f) "+%.1f".format(gainDb) else if (gainDb < -0.05f) "%.1f".format(gainDb) else "0.0"
        Text(
            text = formattedGain,
            color = if (gainDb != 0f) accentColor else TextMuted,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Fader Track & Thumb Canvas
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(160.dp)
                .pointerInput(minGain, maxGain) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Dragging upwards increases gain
                        val totalHeight = size.height.toFloat()
                        val deltaGain = -dragAmount.y / totalHeight * (maxGain - minGain)
                        val newGain = (gainDb + deltaGain).coerceIn(minGain, maxGain)
                        // Snap near 0 dB
                        val snappedGain = if (newGain in -0.3f..0.3f) 0f else newGain
                        onGainChange(snappedGain)
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { onGainChange(0f) }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val trackWidth = 6.dp.toPx()
                val centerX = size.width / 2f
                val trackHeight = size.height - 20.dp.toPx()
                val trackTop = 10.dp.toPx()
                val trackBottom = trackTop + trackHeight

                // Background track slot
                drawRoundRect(
                    color = Color(0xFF0B101B),
                    topLeft = Offset(centerX - trackWidth / 2f, trackTop),
                    size = Size(trackWidth, trackHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )

                // Zero-dB center line tick
                val zeroY = trackTop + trackHeight * 0.5f
                drawLine(
                    color = TextMuted.copy(alpha = 0.5f),
                    start = Offset(4.dp.toPx(), zeroY),
                    end = Offset(size.width - 4.dp.toPx(), zeroY),
                    strokeWidth = 1.5.dp.toPx()
                )

                // Scale ticks along track
                val steps = 6
                for (s in 0..steps) {
                    val y = trackTop + trackHeight * (s.toFloat() / steps)
                    val tickLen = if (s == 3) 14.dp.toPx() else 8.dp.toPx()
                    drawLine(
                        color = if (s == 3) accentColor.copy(alpha = 0.7f) else StudioRackBorder,
                        start = Offset(centerX - tickLen / 2f, y),
                        end = Offset(centerX + tickLen / 2f, y),
                        strokeWidth = 1.dp.toPx()
                    )
                }

                // Active Gain Level Glow line from 0dB to thumb
                val thumbY = trackTop + trackHeight * (1f - normGain)
                if (normGain != 0.5f) {
                    val topY = min(zeroY, thumbY)
                    val botY = max(zeroY, thumbY)
                    drawRoundRect(
                        color = accentColor.copy(alpha = 0.85f),
                        topLeft = Offset(centerX - trackWidth / 2f + 1.dp.toPx(), topY),
                        size = Size(trackWidth - 2.dp.toPx(), botY - topY),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }

                // Fader Cap / Thumb
                val thumbWidth = 32.dp.toPx()
                val thumbHeight = 18.dp.toPx()
                val thumbLeft = centerX - thumbWidth / 2f
                val thumbTop = thumbY - thumbHeight / 2f

                // Thumb shadow & metallic body
                drawRoundRect(
                    color = KnobRim,
                    topLeft = Offset(thumbLeft, thumbTop),
                    size = Size(thumbWidth, thumbHeight),
                    cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx())
                )
                drawRoundRect(
                    color = KnobFace,
                    topLeft = Offset(thumbLeft + 1.dp.toPx(), thumbTop + 1.dp.toPx()),
                    size = Size(thumbWidth - 2.dp.toPx(), thumbHeight - 2.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )

                // Illuminated center indicator line on thumb
                drawLine(
                    color = accentColor,
                    start = Offset(thumbLeft + 3.dp.toPx(), thumbY),
                    end = Offset(thumbLeft + thumbWidth - 3.dp.toPx(), thumbY),
                    strokeWidth = 2.5.dp.toPx()
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Frequency Label
        Text(
            text = frequencyLabel,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
