package com.luisisaza.habitos.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.luisisaza.habitos.domain.model.User
import com.luisisaza.habitos.domain.usecase.AuthResult
import com.luisisaza.habitos.domain.usecase.AuthUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val currentUser: User? = null,
    val error: String? = null,
    val success: Boolean = false
)

class AuthViewModel(private val authUseCase: AuthUseCase) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    fun login(username: String, password: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authUseCase.login(username, password)) {
                is AuthResult.Success -> _state.value = AuthUiState(
                    currentUser = result.user,
                    success = true
                )
                is AuthResult.Error -> _state.value = AuthUiState(error = result.message)
            }
        }
    }

    fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        photoPath: String?
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            when (val result = authUseCase.register(
                name, username, email, password, confirmPassword, photoPath
            )) {
                is AuthResult.Success -> _state.value = AuthUiState(
                    currentUser = result.user,
                    success = true
                )
                is AuthResult.Error -> _state.value = AuthUiState(error = result.message)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authUseCase.logout()
            _state.value = AuthUiState()
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetSuccess() {
        _state.value = _state.value.copy(success = false)
    }
}

class AuthViewModelFactory(private val authUseCase: AuthUseCase) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T = AuthViewModel(authUseCase) as T
}
