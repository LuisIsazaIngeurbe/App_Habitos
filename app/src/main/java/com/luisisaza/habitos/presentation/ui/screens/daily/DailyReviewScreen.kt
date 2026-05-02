package com.luisisaza.habitos.presentation.ui.screens.daily

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import com.luisisaza.habitos.presentation.ui.components.BottomTab
import com.luisisaza.habitos.presentation.ui.components.HabitosBottomBar
import com.luisisaza.habitos.presentation.viewmodel.DailyReviewViewModel
import com.luisisaza.habitos.presentation.viewmodel.DailyReviewViewModelFactory
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyReviewScreen(
    onNavigateHome: () -> Unit,
    onNavigateHabits: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    val viewModel: DailyReviewViewModel = viewModel(
        factory = DailyReviewViewModelFactory(app.habitUseCase, app.habitLogRepository, userId)
    )
    val state by viewModel.state.collectAsState()

    var showSavedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.saved) {
        if (state.saved) {
            showSavedDialog = true
            viewModel.resetSaved()
        }
    }

    if (showSavedDialog) {
        AlertDialog(
            onDismissRequest = { showSavedDialog = false },
            icon = {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            },
            title = {
                Text(
                    stringResource(R.string.daily_save_success),
                    fontWeight = FontWeight.Bold
                )
            },
            confirmButton = {
                TextButton(onClick = { showSavedDialog = false }) {
                    Text(stringResource(R.string.generic_ok))
                }
            }
        )
    }

    val selectedDate = state.selectedDate
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale("es"))
    val dateLabel = selectedDate.format(dateFormatter).replaceFirstChar { it.uppercase() }
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DateSelectorDialog(
            initialDate = selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                viewModel.setSelectedDate(it)
                showDatePicker = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.daily_title), fontWeight = FontWeight.Bold)
                        Text(
                            text = dateLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = stringResource(R.string.daily_select_date)
                        )
                    }
                    com.luisisaza.habitos.presentation.ui.components.ProfileAvatarAction(
                        onClick = onNavigateProfile
                    )
                }
            )
        },
        bottomBar = {
            HabitosBottomBar(
                selected = BottomTab.DAILY,
                onHomeClick = onNavigateHome,
                onHabitsClick = onNavigateHabits,
                onDailyClick = {},
                onReportsClick = onNavigateReports
            )
        }
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            state.items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.daily_no_habits),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val goodHabits = state.items.filter { it.habit.type == HabitType.GOOD }
                val badHabits = state.items.filter { it.habit.type == HabitType.BAD }

                if (goodHabits.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.habits_good),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    items(goodHabits, key = { it.habit.id }) { item ->
                        DailyGoodHabitCard(
                            habit = item.habit,
                            completed = item.log?.completed,
                            onToggle = { checked ->
                                viewModel.toggleHabitCompletion(item.habit.id, checked)
                            }
                        )
                    }
                }

                if (badHabits.isNotEmpty()) {
                    item {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.habits_bad),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    items(badHabits, key = { it.habit.id }) { item ->
                        val initialQty = item.pendingValue ?: item.log?.value
                        DailyBadHabitCard(
                            habit = item.habit,
                            completed = item.log?.completed,
                            initialQuantity = initialQty,
                            onMarkComplied = { viewModel.toggleHabitCompletion(item.habit.id, true) },
                            onMarkFailed = { viewModel.toggleHabitCompletion(item.habit.id, false) },
                            onQuantityChange = { qty ->
                                viewModel.setFailureQuantity(item.habit.id, qty)
                            }
                        )
                    }
                }

                item {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.saveReview() },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        enabled = !state.isSaving
                    ) {
                        if (state.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(R.string.daily_save))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyGoodHabitCard(
    habit: Habit,
    completed: Boolean?,
    onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(habit.name, style = MaterialTheme.typography.titleMedium)
                val subtitle = when {
                    habit.scheduleStartTime != null ->
                        "${habit.scheduleStartTime}  •  ${habit.duration?.displayLabel ?: ""}"
                    habit.goal != null -> habit.goal
                    else -> null
                }
                subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Checkbox(
                checked = completed == true,
                onCheckedChange = onToggle,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
            )
        }
    }
}

@Composable
private fun DailyBadHabitCard(
    habit: Habit,
    completed: Boolean?,
    initialQuantity: Int?,
    onMarkComplied: () -> Unit,
    onMarkFailed: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    var quantityText by remember(habit.id, initialQuantity) {
        mutableStateOf(initialQuantity?.takeIf { it > 0 }?.toString() ?: "")
    }
    val unitDefined = !habit.failureUnit.isNullOrBlank()
    val quantityEnabled = unitDefined && completed == false

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(habit.name, style = MaterialTheme.typography.titleMedium)
            Text(
                text = "🔥 ${habit.streakCount} ${stringResource(R.string.daily_streak)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = completed == true,
                    onClick = onMarkComplied,
                    label = { Text(stringResource(R.string.daily_completed)) },
                    leadingIcon = if (completed == true) {
                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = completed == false,
                    onClick = onMarkFailed,
                    label = { Text(stringResource(R.string.daily_failed)) },
                    leadingIcon = if (completed == false) {
                        { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.error
                    )
                )
            }

            if (unitDefined) {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { input ->
                        if (input.all { it.isDigit() } && input.length <= 4) {
                            quantityText = input
                            onQuantityChange(input.toIntOrNull() ?: 0)
                        }
                    },
                    enabled = quantityEnabled,
                    label = { Text(stringResource(R.string.daily_quantity_for_unit, habit.failureUnit ?: "")) },
                    placeholder = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectorDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(java.time.ZoneOffset.UTC)
            .toInstant()
            .toEpochMilli()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                pickerState.selectedDateMillis?.let { millis ->
                    val date = java.time.Instant.ofEpochMilli(millis)
                        .atZone(java.time.ZoneOffset.UTC)
                        .toLocalDate()
                    onDateSelected(date)
                } ?: onDismiss()
            }) { Text(stringResource(R.string.generic_ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.generic_cancel)) }
        }
    ) {
        DatePicker(state = pickerState)
    }
}
