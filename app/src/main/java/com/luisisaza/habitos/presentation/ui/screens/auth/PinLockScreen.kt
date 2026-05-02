package com.luisisaza.habitos.presentation.ui.screens.auth

import android.hardware.biometrics.BiometricManager
import android.os.CancellationSignal
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import kotlinx.coroutines.launch

@Composable
fun PinLockScreen(
    onUnlocked: () -> Unit,
    onUsePinFallback: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val scope = rememberCoroutineScope()

    var pin by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf("") }
    val shakeOffset = remember { Animatable(0f) }

    val biometricEnabled by app.sessionManager.isBiometricEnabled.collectAsState(initial = false)

    val biometricAvailable = remember {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            context.getSystemService(BiometricManager::class.java)
                ?.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ==
                    BiometricManager.BIOMETRIC_SUCCESS
        } else false
    }

    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    10f at 50; (-10f) at 100; 10f at 150; (-10f) at 200
                    8f at 250; (-8f) at 300; 0f at 400
                }
            )
        }
    }

    fun verifyPin() {
        scope.launch {
            val correct = app.sessionManager.verifyPin(pin)
            if (correct) {
                onUnlocked()
            } else {
                errorMsg = context.getString(R.string.pin_error)
                pin = ""
                triggerShake()
            }
        }
    }

    fun launchBiometric() {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.P) return

        val executor = context.mainExecutor
        val cancel = CancellationSignal()
        val callback = object : android.hardware.biometrics.BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: android.hardware.biometrics.BiometricPrompt.AuthenticationResult) {
                onUnlocked()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                errorMsg = errString.toString()
            }
            override fun onAuthenticationFailed() {
                errorMsg = context.getString(R.string.biometric_failed)
            }
        }

        android.hardware.biometrics.BiometricPrompt.Builder(context)
            .setTitle(context.getString(R.string.biometric_title))
            .setSubtitle(context.getString(R.string.biometric_subtitle))
            .setNegativeButton(
                context.getString(R.string.biometric_negative),
                executor
            ) { _, _ -> }
            .build()
            .authenticate(cancel, executor, callback)
    }

    LaunchedEffect(pin) {
        if (pin.length == 4) verifyPin()
    }

    LaunchedEffect(biometricEnabled) {
        if (biometricEnabled && biometricAvailable) launchBiometric()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.pin_title),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(32.dp))

        // PIN dots
        Row(
            modifier = Modifier.offset(x = shakeOffset.value.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(
                            if (index < pin.length) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                        )
                )
            }
        }

        if (errorMsg.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Text(
                text = errorMsg,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(32.dp))

        // Numpad
        PinNumpad(
            onDigit = { digit ->
                if (pin.length < 4) pin += digit
            },
            onBackspace = {
                if (pin.isNotEmpty()) pin = pin.dropLast(1)
                errorMsg = ""
            },
            showBiometric = biometricEnabled && biometricAvailable,
            onBiometric = ::launchBiometric
        )
    }
}

@Composable
private fun PinNumpad(
    onDigit: (String) -> Unit,
    onBackspace: () -> Unit,
    showBiometric: Boolean,
    onBiometric: () -> Unit
) {
    val rows = listOf(
        listOf("1", "2", "3"),
        listOf("4", "5", "6"),
        listOf("7", "8", "9"),
        listOf("bio", "0", "del")
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { key ->
                    when (key) {
                        "bio" -> if (showBiometric) {
                            PinKey(content = {
                                Icon(
                                    Icons.Default.Fingerprint,
                                    contentDescription = stringResource(R.string.login_biometric),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }, onClick = onBiometric)
                        } else {
                            Spacer(Modifier.size(72.dp))
                        }
                        "del" -> PinKey(content = {
                            Text("⌫", fontSize = 20.sp)
                        }, onClick = onBackspace)
                        else -> PinKey(content = {
                            Text(key, style = MaterialTheme.typography.titleLarge)
                        }, onClick = { onDigit(key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun PinKey(
    content: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}
