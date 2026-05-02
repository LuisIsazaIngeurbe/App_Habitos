package com.luisisaza.habitos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import com.luisisaza.habitos.domain.usecase.AnalyticsUseCase
import com.luisisaza.habitos.domain.usecase.HabitCompliance
import com.luisisaza.habitos.domain.usecase.HabitQuantityTotal
import com.luisisaza.habitos.domain.usecase.HabitUseCase
import com.luisisaza.habitos.domain.usecase.MonthlyAnalysis
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

data class ReportsUiState(
    val habits: List<Habit> = emptyList(),
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedDay: Int = LocalDate.now().dayOfMonth,
    val selectedTypeFilter: HabitType? = null,
    val selectedHabitIds: List<Long> = emptyList(),
    val monthlyAnalysis: MonthlyAnalysis? = null,
    val totalCompliance: Double = 0.0,
    val habitCompliances: List<HabitCompliance> = emptyList(),
    val quantityTotals: List<HabitQuantityTotal> = emptyList(),
    val weeklyBarData: List<Float> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ReportsViewModel(
    private val habitUseCase: HabitUseCase,
    private val analyticsUseCase: AnalyticsUseCase,
    private val userId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(ReportsUiState(isLoading = true))
    val state: StateFlow<ReportsUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            habitUseCase.getAllHabits(userId).collect { habits ->
                val allIds = habits.map { it.id }
                _state.value = _state.value.copy(
                    habits = habits,
                    selectedHabitIds = allIds
                )
                loadAnalytics()
            }
        }
    }

    fun setYear(year: Int) {
        _state.value = _state.value.copy(selectedYear = year)
        loadAnalytics()
    }

    fun setMonth(month: Int) {
        _state.value = _state.value.copy(selectedMonth = month)
        loadAnalytics()
    }

    fun setSelectedDay(day: Int) {
        _state.value = _state.value.copy(selectedDay = day)
        loadAnalytics()
    }

    fun setTypeFilter(type: HabitType?) {
        val filtered = if (type == null) {
            _state.value.habits.map { it.id }
        } else {
            _state.value.habits.filter { it.type == type }.map { it.id }
        }
        _state.value = _state.value.copy(
            selectedTypeFilter = type,
            selectedHabitIds = filtered
        )
        loadAnalytics()
    }

    fun setSelectedHabits(habitIds: List<Long>) {
        _state.value = _state.value.copy(selectedHabitIds = habitIds)
        loadAnalytics()
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val s = _state.value
            if (s.selectedHabitIds.isEmpty()) {
                _state.value = s.copy(
                    monthlyAnalysis = null,
                    totalCompliance = 0.0,
                    habitCompliances = emptyList(),
                    weeklyBarData = emptyList(),
                    isLoading = false
                )
                return@launch
            }

            _state.value = s.copy(isLoading = true)
            runCatching {
                val yearMonth = YearMonth.of(s.selectedYear, s.selectedMonth)
                val startDate = yearMonth.atDay(1)
                val endDate = yearMonth.atEndOfMonth()

                val monthly = analyticsUseCase.getMonthlyAnalysis(
                    s.selectedHabitIds, s.selectedYear, s.selectedMonth
                )
                val compliance = analyticsUseCase.getCompliancePercentage(
                    s.selectedHabitIds, startDate, endDate
                )
                val habitNames = s.habits.associate { it.id to it.name }
                val breakdowns = analyticsUseCase.getHabitComplianceList(
                    s.selectedHabitIds, startDate, endDate, habitNames
                )
                val refDate = runCatching {
                    LocalDate.of(s.selectedYear, s.selectedMonth, s.selectedDay)
                }.getOrElse { LocalDate.of(s.selectedYear, s.selectedMonth, 1) }
                val weekly = analyticsUseCase.getWeeklyBarData(
                    s.selectedHabitIds, refDate
                )
                val quantities = analyticsUseCase.getQuantityTotals(
                    s.selectedHabitIds, startDate, endDate
                )

                AnalyticsBundle(monthly, compliance, breakdowns, weekly, quantities)
            }
                .onSuccess { bundle ->
                    _state.value = _state.value.copy(
                        monthlyAnalysis = bundle.monthly,
                        totalCompliance = bundle.compliance,
                        habitCompliances = bundle.breakdowns,
                        quantityTotals = bundle.quantities,
                        weeklyBarData = bundle.weekly,
                        isLoading = false
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }

    fun refresh() = loadAnalytics()
}

private data class AnalyticsBundle(
    val monthly: MonthlyAnalysis?,
    val compliance: Double,
    val breakdowns: List<HabitCompliance>,
    val weekly: List<Float>,
    val quantities: List<HabitQuantityTotal>
)

class ReportsViewModelFactory(
    private val habitUseCase: HabitUseCase,
    private val analyticsUseCase: AnalyticsUseCase,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ReportsViewModel(habitUseCase, analyticsUseCase, userId) as T
}
