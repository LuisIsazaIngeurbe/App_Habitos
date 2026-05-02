package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.domain.utils.DayColor
import java.time.LocalDate
import java.time.YearMonth

private val DAY_HEADERS = listOf("L", "M", "X", "J", "V", "S", "D")

@Composable
fun MonthCalendarGrid(
    year: Int,
    month: Int,
    dayColors: Map<Int, DayColor>,
    selectedDay: Int?,
    onDayClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val yearMonth = YearMonth.of(year, month)
    val today = LocalDate.now()
    // Number of empty cells before the 1st day (Monday = 0, Sunday = 6)
    val firstDayOffset = (yearMonth.atDay(1).dayOfWeek.value - 1).coerceIn(0, 6)
    val daysInMonth = yearMonth.lengthOfMonth()

    Column(modifier = modifier) {
        // Day-of-week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            DAY_HEADERS.forEach { header ->
                Text(
                    text = header,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        val totalCells = firstDayOffset + daysInMonth
        val rowCount = (totalCells + 6) / 7

        repeat(rowCount) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    val cellIndex = row * 7 + col
                    val day = cellIndex - firstDayOffset + 1

                    if (day in 1..daysInMonth) {
                        val date = yearMonth.atDay(day)
                        val color = dayColors[day] ?: DayColor.GRAY
                        val isSelected = day == selectedDay
                        val isToday = date == today

                        DayCalendarCell(
                            day = day,
                            color = color,
                            isSelected = isSelected,
                            isToday = isToday,
                            onClick = { onDayClick(day) },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        EmptyDayCell(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
