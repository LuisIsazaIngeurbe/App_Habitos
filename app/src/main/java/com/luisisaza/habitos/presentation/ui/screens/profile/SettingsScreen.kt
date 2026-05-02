package com.luisisaza.habitos.presentation.ui.screens.profile

import android.hardware.biometrics.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val scope = rememberCoroutineScope()

    val isPinEnabled by app.sessionManager.isPinEnabled.collectAsState(initial = false)
    val isBiometricEnabled by app.sessionManager.isBiometricEnabled.collectAsState(initial = false)

    var showPinSetup by remember { mutableStateOf(false) }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf("") }

    val biometricAvailable = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.getSystemService(BiometricManager::class.java)
                ?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    BiometricManager.BIOMETRIC_SUCCESS
        } else false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Security section
            SectionHeader(stringResource(R.string.settings_security))

            SettingsRow(
                icon = Icons.Default.Pin,
                title = stringResource(R.string.settings_pin),
                subtitle = if (isPinEnabled) stringResource(R.string.generic_enabled)
                else stringResource(R.string.generic_disabled),
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isPinEnabled) {
                            TextButton(onClick = {
                                scope.launch { app.sessionManager.disablePin() }
                            }) { Text(stringResource(R.string.pin_disable), color = MaterialTheme.colorScheme.error) }
                        }
                        Switch(
                            checked = isPinEnabled,
                            onCheckedChange = {
                                if (!isPinEnabled) showPinSetup = true
                                else scope.launch { app.sessionManager.disablePin() }
                            }
                        )
                    }
                }
            )

            if (showPinSetup) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPin = it },
                            label = { Text(stringResource(R.string.pin_setup_title)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                            label = { Text(stringResource(R.string.pin_setup_confirm)) },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            isError = pinError.isNotBlank()
                        )
                        if (pinError.isNotBlank()) {
                            Text(pinError, color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(
                                onClick = {
                                    showPinSetup = false
                                    newPin = ""; confirmPin = ""; pinError = ""
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.generic_cancel)) }
                            Button(
                                onClick = {
                                    if (newPin.length != 4) {
                                        pinError = "El PIN debe tener 4 dígitos"
                                    } else if (newPin != confirmPin) {
                                        pinError = context.getString(R.string.pin_mismatch)
                                        confirmPin = ""
                                    } else {
                                        scope.launch {
                                            app.sessionManager.setPin(newPin)
                                            showPinSetup = false
                                            newPin = ""; confirmPin = ""; pinError = ""
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) { Text(stringResource(R.string.generic_save)) }
                        }
                    }
                }
            }

            if (biometricAvailable) {
                SettingsRow(
                    icon = Icons.Default.Fingerprint,
                    title = stringResource(R.string.settings_biometric),
                    subtitle = if (isBiometricEnabled) stringResource(R.string.generic_enabled)
                    else stringResource(R.string.generic_disabled),
                    trailing = {
                        Switch(
                            checked = isBiometricEnabled,
                            onCheckedChange = { enabled ->
                                scope.launch { app.sessionManager.setBiometricEnabled(enabled) }
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            trailing()
        }
    }
}
