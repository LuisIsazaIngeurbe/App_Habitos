package com.luisisaza.habitos.presentation.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.toDomain
import com.luisisaza.habitos.domain.utils.DayColor
import com.luisisaza.habitos.presentation.ui.components.BottomTab
import com.luisisaza.habitos.presentation.ui.components.FilterBar
import com.luisisaza.habitos.presentation.ui.components.HabitosBottomBar
import com.luisisaza.habitos.presentation.ui.components.MonthCalendarGrid
import com.luisisaza.habitos.presentation.viewmodel.ReportsViewModel
import com.luisisaza.habitos.presentation.viewmodel.ReportsViewModelFactory
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

private data class DayBreakdown(
    val completed: List<Habit>,
    val failed: List<Habit>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToHabits: () -> Unit,
    onNavigateToDaily: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    val reportsVm: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(app.habitUseCase, app.analyticsUseCase, userId)
    )
    val state by reportsVm.state.collectAsState()
    val scope = rememberCoroutineScope()

    var selectedDay by remember { mutableStateOf<Int?>(LocalDate.now().dayOfMonth) }
    var dayBreakdown by remember { mutableStateOf<DayBreakdown?>(null) }

    LifecycleResumeEffect(Unit) {
        reportsVm.refresh()
        onPauseOrDispose { }
    }

    val today = LocalDate.now()
    val monthNames = remember {
        (1..12).map {
            Month.of(it).getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { c -> c.uppercase() }
        }
    }

    // Clamp selectedDay if it exceeds the days available in the current month
    LaunchedEffect(state.selectedYear, state.selectedMonth) {
        val maxDay = java.time.YearMonth.of(state.selectedYear, state.selectedMonth).lengthOfMonth()
        val current = selectedDay
        if (current != null && current > maxDay) {
            selectedDay = maxDay
        }
    }

    // Auto-load breakdown for selected day
    LaunchedEffect(selectedDay, state.selectedYear, state.selectedMonth, state.selectedHabitIds) {
        val day = selectedDay ?: return@LaunchedEffect
        if (state.selectedHabitIds.isEmpty()) {
            dayBreakdown = null
            return@LaunchedEffect
        }
        scope.launch {
            val date = runCatching {
                LocalDate.of(state.selectedYear, state.selectedMonth, day)
            }.getOrNull() ?: return@launch
            val logs = app.habitLogRepository.getLogsForDaySuspend(
                state.selectedHabitIds, date.toEpochDay()
            ).map { it.toDomain() }
            val logsByHabit = logs.associateBy { it.habitId }

            val applicableHabits = state.habits.filter { it.id in state.selectedHabitIds }
            val completed = mutableListOf<Habit>()
            val failed = mutableListOf<Habit>()
            applicableHabits.forEach { habit ->
                val log = logsByHabit[habit.id]
                when {
                    log == null -> {} // no log, skip (not yet reviewed)
                    log.completed -> completed.add(habit)
                    else -> failed.add(habit)
                }
            }
            dayBreakdown = DayBreakdown(completed = completed, failed = failed)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.home_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    val systemDark = androidx.compose.foundation.isSystemInDarkTheme()
                    val storedDarkMode by app.sessionManager.isDarkMode
                        .collectAsState(initial = null)
                    val isDark = storedDarkMode ?: systemDark
                    IconButton(onClick = {
                        scope.launch { app.sessionManager.setDarkMode(!isDark) }
                    }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode
                            else Icons.Default.DarkMode,
                            contentDescription = stringResource(
                                if (isDark) R.string.cd_switch_to_light
                                else R.string.cd_switch_to_dark
                            )
                        )
                    }
                    com.luisisaza.habitos.presentation.ui.components.ProfileAvatarAction(
                        onClick = onNavigateToProfile
                    )
                }
            )
        },
        bottomBar = {
            HabitosBottomBar(
                selected = BottomTab.HOME,
                onHomeClick = {},
                onHabitsClick = onNavigateToHabits,
                onDailyClick = onNavigateToDaily,
                onReportsClick = onNavigateToReports
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FilterBar(
                    selectedYear = state.selectedYear,
                    onYearChange = { reportsVm.setYear(it) },
                    selectedMonth = state.selectedMonth,
                    onMonthChange = { reportsVm.setMonth(it) },
                    selectedType = state.selectedTypeFilter,
                    onTypeChange = { reportsVm.setTypeFilter(it) },
                    selectedHabitIds = state.selectedHabitIds,
                    onHabitsChange = { reportsVm.setSelectedHabits(it) },
                    availableHabits = state.habits.filter {
                        state.selectedTypeFilter == null || it.type == state.selectedTypeFilter
                    }
                )
            }

            val analysis = state.monthlyAnalysis
            if (analysis != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${monthNames[state.selectedMonth - 1]} ${state.selectedYear}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))

                            val dayColors = analysis.days.mapValues { (_, v) -> v.color }

                            MonthCalendarGrid(
                                year = state.selectedYear,
                                month = state.selectedMonth,
                                dayColors = dayColors,
                                selectedDay = selectedDay,
                                onDayClick = { selectedDay = it }
                            )

                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                ColorLegendItem(DayColor.GREEN.color, "Cumplido")
                                ColorLegendItem(DayColor.YELLOW.color, "Parcial")
                                ColorLegendItem(DayColor.RED.color, "No cumplido")
                            }
                        }
                    }
                }

                if (selectedDay != null) {
                    val dayData = analysis.days[selectedDay]
                    if (dayData != null) {
                        item {
                            DayDetailCard(
                                day = selectedDay!!,
                                month = monthNames[state.selectedMonth - 1],
                                completedCount = dayData.completedCount,
                                totalCount = dayData.totalCount
                            )
                        }
                    }

                    val breakdown = dayBreakdown
                    if (breakdown != null) {
                        if (breakdown.completed.isNotEmpty()) {
                            item {
                                Text(
                                    text = stringResource(R.string.day_completed_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DayColor.GREEN.color
                                )
                            }
                            items(breakdown.completed.size) { i ->
                                DayHabitRow(
                                    habit = breakdown.completed[i],
                                    color = DayColor.GREEN.color,
                                    icon = Icons.Default.CheckCircle
                                )
                            }
                        }
                        if (breakdown.failed.isNotEmpty()) {
                            item {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.day_pending_title),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = DayColor.RED.color
                                )
                            }
                            items(breakdown.failed.size) { i ->
                                DayHabitRow(
                                    habit = breakdown.failed[i],
                                    color = DayColor.RED.color,
                                    icon = Icons.Default.Cancel
                                )
                            }
                        }
                    }
                }
            } else if (state.habits.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.AddTask, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.home_no_habits),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = onNavigateToHabits) {
                            Text(stringResource(R.string.home_add_first))
                        }
                    }
                }
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
private fun ColorLegendItem(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(shape = CircleShape, color = color, modifier = Modifier.size(10.dp)) {}
        Spacer(Modifier.width(4.dp))
        Text(
            label, style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayDetailCard(day: Int, month: String, completedCount: Int, totalCount: Int) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "$day de $month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "$completedCount/$totalCount hábitos cumplidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(16.dp))
            val percent = if (totalCount > 0) (completedCount * 100 / totalCount) else 0
            Box(
                modifier = Modifier.widthIn(min = 72.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        percent >= 67 -> DayColor.GREEN.color
                        percent >= 34 -> DayColor.YELLOW.color
                        else -> DayColor.RED.color
                    }
                )
            }
        }
    }
}

@Composable
private fun DayHabitRow(
    habit: Habit,
    color: androidx.compose.ui.graphics.Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    var expanded by remember(habit.id) { mutableStateOf(false) }
    val description = habit.description.takeIf { it.isNotBlank() }
    val phrase = habit.reminderPhrase?.takeIf { it.isNotBlank() }
    val canExpand = description != null || phrase != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = canExpand) { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color)
                Spacer(Modifier.width(12.dp))
                Text(
                    habit.name,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                if (canExpand) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess
                        else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                phrase?.let {
                    if (description != null) Spacer(Modifier.height(4.dp))
                    Text(
                        text = "“$it”",
                        style = MaterialTheme.typography.bodySmall,
                        color = color,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
