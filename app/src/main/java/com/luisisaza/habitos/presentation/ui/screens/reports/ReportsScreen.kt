package com.luisisaza.habitos.presentation.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.presentation.ui.components.BottomTab
import com.luisisaza.habitos.presentation.ui.components.FilterBar
import com.luisisaza.habitos.presentation.ui.components.HabitosBottomBar
import com.luisisaza.habitos.presentation.ui.components.MonthCalendarGrid
import com.luisisaza.habitos.presentation.ui.components.WeeklyBarChart
import com.luisisaza.habitos.presentation.viewmodel.ReportsViewModel
import com.luisisaza.habitos.presentation.viewmodel.ReportsViewModelFactory
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    onNavigateHome: () -> Unit,
    onNavigateHabits: () -> Unit,
    onNavigateDaily: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    val viewModel: ReportsViewModel = viewModel(
        factory = ReportsViewModelFactory(app.habitUseCase, app.analyticsUseCase, userId)
    )
    val state by viewModel.state.collectAsState()

    val monthNames = remember {
        (1..12).map {
            Month.of(it).getDisplayName(TextStyle.FULL, Locale("es"))
                .replaceFirstChar { c -> c.uppercase() }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.reports_title), fontWeight = FontWeight.Bold) },
                actions = {
                    com.luisisaza.habitos.presentation.ui.components.ProfileAvatarAction(
                        onClick = onNavigateProfile
                    )
                }
            )
        },
        bottomBar = {
            HabitosBottomBar(
                selected = BottomTab.REPORTS,
                onHomeClick = onNavigateHome,
                onHabitsClick = onNavigateHabits,
                onDailyClick = onNavigateDaily,
                onReportsClick = {}
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FilterBar(
                    selectedYear = state.selectedYear,
                    onYearChange = { viewModel.setYear(it) },
                    selectedMonth = state.selectedMonth,
                    onMonthChange = { viewModel.setMonth(it) },
                    selectedType = state.selectedTypeFilter,
                    onTypeChange = { viewModel.setTypeFilter(it) },
                    selectedHabitIds = state.selectedHabitIds,
                    onHabitsChange = { viewModel.setSelectedHabits(it) },
                    availableHabits = state.habits.filter {
                        state.selectedTypeFilter == null || it.type == state.selectedTypeFilter
                    }
                )
            }

            if (state.isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                return@LazyColumn
            }

            if (state.selectedHabitIds.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = stringResource(R.string.reports_no_data),
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@LazyColumn
            }

            // Stats summary cards (uniform size)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.reports_compliance),
                        value = "${state.totalCompliance.roundToInt()}%"
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.reports_best_streak),
                        value = "${state.habitCompliances.maxOfOrNull { it.streak.bestStreak } ?: 0}",
                        unit = stringResource(R.string.reports_days)
                    )
                    StatCard(
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        title = stringResource(R.string.reports_current_streak),
                        value = "${state.habitCompliances.maxOfOrNull { it.streak.currentStreak } ?: 0}",
                        unit = stringResource(R.string.reports_days)
                    )
                }
            }

            // Calendar of selected month (like home)
            val analysis = state.monthlyAnalysis
            if (analysis != null) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
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
                                selectedDay = state.selectedDay,
                                onDayClick = { viewModel.setSelectedDay(it) }
                            )
                        }
                    }
                }
            }

            // Weekly bar chart based on selected day
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        val weekRangeLabel = remember(state.selectedYear, state.selectedMonth, state.selectedDay) {
                            buildWeekRangeLabel(state.selectedYear, state.selectedMonth, state.selectedDay, monthNames)
                        }
                        Text(
                            text = stringResource(R.string.reports_weekly_chart),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = weekRangeLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        WeeklyBarChart(
                            data = state.weeklyBarData,
                            modifier = Modifier.fillMaxWidth(),
                            barColor = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Quantity totals (bad habits with units)
            if (state.quantityTotals.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.reports_quantity_section),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(12.dp))
                            state.quantityTotals.forEach { qt ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = qt.habitName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Text(
                                        text = "${qt.totalQuantity} ${qt.unit}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Habit breakdown table
            if (state.habitCompliances.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.reports_habit_breakdown),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    stringResource(R.string.reports_habit_column),
                                    modifier = Modifier.weight(1f),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    stringResource(R.string.reports_compliance_column),
                                    modifier = Modifier.width(60.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    stringResource(R.string.reports_streak_column),
                                    modifier = Modifier.width(56.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    stringResource(R.string.reports_failures_column),
                                    modifier = Modifier.width(60.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            state.habitCompliances.forEach { hc ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = hc.habitName,
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${hc.compliancePercent.roundToInt()}%",
                                        modifier = Modifier.width(56.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = when {
                                            hc.compliancePercent >= 67 -> com.luisisaza.habitos.domain.utils.DayColor.GREEN.color
                                            hc.compliancePercent >= 34 -> com.luisisaza.habitos.domain.utils.DayColor.YELLOW.color
                                            else -> com.luisisaza.habitos.domain.utils.DayColor.RED.color
                                        }
                                    )
                                    Text(
                                        text = "${hc.streak.currentStreak}",
                                        modifier = Modifier.width(56.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    val failureLabel = when {
                                        hc.habitType != com.luisisaza.habitos.domain.model.HabitType.BAD -> "—"
                                        hc.totalFailureQuantity > 0 && hc.failureUnit != null ->
                                            "${hc.totalFailureQuantity}"
                                        else -> "${hc.failureCount}"
                                    }
                                    Text(
                                        text = failureLabel,
                                        modifier = Modifier.width(60.dp),
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = if (hc.habitType == com.luisisaza.habitos.domain.model.HabitType.BAD &&
                                            (hc.failureCount > 0 || hc.totalFailureQuantity > 0))
                                            MaterialTheme.colorScheme.error
                                        else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    unit: String? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )
            if (unit != null) {
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

private fun buildWeekRangeLabel(year: Int, month: Int, day: Int, monthNames: List<String>): String {
    val refDate = runCatching { LocalDate.of(year, month, day) }
        .getOrElse { return "${monthNames[month - 1]} $year" }
    val weekStart = refDate.minusDays(refDate.dayOfWeek.value.toLong() - 1)
    val weekEnd = weekStart.plusDays(6)
    val startMonth = monthNames.getOrElse(weekStart.monthValue - 1) { "" }.take(3)
    val endMonth = monthNames.getOrElse(weekEnd.monthValue - 1) { "" }.take(3)
    return if (weekStart.month == weekEnd.month) {
        "${weekStart.dayOfMonth}–${weekEnd.dayOfMonth} $endMonth ${weekEnd.year}"
    } else {
        "${weekStart.dayOfMonth} $startMonth – ${weekEnd.dayOfMonth} $endMonth ${weekEnd.year}"
    }
}
