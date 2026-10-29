package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.EqualizerConfig
import com.example.ui.theme.*
import kotlin.math.*

/**
 * Interactive Studio Parametric Curve Canvas (20 Hz - 20 kHz, -15 dB to +15 dB)
 */
@Composable
fun ParametricEqGraph(
    config: EqualizerConfig,
    onBandGainChange: (bandIndex: Int, gainDb: Float) -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = "parametric_eq_graph"
) {
    val bands = config.bands
    val minFreq = 20.0
    val maxFreq = 20000.0
    val logMin = log10(minFreq)
    val logMax = log10(maxFreq)

    fun freqToX(freq: Double, width: Float): Float {
        val norm = (log10(freq.coerceIn(minFreq, maxFreq)) - logMin) / (logMax - logMin)
        return (norm * width).toFloat()
    }

    fun gainToY(gain: Float, height: Float): Float {
        val norm = (gain.coerceIn(-15f, 15f) - (-15f)) / 30f
        return height * (1f - norm)
    }

    fun yToGain(y: Float, height: Float): Float {
        val norm = 1f - (y / height).coerceIn(0f, 1f)
        return -15f + norm * 30f
    }

    Box(
        modifier = modifier
            .testTag(testTag)
            .fillMaxWidth()
            .height(170.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF070B12))
            .pointerInput(bands) {
                detectDragGestures { change, _ ->
                    val pos = change.position
                    val width = size.width.toFloat()
                    val height = size.height.toFloat()

                    // Find nearest band in X
                    var closestBand = 0
                    var minDist = Float.MAX_VALUE
                    for (i in bands.indices) {
                        val bandX = freqToX(bands[i].centerFreqHz.toDouble(), width)
                        val dist = abs(bandX - pos.x)
                        if (dist < minDist) {
                            minDist = dist
                            closestBand = i
                        }
                    }

                    if (minDist < 60f) {
                        change.consume()
                        val newGain = yToGain(pos.y, height)
                        onBandGainChange(closestBand, newGain.coerceIn(-15f, 15f))
                    }
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 4.dp, vertical = 6.dp)) {
            val w = size.width
            val h = size.height

            // 1. Draw dB horizontal grid lines (+12, +6, 0, -6, -12 dB)
            val dbGrid = listOf(12f, 6f, 0f, -6f, -12f)
            for (db in dbGrid) {
                val y = gainToY(db, h)
                val isZero = db == 0f
                drawLine(
                    color = if (isZero) TextMuted.copy(alpha = 0.6f) else StudioRackBorder.copy(alpha = 0.5f),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (isZero) 1.5.dp.toPx() else 0.8.dp.toPx()
                )
            }

            // 2. Draw logarithmic frequency vertical lines (50, 100, 250, 500, 1k, 2k, 4k, 8k, 16k)
            val freqGrid = listOf(50, 100, 250, 500, 1000, 2000, 4000, 8000, 16000)
            for (f in freqGrid) {
                val x = freqToX(f.toDouble(), w)
                drawLine(
                    color = StudioRackBorder.copy(alpha = 0.5f),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 0.8.dp.toPx()
                )
            }

            // 3. Compute Composite Response Curve
            val path = Path()
            val fillPath = Path()
            val steps = 120

            var firstPoint = true
            for (s in 0..steps) {
                val normX = s.toFloat() / steps
                val freq = 10.0.pow(logMin + normX * (logMax - logMin))
                val x = normX * w

                // Calculate composite gain from 10 peaking bands + HPF + LPF
                var compositeGain = 0f

                if (config.enabled) {
                    for (b in bands) {
                        val f0 = b.centerFreqHz.toDouble()
                        val q = 1.4
                        val fRatio = freq / f0
                        // Peaking filter approximation in dB
                        val bandGain = b.gainDb
                        if (bandGain != 0f) {
                            val response = 1.0 / (1.0 + q * (fRatio - 1.0 / fRatio).pow(2.0))
                            compositeGain += (bandGain * response).toFloat()
                        }
                    }

                    // HPF attenuation below cutoff (-12dB/oct)
                    if (freq < config.hpfCutoffHz) {
                        val octavesBelow = log2(config.hpfCutoffHz.toDouble() / freq)
                        compositeGain -= (octavesBelow * 12.0).toFloat()
                    }

                    // LPF attenuation above cutoff (-12dB/oct)
                    if (freq > config.lpfCutoffHz) {
                        val octavesAbove = log2(freq / config.lpfCutoffHz.toDouble())
                        compositeGain -= (octavesAbove * 12.0).toFloat()
                    }
                }

                val y = gainToY(compositeGain + config.preGainDb, h)

                if (firstPoint) {
                    path.moveTo(x, y)
                    fillPath.moveTo(x, gainToY(0f, h))
                    fillPath.lineTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            // Close fill path
            fillPath.lineTo(w, gainToY(0f, h))
            fillPath.close()

            // Draw translucent neon glow fill
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        NeonCyan.copy(alpha = 0.25f),
                        NeonCyanDim.copy(alpha = 0.05f)
                    )
                )
            )

            // Draw response curve stroke
            drawPath(
                path = path,
                color = if (config.enabled) NeonCyan else TextMuted,
                style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
            )

            // 4. Draw Interactive Control Nodes for each of the 10 bands
            for (i in bands.indices) {
                val b = bands[i]
                val bx = freqToX(b.centerFreqHz.toDouble(), w)
                val by = gainToY(if (config.enabled) b.gainDb + config.preGainDb else 0f, h)

                // Node halo
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.25f),
                    radius = 9.dp.toPx(),
                    center = Offset(bx, by)
                )
                // Node body
                drawCircle(
                    color = if (b.gainDb != 0f) NeonCyan else TextSecondary,
                    radius = 4.5.dp.toPx(),
                    center = Offset(bx, by)
                )
                // Center pin
                drawCircle(
                    color = StudioDarkBg,
                    radius = 2.dp.toPx(),
                    center = Offset(bx, by)
                )
            }
        }

        // Overlay corner info
        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "RTA RESPONSE CURVE (20Hz - 20kHz)",
                color = TextSecondary,
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
