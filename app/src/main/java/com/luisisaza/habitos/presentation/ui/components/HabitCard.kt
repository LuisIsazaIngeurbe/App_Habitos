package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitLog
import com.luisisaza.habitos.domain.model.HabitType

@Composable
fun HabitCard(
    habit: Habit,
    todayLog: HabitLog?,
    onComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type indicator (uses fixed semantic colors, immutable to palette)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (habit.type == HabitType.GOOD)
                    com.luisisaza.habitos.presentation.ui.theme.HabitGoodContainer
                else com.luisisaza.habitos.presentation.ui.theme.HabitBadContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = if (habit.type == HabitType.GOOD) Icons.Default.CheckCircle
                        else Icons.Default.Block,
                        contentDescription = null,
                        tint = if (habit.type == HabitType.GOOD)
                            com.luisisaza.habitos.presentation.ui.theme.HabitGoodIcon
                        else com.luisisaza.habitos.presentation.ui.theme.HabitBadIcon
                    )
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (habit.type == HabitType.BAD) {
                    Text(
                        text = "🔥 ${habit.streakCount} ${stringResource(R.string.daily_streak)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else if (habit.scheduleStartTime != null) {
                    Text(
                        text = "${habit.scheduleStartTime} – ${habit.scheduleEndTime ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.habits_edit),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.habits_delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun DailyHabitCard(
    habit: Habit,
    completed: Boolean?,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = habit.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                val subtitle = when {
                    habit.type == HabitType.BAD ->
                        "🔥 ${habit.streakCount} ${stringResource(R.string.daily_streak)}"
                    habit.scheduleStartTime != null ->
                        "${habit.scheduleStartTime}  •  ${habit.duration?.displayLabel ?: ""}"
                    habit.goal != null -> habit.goal
                    else -> null
                }
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Checkbox(
                checked = completed == true,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(
                    checkedColor = MaterialTheme.colorScheme.secondary
                )
            )
        }
    }
}
