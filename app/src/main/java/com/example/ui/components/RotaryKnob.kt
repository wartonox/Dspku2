package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Tactile Studio Rotary Dial with angular drag and glowing arc indicator
 */
@Composable
fun RotaryKnob(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    range: ClosedFloatingPointRange<Float> = 0f..1f,
    defaultValue: Float = range.start + (range.endInclusive - range.start) / 2f,
    label: String,
    valueFormatter: (Float) -> String = { String.format("%.1f", it) },
    accentColor: Color = NeonCyan,
    size: Dp = 68.dp,
    testTag: String = "rotary_knob"
) {
    val normalizedValue = ((value - range.start) / (range.endInclusive - range.start)).coerceIn(0f, 1f)
    val startAngle = 135f
    val sweepMaxAngle = 270f
    val currentSweep = normalizedValue * sweepMaxAngle

    Column(
        modifier = modifier
            .testTag(testTag)
            .width(size + 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .pointerInput(range) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        // Vertical drag: upward increases value
                        val sensitivity = 0.005f
                        val deltaNormalized = -dragAmount.y * sensitivity
                        val newNorm = (normalizedValue + deltaNormalized).coerceIn(0f, 1f)
                        val newValue = range.start + newNorm * (range.endInclusive - range.start)
                        onValueChange(newValue)
                    }
                }
                .pointerInput(defaultValue) {
                    detectTapGestures(
                        onDoubleTap = {
                            onValueChange(defaultValue)
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
                val strokeWidth = 5.dp.toPx()
                val radius = (size.toPx() - strokeWidth) / 2f - 2.dp.toPx()

                // Background track arc
                drawArc(
                    color = StudioRackBorder,
                    startAngle = startAngle,
                    sweepAngle = sweepMaxAngle,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Active glowing value arc
                if (currentSweep > 1f) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(accentColor.copy(alpha = 0.6f), accentColor),
                            center = center
                        ),
                        startAngle = startAngle,
                        sweepAngle = currentSweep,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                // Inner Knob Body (aluminum cap)
                val innerRadius = radius - 7.dp.toPx()
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(KnobRim, KnobFace),
                        center = center,
                        radius = innerRadius
                    ),
                    radius = innerRadius,
                    center = center
                )

                // Outer bezel ring
                drawCircle(
                    color = StudioRackBorder,
                    radius = innerRadius,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )

                // Rotary Pointer Tick
                val indicatorAngleRad = Math.toRadians((startAngle + currentSweep).toDouble())
                val tickStart = Offset(
                    center.x + (innerRadius * 0.45f) * cos(indicatorAngleRad).toFloat(),
                    center.y + (innerRadius * 0.45f) * sin(indicatorAngleRad).toFloat()
                )
                val tickEnd = Offset(
                    center.x + (innerRadius * 0.88f) * cos(indicatorAngleRad).toFloat(),
                    center.y + (innerRadius * 0.88f) * sin(indicatorAngleRad).toFloat()
                )

                drawLine(
                    color = accentColor,
                    start = tickStart,
                    end = tickEnd,
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Center Value readout
        Text(
            text = valueFormatter(value),
            color = accentColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            maxLines = 1
        )

        // Parameter Name
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}
