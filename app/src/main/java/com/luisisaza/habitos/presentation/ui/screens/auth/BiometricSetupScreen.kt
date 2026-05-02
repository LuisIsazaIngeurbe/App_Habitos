package com.luisisaza.habitos.presentation.ui.screens.auth

import android.hardware.biometrics.BiometricManager
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import kotlinx.coroutines.launch

@Composable
fun BiometricSetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinStep by remember { mutableStateOf(0) } // 0=initial, 1=enter pin, 2=confirm pin
    var errorMsg by remember { mutableStateOf("") }
    var pinVisible by remember { mutableStateOf(false) }

    val biometricAvailable = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.getSystemService(BiometricManager::class.java)
                ?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    BiometricManager.BIOMETRIC_SUCCESS
        } else false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.biometric_setup_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.biometric_setup_description),
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(40.dp))

        if (pinStep == 0) {
            // Initial choice
            if (biometricAvailable) {
                Button(
                    onClick = {
                        scope.launch {
                            app.sessionManager.setBiometricEnabled(true)
                        }
                        pinStep = 1
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Fingerprint, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.biometric_enable))
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedButton(
                onClick = { pinStep = 1 },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(stringResource(R.string.pin_enable))
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onComplete) {
                Text(stringResource(R.string.generic_cancel))
            }

        } else if (pinStep == 1) {
            Text(
                text = stringResource(R.string.pin_setup_title),
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text(stringResource(R.string.pin_title)) },
                singleLine = true,
                visualTransformation = if (pinVisible) androidx.compose.ui.text.input.VisualTransformation.None
                else androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                ),
                modifier = Modifier.fillMaxWidth()
            )
            if (errorMsg.isNotBlank()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (pin.length == 4) {
                        pinStep = 2
                        errorMsg = ""
                    } else {
                        errorMsg = "El PIN debe tener 4 dígitos"
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.generic_confirm)) }

        } else {
            // Confirm PIN
            Text(stringResource(R.string.pin_setup_confirm), style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPin = it },
                label = { Text(stringResource(R.string.pin_setup_confirm)) },
                singleLine = true,
                visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                ),
                modifier = Modifier.fillMaxWidth(),
                isError = errorMsg.isNotBlank()
            )
            if (errorMsg.isNotBlank()) {
                Text(errorMsg, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    if (confirmPin == pin) {
                        scope.launch {
                            app.sessionManager.setPin(pin)
                            onComplete()
                        }
                    } else {
                        errorMsg = context.getString(R.string.pin_mismatch)
                        confirmPin = ""
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(stringResource(R.string.generic_save)) }
        }
    }
}
