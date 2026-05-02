package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private val DAY_LABELS = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun WeeklyBarChart(
    data: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    val safeData = if (data.size == 7) data else List(7) { 0f }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val animatedValues = safeData.map { target ->
        val value by animateFloatAsState(
            targetValue = if (started) target else 0f,
            animationSpec = tween(durationMillis = 600),
            label = "bar"
        )
        value
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            animatedValues.forEachIndexed { index, value ->
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    val fraction = (value / 100f).coerceIn(0f, 1f)
                    val maxBarHeight = 120.dp
                    val barHeight = (maxBarHeight.value * fraction).coerceAtLeast(4f)

                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(barHeight.dp)
                    ) {
                        val cornerRadius = CornerRadius(8f, 8f)
                        drawRoundRect(
                            color = barColor.copy(alpha = if (fraction > 0) 1f else 0.2f),
                            topLeft = Offset.Zero,
                            size = Size(size.width, size.height),
                            cornerRadius = cornerRadius
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            DAY_LABELS.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
