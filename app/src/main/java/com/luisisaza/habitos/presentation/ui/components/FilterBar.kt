package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import java.time.LocalDate

@Suppress("OPT_IN_IS_NOT_ENABLED")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBar(
    selectedYear: Int,
    onYearChange: (Int) -> Unit,
    selectedMonth: Int,
    onMonthChange: (Int) -> Unit,
    selectedType: HabitType?,
    onTypeChange: (HabitType?) -> Unit,
    selectedHabitIds: List<Long>,
    onHabitsChange: (List<Long>) -> Unit,
    availableHabits: List<Habit>,
    modifier: Modifier = Modifier
) {
    val currentYear = LocalDate.now().year
    val years = (currentYear - 3..currentYear).toList().reversed()
    val months = (1..12).toList()
    val monthNames = listOf("Ene","Feb","Mar","Abr","May","Jun","Jul","Ago","Sep","Oct","Nov","Dic")

    var yearExpanded by remember { mutableStateOf(false) }
    var monthExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var habitExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Year selector
        ExposedDropdownMenuBox(
            expanded = yearExpanded,
            onExpandedChange = { yearExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedYear.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.home_filter_year)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(yearExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .width(110.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
            ExposedDropdownMenu(
                expanded = yearExpanded,
                onDismissRequest = { yearExpanded = false }
            ) {
                years.forEach { year ->
                    DropdownMenuItem(
                        text = { Text(year.toString()) },
                        onClick = {
                            onYearChange(year)
                            yearExpanded = false
                        }
                    )
                }
            }
        }

        // Month selector
        ExposedDropdownMenuBox(
            expanded = monthExpanded,
            onExpandedChange = { monthExpanded = it }
        ) {
            OutlinedTextField(
                value = monthNames[selectedMonth - 1],
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.home_filter_month)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(monthExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .width(120.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
            ExposedDropdownMenu(
                expanded = monthExpanded,
                onDismissRequest = { monthExpanded = false }
            ) {
                months.forEach { m ->
                    DropdownMenuItem(
                        text = { Text(monthNames[m - 1]) },
                        onClick = {
                            onMonthChange(m)
                            monthExpanded = false
                        }
                    )
                }
            }
        }

        // Type selector
        ExposedDropdownMenuBox(
            expanded = typeExpanded,
            onExpandedChange = { typeExpanded = it }
        ) {
            val typeLabel = when (selectedType) {
                HabitType.GOOD -> stringResource(R.string.home_filter_good)
                HabitType.BAD -> stringResource(R.string.home_filter_bad)
                null -> stringResource(R.string.home_filter_all)
            }
            OutlinedTextField(
                value = typeLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.home_filter_type)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(typeExpanded) },
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .width(140.dp),
                textStyle = MaterialTheme.typography.bodySmall
            )
            ExposedDropdownMenu(
                expanded = typeExpanded,
                onDismissRequest = { typeExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_filter_all)) },
                    onClick = { onTypeChange(null); typeExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_filter_good)) },
                    onClick = { onTypeChange(HabitType.GOOD); typeExpanded = false }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.home_filter_bad)) },
                    onClick = { onTypeChange(HabitType.BAD); typeExpanded = false }
                )
            }
        }

        // Habit multi-selector (only when multiple habits available)
        if (availableHabits.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = habitExpanded,
                onExpandedChange = { habitExpanded = it }
            ) {
                val habitLabel = when {
                    selectedHabitIds.size == availableHabits.size -> stringResource(R.string.home_filter_all)
                    selectedHabitIds.size == 1 -> availableHabits.firstOrNull {
                        it.id == selectedHabitIds.first()
                    }?.name?.take(12) ?: "1 hábito"
                    else -> "${selectedHabitIds.size} hábitos"
                }
                OutlinedTextField(
                    value = habitLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(R.string.home_filter_habit)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(habitExpanded) },
                    modifier = Modifier
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .width(150.dp),
                    textStyle = MaterialTheme.typography.bodySmall
                )
                ExposedDropdownMenu(
                    expanded = habitExpanded,
                    onDismissRequest = { habitExpanded = false }
                ) {
                    // "All" option
                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = selectedHabitIds.size == availableHabits.size,
                                    onCheckedChange = null
                                )
                                Text(stringResource(R.string.home_filter_all))
                            }
                        },
                        onClick = {
                            val allIds = availableHabits.map { it.id }
                            onHabitsChange(allIds)
                        }
                    )
                    availableHabits.forEach { habit ->
                        val checked = habit.id in selectedHabitIds
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Checkbox(checked = checked, onCheckedChange = null)
                                    Text(habit.name, maxLines = 1)
                                }
                            },
                            onClick = {
                                val newIds = if (checked) {
                                    selectedHabitIds - habit.id
                                } else {
                                    selectedHabitIds + habit.id
                                }
                                onHabitsChange(newIds)
                            }
                        )
                    }
                }
            }
        }
    }
}
