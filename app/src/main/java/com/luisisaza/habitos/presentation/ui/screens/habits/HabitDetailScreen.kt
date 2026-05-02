package com.luisisaza.habitos.presentation.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.unit.dp
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.HabitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    var habitName by remember { mutableStateOf("") }
    var habitDescription by remember { mutableStateOf("") }
    var habitType by remember { mutableStateOf(HabitType.GOOD) }
    var streakCount by remember { mutableIntStateOf(0) }

    LaunchedEffect(habitId) {
        app.habitRepository.getHabitById(habitId)?.let { entity ->
            habitName = entity.name
            habitDescription = entity.description
            habitType = HabitType.fromValue(entity.type)
            streakCount = entity.streakCount
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(habitName, fontWeight = FontWeight.Bold) },
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
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (habitType == HabitType.BAD) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔥", style = MaterialTheme.typography.displayLarge)
                        Text(
                            text = "$streakCount",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.daily_streak_days, habitName),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.habit_form_description),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(text = habitDescription, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}
