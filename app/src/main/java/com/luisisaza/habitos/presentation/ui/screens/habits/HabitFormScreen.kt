package com.luisisaza.habitos.presentation.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.*
import com.luisisaza.habitos.presentation.viewmodel.HabitViewModel
import com.luisisaza.habitos.presentation.viewmodel.HabitViewModelFactory
import com.luisisaza.habitos.workers.HabitReminderWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitFormScreen(
    editHabitId: Long?,
    onSaved: () -> Unit,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    val viewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(app.habitUseCase, userId)
    )
    val state by viewModel.state.collectAsState()

    // Form state
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<HabitDay>()) }
    var scheduleEnabled by remember { mutableStateOf(false) }
    var startTime by remember { mutableStateOf("08:00") }
    var endTime by remember { mutableStateOf("10:00") }
    var selectedDuration by remember { mutableStateOf<HabitDuration?>(null) }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("08:00") }
    var goal by remember { mutableStateOf("") }
    var nameError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }
    var durationExpanded by remember { mutableStateOf(false) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val isEdit = editHabitId != null
    val existingHabit = if (isEdit) state.habits.firstOrNull { it.id == editHabitId } else null

    LaunchedEffect(existingHabit) {
        existingHabit?.let { h ->
            name = h.name
            description = h.description
            selectedDays = h.days.toSet()
            scheduleEnabled = h.scheduleEnabled
            startTime = h.scheduleStartTime ?: "08:00"
            endTime = h.scheduleEndTime ?: "10:00"
            selectedDuration = h.duration
            reminderEnabled = h.reminderEnabled
            reminderTime = h.reminderTime ?: "08:00"
            goal = h.goal ?: ""
        }
    }

    LaunchedEffect(state.operationSuccess) {
        if (state.operationSuccess) {
            viewModel.resetSuccess()
            onSaved()
        }
    }

    fun save() {
        if (hasSubmitted) return
        nameError = null; daysError = null
        if (name.isBlank()) { nameError = context.getString(R.string.field_required); return }
        if (name.length > 50) { nameError = context.getString(R.string.field_max_chars, 50); return }
        if (selectedDays.isEmpty()) { daysError = context.getString(R.string.habit_form_days_hint); return }
        hasSubmitted = true

        val habit = Habit(
            id = existingHabit?.id ?: 0L,
            userId = userId,
            name = name.trim(),
            description = description.trim(),
            type = HabitType.GOOD,
            days = selectedDays.toList(),
            scheduleEnabled = scheduleEnabled,
            scheduleStartTime = if (scheduleEnabled) startTime else null,
            scheduleEndTime = if (scheduleEnabled) endTime else null,
            duration = if (scheduleEnabled) selectedDuration else null,
            reminderEnabled = reminderEnabled,
            reminderTime = if (reminderEnabled) reminderTime else null,
            reminderPhrase = null,
            failureUnit = null,
            goal = goal.takeIf { it.isNotBlank() },
            streakCount = existingHabit?.streakCount ?: 0,
            isActive = true,
            createdAt = existingHabit?.createdAt ?: System.currentTimeMillis()
        )

        if (isEdit) viewModel.updateHabit(habit) else viewModel.addHabit(habit)

        if (reminderEnabled) {
            HabitReminderWorker.schedule(context, habit.id, habit.name, reminderTime)
        } else {
            HabitReminderWorker.cancel(context, habit.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) stringResource(R.string.habit_form_title_edit)
                        else stringResource(R.string.habit_form_title_create),
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.cd_back_button))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                label = { Text(stringResource(R.string.habit_form_name)) },
                placeholder = { Text(stringResource(R.string.habit_form_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } }
            )

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                label = { Text(stringResource(R.string.habit_form_description)) },
                placeholder = { Text(stringResource(R.string.habit_form_description_hint)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            // Days selection
            Text(
                text = stringResource(R.string.habit_form_days),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HabitDay.entries.forEach { day ->
                    val selected = day in selectedDays
                    FilterChip(
                        selected = selected,
                        onClick = {
                            selectedDays = if (selected) selectedDays - day else selectedDays + day
                            daysError = null
                        },
                        label = { Text(day.displayLabel) }
                    )
                }
            }
            daysError?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            // Schedule toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.habit_form_schedule_enabled), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = scheduleEnabled, onCheckedChange = { scheduleEnabled = it })
            }

            if (scheduleEnabled) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text(stringResource(R.string.habit_form_schedule_time)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("08:00") }
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text(stringResource(R.string.habit_form_schedule_end)) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("10:00") }
                    )
                }

                // Duration dropdown
                ExposedDropdownMenuBox(
                    expanded = durationExpanded,
                    onExpandedChange = { durationExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedDuration?.displayLabel ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.habit_form_duration)) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(durationExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = durationExpanded,
                        onDismissRequest = { durationExpanded = false }
                    ) {
                        HabitDuration.all().forEach { duration ->
                            DropdownMenuItem(
                                text = { Text(duration.displayLabel) },
                                onClick = {
                                    selectedDuration = duration
                                    durationExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Goal
            OutlinedTextField(
                value = goal,
                onValueChange = { goal = it },
                label = { Text(stringResource(R.string.habit_form_goal)) },
                placeholder = { Text(stringResource(R.string.habit_form_goal_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Reminder toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.habit_form_reminder), style = MaterialTheme.typography.bodyLarge)
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            if (reminderEnabled) {
                OutlinedTextField(
                    value = reminderTime,
                    onValueChange = { reminderTime = it },
                    label = { Text(stringResource(R.string.habit_form_reminder_time)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("08:00") }
                )
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = ::save,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = !state.isLoading && !hasSubmitted
            ) {
                if (state.isLoading || hasSubmitted) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.habit_form_save))
                }
            }
        }
    }
}
