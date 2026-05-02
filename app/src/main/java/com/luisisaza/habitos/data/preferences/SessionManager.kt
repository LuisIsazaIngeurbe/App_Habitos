package com.luisisaza.habitos.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.security.MessageDigest

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "habitos_session")
private val Context.securityDataStore: DataStore<Preferences> by preferencesDataStore(name = "habitos_security")

class SessionManager(private val context: Context) {

    companion object {
        private val KEY_LOGGED_USER_ID = longPreferencesKey("logged_user_id")
        private val KEY_PIN_HASH = stringPreferencesKey("pin_hash")
        private val KEY_PIN_ENABLED = booleanPreferencesKey("pin_enabled")
        private val KEY_BIOMETRIC_ENABLED = booleanPreferencesKey("biometric_enabled")
        private val KEY_PROFILE_IMAGE_PATH = stringPreferencesKey("profile_image_path")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode_enabled")
    }

    // ---- Session ----

    val loggedUserId: Flow<Long?> = context.sessionDataStore.data
        .map { prefs -> prefs[KEY_LOGGED_USER_ID]?.takeIf { it > 0 } }

    suspend fun setLoggedUser(userId: Long) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_LOGGED_USER_ID] = userId
        }
    }

    suspend fun clearSession() {
        context.sessionDataStore.edit { prefs ->
            prefs.remove(KEY_LOGGED_USER_ID)
        }
    }

    // ---- PIN ----

    val isPinEnabled: Flow<Boolean> = context.securityDataStore.data
        .map { prefs -> prefs[KEY_PIN_ENABLED] ?: false }

    suspend fun setPin(pin: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_PIN_HASH] = sha256(pin)
            prefs[KEY_PIN_ENABLED] = true
        }
    }

    suspend fun disablePin() {
        context.securityDataStore.edit { prefs ->
            prefs.remove(KEY_PIN_HASH)
            prefs[KEY_PIN_ENABLED] = false
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        val storedHash = context.securityDataStore.data
            .map { it[KEY_PIN_HASH] }
            .firstOrNull()
        return storedHash != null && storedHash == sha256(pin)
    }

    // ---- Biometric ----

    val isBiometricEnabled: Flow<Boolean> = context.securityDataStore.data
        .map { prefs -> prefs[KEY_BIOMETRIC_ENABLED] ?: false }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_BIOMETRIC_ENABLED] = enabled
        }
    }

    // ---- Profile image ----

    val profileImagePath: Flow<String> = context.securityDataStore.data
        .map { prefs -> prefs[KEY_PROFILE_IMAGE_PATH] ?: "" }

    suspend fun setProfileImagePath(path: String) {
        context.securityDataStore.edit { prefs ->
            prefs[KEY_PROFILE_IMAGE_PATH] = path
        }
    }

    // ---- Dark mode ----

    val isDarkMode: Flow<Boolean?> = context.sessionDataStore.data
        .map { prefs -> prefs[KEY_DARK_MODE] }

    suspend fun setDarkMode(enabled: Boolean) {
        context.sessionDataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    // ---- Util ----

    private fun sha256(input: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String): String = sha256(password)
}
