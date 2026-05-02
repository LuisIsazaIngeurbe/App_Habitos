package com.luisisaza.habitos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import com.luisisaza.habitos.domain.usecase.HabitUseCase
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HabitListUiState(
    val habits: List<Habit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val operationSuccess: Boolean = false
)

class HabitViewModel(
    private val habitUseCase: HabitUseCase,
    private val userId: Long
) : ViewModel() {

    private val _state = MutableStateFlow(HabitListUiState(isLoading = true))
    val state: StateFlow<HabitListUiState> = _state.asStateFlow()

    init {
        loadHabits()
    }

    private fun loadHabits() {
        viewModelScope.launch {
            habitUseCase.getAllHabits(userId)
                .catch { e -> _state.value = HabitListUiState(error = e.message) }
                .collect { habits ->
                    _state.value = HabitListUiState(habits = habits)
                }
        }
    }

    fun getHabitsByType(type: HabitType): Flow<List<Habit>> =
        habitUseCase.getHabitsByType(userId, type)

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { habitUseCase.addHabit(habit) }
                .onSuccess { _state.value = _state.value.copy(isLoading = false, operationSuccess = true) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            runCatching { habitUseCase.updateHabit(habit) }
                .onSuccess { _state.value = _state.value.copy(isLoading = false, operationSuccess = true) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun deleteHabit(id: Long) {
        viewModelScope.launch {
            runCatching { habitUseCase.deleteHabit(id) }
                .onFailure { _state.value = _state.value.copy(error = it.message) }
        }
    }

    fun clearError() { _state.value = _state.value.copy(error = null) }
    fun resetSuccess() { _state.value = _state.value.copy(operationSuccess = false) }
}

class HabitViewModelFactory(
    private val habitUseCase: HabitUseCase,
    private val userId: Long
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        HabitViewModel(habitUseCase, userId) as T
}
