package com.luisisaza.habitos.domain.usecase

import com.luisisaza.habitos.data.database.entity.UserEntity
import com.luisisaza.habitos.data.preferences.SessionManager
import com.luisisaza.habitos.data.repository.UserRepository
import com.luisisaza.habitos.domain.model.User
import com.luisisaza.habitos.domain.model.toDomain

sealed class AuthResult {
    data class Success(val user: User) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class AuthUseCase(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {

    suspend fun login(username: String, password: String): AuthResult {
        if (username.isBlank() || password.isBlank()) {
            return AuthResult.Error("Completa todos los campos")
        }
        val hash = sessionManager.hashPassword(password)
        val entity = userRepository.getUserByCredentials(username.trim(), hash)
            ?: return AuthResult.Error("Usuario o contraseña incorrectos")

        sessionManager.setLoggedUser(entity.id)
        return AuthResult.Success(entity.toDomain())
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        confirmPassword: String,
        photoPath: String?
    ): AuthResult {
        when {
            name.isBlank() || username.isBlank() || password.isBlank() ->
                return AuthResult.Error("Completa todos los campos requeridos")
            password != confirmPassword ->
                return AuthResult.Error("Las contraseñas no coinciden")
            password.length < 6 ->
                return AuthResult.Error("La contraseña debe tener al menos 6 caracteres")
            userRepository.getUserByUsername(username.trim()) != null ->
                return AuthResult.Error("El nombre de usuario ya está en uso")
        }

        val entity = UserEntity(
            name = name.trim(),
            username = username.trim(),
            email = email.trim(),
            passwordHash = sessionManager.hashPassword(password),
            photoPath = photoPath,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        val newId = userRepository.insertUser(entity)
        sessionManager.setLoggedUser(newId)
        return AuthResult.Success(entity.copy(id = newId).toDomain())
    }

    suspend fun logout() = sessionManager.clearSession()
}
