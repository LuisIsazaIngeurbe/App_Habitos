package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.domain.utils.DayColor

@Composable
fun DayCalendarCell(
    day: Int,
    color: DayColor,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedBg by animateColorAsState(
        targetValue = color.color,
        animationSpec = tween(durationMillis = 300),
        label = "dayBg"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(animatedBg)
            .then(
                if (isSelected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                else if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                else Modifier
            )
            .clickable(onClick = onClick)
            .semantics { contentDescription = "Día $day" }
    ) {
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = resolveDayTextColor(color)
        )
    }
}

@Composable
fun EmptyDayCell(modifier: Modifier = Modifier) {
    Box(modifier = modifier.aspectRatio(1f))
}

private fun resolveDayTextColor(dayColor: DayColor): Color =
    when (dayColor) {
        DayColor.GREEN, DayColor.RED -> Color.White
        DayColor.YELLOW -> Color(0xFF5D4037)
        DayColor.GRAY, DayColor.FUTURE -> Color(0xFF9E9E9E)
    }
