package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonPurple
import kotlin.math.sin

@Composable
fun VoiceWaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 28
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val amplitudeMultiplier by animateFloatAsState(
        targetValue = if (isActive) 1f else 0.15f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "amplitude"
    )

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(70.dp)
    ) {
        val width = size.width
        val height = size.height
        val centerY = height / 2
        val barSpacing = width / (barCount + 1)

        val gradient = Brush.linearGradient(
            colors = listOf(CyberCyan, ElectricBlue, NeonPurple),
            start = Offset(0f, centerY),
            end = Offset(width, centerY)
        )

        for (i in 0 until barCount) {
            val x = (i + 1) * barSpacing
            val normalizedX = i.toFloat() / barCount
            val wave1 = sin(normalizedX * 4 * Math.PI + phase).toFloat()
            val wave2 = sin(normalizedX * 8 * Math.PI - phase * 1.5f).toFloat() * 0.5f

            val envelope = sin(normalizedX * Math.PI).toFloat() // window function to taper edges
            val rawHeight = (wave1 + wave2) * (height * 0.45f) * envelope * amplitudeMultiplier
            val barHeight = kotlin.math.max(rawHeight.toDouble().let { kotlin.math.abs(it) }.toFloat(), 4f)

            drawLine(
                brush = gradient,
                start = Offset(x, centerY - barHeight),
                end = Offset(x, centerY + barHeight),
                strokeWidth = 3.5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}
