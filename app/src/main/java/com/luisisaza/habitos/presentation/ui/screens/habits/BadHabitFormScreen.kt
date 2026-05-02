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
fun BadHabitFormScreen(
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

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var reminderPhrase by remember { mutableStateOf("") }
    var failureUnit by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf("08:00") }

    var nameError by remember { mutableStateOf<String?>(null) }
    var descError by remember { mutableStateOf<String?>(null) }
    var phraseError by remember { mutableStateOf<String?>(null) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val isEdit = editHabitId != null
    val existingHabit = if (isEdit) state.habits.firstOrNull { it.id == editHabitId } else null

    LaunchedEffect(existingHabit) {
        existingHabit?.let { h ->
            name = h.name
            description = h.description
            reminderPhrase = h.reminderPhrase ?: ""
            failureUnit = h.failureUnit ?: ""
            reminderEnabled = h.reminderEnabled
            reminderTime = h.reminderTime ?: "08:00"
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
        nameError = null; descError = null; phraseError = null
        if (name.isBlank()) { nameError = context.getString(R.string.field_required); return }
        if (name.length > 50) { nameError = context.getString(R.string.field_max_chars, 50); return }
        if (description.isBlank()) { descError = context.getString(R.string.field_required); return }
        if (description.length > 300) { descError = context.getString(R.string.field_max_chars, 300); return }
        if (reminderPhrase.isBlank()) { phraseError = context.getString(R.string.field_required); return }
        if (reminderPhrase.length > 100) { phraseError = context.getString(R.string.field_max_chars, 100); return }

        hasSubmitted = true

        val habit = Habit(
            id = existingHabit?.id ?: 0L,
            userId = userId,
            name = name.trim(),
            description = description.trim(),
            type = HabitType.BAD,
            days = emptyList(),
            scheduleEnabled = false,
            scheduleStartTime = null,
            scheduleEndTime = null,
            duration = null,
            reminderEnabled = reminderEnabled,
            reminderTime = if (reminderEnabled) reminderTime else null,
            reminderPhrase = reminderPhrase.trim(),
            failureUnit = failureUnit.trim().takeIf { it.isNotBlank() },
            goal = null,
            streakCount = existingHabit?.streakCount ?: 0,
            isActive = true,
            createdAt = existingHabit?.createdAt ?: System.currentTimeMillis()
        )

        if (isEdit) viewModel.updateHabit(habit) else viewModel.addHabit(habit)

        if (reminderEnabled) {
            HabitReminderWorker.schedule(context, habit.id, habit.name, reminderTime)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEdit) stringResource(R.string.bad_habit_form_title_edit)
                        else stringResource(R.string.bad_habit_form_title_create),
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
            OutlinedTextField(
                value = name,
                onValueChange = { if (it.length <= 50) name = it },
                label = { Text(stringResource(R.string.bad_habit_form_name)) },
                placeholder = { Text(stringResource(R.string.bad_habit_form_name_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                isError = nameError != null,
                supportingText = { nameError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { if (it.length <= 300) description = it },
                label = { Text(stringResource(R.string.bad_habit_form_description)) },
                placeholder = { Text(stringResource(R.string.bad_habit_form_description_hint)) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth(),
                isError = descError != null,
                supportingText = { descError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = reminderPhrase,
                onValueChange = { if (it.length <= 100) reminderPhrase = it },
                label = { Text(stringResource(R.string.bad_habit_form_reminder_phrase)) },
                placeholder = { Text(stringResource(R.string.bad_habit_form_reminder_phrase_hint)) },
                minLines = 2,
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                isError = phraseError != null,
                supportingText = { phraseError?.let { Text(it) } }
            )

            OutlinedTextField(
                value = failureUnit,
                onValueChange = { if (it.length <= 30) failureUnit = it },
                label = { Text(stringResource(R.string.bad_habit_form_unit)) },
                placeholder = { Text(stringResource(R.string.bad_habit_form_unit_hint)) },
                supportingText = { Text(stringResource(R.string.bad_habit_form_unit_help)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

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
                    Text(stringResource(R.string.bad_habit_form_save))
                }
            }
        }
    }
}
