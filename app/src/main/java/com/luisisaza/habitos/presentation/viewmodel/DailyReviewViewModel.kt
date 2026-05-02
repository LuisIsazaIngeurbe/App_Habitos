package com.luisisaza.habitos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luisisaza.habitos.data.database.entity.HabitLogEntity
import com.luisisaza.habitos.data.repository.HabitLogRepository
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitLog
import com.luisisaza.habitos.domain.model.toDomain
import com.luisisaza.habitos.domain.usecase.HabitUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DailyHabitItem(
    val habit: Habit,
    val log: HabitLog?,
    val pendingValue: Int? = null
)

data class DailyReviewUiState(
    val items: List<DailyHabitItem> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now()
)

class DailyReviewViewModel(
    private val habitUseCase: HabitUseCase,
    private val habitLogRepository: HabitLogRepository,
    private val userId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(DailyReviewUiState())
    val state: StateFlow<DailyReviewUiState> = _state.asStateFlow()

    private val pendingChanges = mutableMapOf<Long, Pair<Boolean, Int?>>()

    init {
        loadHabitsForDate()
    }

    fun setSelectedDate(date: LocalDate) {
        if (date == _state.value.selectedDate) return
        pendingChanges.clear()
        _state.value = _state.value.copy(selectedDate = date)
        loadHabitsForDate()
    }

    private fun loadHabitsForDate() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching {
                val today = _state.value.selectedDate
                val todayHabits = habitUseCase.getApplicableHabits(userId, today)
                val epochDay = today.toEpochDay()
                val habitIds = todayHabits.map { it.id }
                val logs = if (habitIds.isEmpty()) emptyList() else
                    habitLogRepository.getLogsForDaySuspend(habitIds, epochDay)
                        .map { it.toDomain() }
                val logsByHabit = logs.associateBy { it.habitId }

                todayHabits.map { habit ->
                    DailyHabitItem(habit = habit, log = logsByHabit[habit.id])
                }
            }
                .onSuccess { items ->
                    _state.value = _state.value.copy(items = items, isLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun toggleHabitCompletion(habitId: Long, completed: Boolean) {
        val currentPending = pendingChanges[habitId]
        pendingChanges[habitId] = completed to (currentPending?.second)
        _state.value = _state.value.copy(
            items = _state.value.items.map { item ->
                if (item.habit.id == habitId) {
                    item.copy(
                        log = HabitLog(
                            id = item.log?.id ?: 0,
                            habitId = habitId,
                            date = _state.value.selectedDate,
                            completed = completed,
                            value = if (completed) null else item.log?.value,
                            notes = item.log?.notes,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                } else item
            }
        )
    }

    fun setFailureQuantity(habitId: Long, quantity: Int) {
        val currentCompleted = pendingChanges[habitId]?.first
            ?: _state.value.items.firstOrNull { it.habit.id == habitId }?.log?.completed ?: false
        pendingChanges[habitId] = currentCompleted to quantity
        _state.value = _state.value.copy(
            items = _state.value.items.map { item ->
                if (item.habit.id == habitId) item.copy(pendingValue = quantity) else item
            }
        )
    }

    fun saveReview() {
        if (_state.value.isSaving) return
        viewModelScope.launch {
            _state.value = _state.value.copy(isSaving = true)
            runCatching {
                val today = _state.value.selectedDate
                val epochDay = today.toEpochDay()
                pendingChanges.forEach { (habitId, change) ->
                    val (completed, quantity) = change
                    val existing = habitLogRepository.getLogForHabitOnDay(habitId, epochDay)
                    val log = HabitLogEntity(
                        id = existing?.id ?: 0L,
                        habitId = habitId,
                        date = epochDay,
                        completed = completed,
                        value = quantity,
                        notes = existing?.notes,
                        timestamp = System.currentTimeMillis()
                    )
                    habitLogRepository.upsertLog(log)
                }
                pendingChanges.clear()
            }
                .onSuccess {
                    _state.value = _state.value.copy(isSaving = false, saved = true)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isSaving = false, error = e.message)
                }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun resetSaved() { _state.value = _state.value.copy(saved = false) }
}

class DailyReviewViewModelFactory(
    private val habitUseCase: HabitUseCase,
    private val habitLogRepository: HabitLogRepository,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        DailyReviewViewModel(habitUseCase, habitLogRepository, userId) as T
}
