package com.luisisaza.habitos.presentation.ui.screens.habits

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.R
import com.luisisaza.habitos.domain.model.Habit
import com.luisisaza.habitos.domain.model.HabitType
import com.luisisaza.habitos.presentation.ui.components.BottomTab
import com.luisisaza.habitos.presentation.ui.components.HabitCard
import com.luisisaza.habitos.presentation.ui.components.HabitosBottomBar
import com.luisisaza.habitos.presentation.viewmodel.HabitViewModel
import com.luisisaza.habitos.presentation.viewmodel.HabitViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitListScreen(
    onAddGoodHabit: () -> Unit,
    onAddBadHabit: () -> Unit,
    onEditGoodHabit: (Long) -> Unit,
    onEditBadHabit: (Long) -> Unit,
    onHabitDetail: (Long) -> Unit,
    onNavigateHome: () -> Unit,
    onNavigateDaily: () -> Unit,
    onNavigateReports: () -> Unit,
    onNavigateProfile: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)
    val userId = loggedUserId ?: return

    val viewModel: HabitViewModel = viewModel(
        factory = HabitViewModelFactory(app.habitUseCase, userId)
    )
    val state by viewModel.state.collectAsState()

    var deleteTarget by remember { mutableStateOf<Habit?>(null) }
    var selectedTab by remember { mutableIntStateOf(0) }

    val tabs = listOf(stringResource(R.string.home_filter_all), stringResource(R.string.habits_good), stringResource(R.string.habits_bad))
    val filtered = when (selectedTab) {
        1 -> state.habits.filter { it.type == HabitType.GOOD }
        2 -> state.habits.filter { it.type == HabitType.BAD }
        else -> state.habits
    }

    deleteTarget?.let { habit ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.habits_delete_confirm)) },
            text = { Text(stringResource(R.string.habits_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHabit(habit.id)
                    deleteTarget = null
                }) { Text(stringResource(R.string.habits_delete), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.generic_cancel)) }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.habits_title), fontWeight = FontWeight.Bold) },
                actions = {
                    com.luisisaza.habitos.presentation.ui.components.ProfileAvatarAction(
                        onClick = onNavigateProfile
                    )
                }
            )
        },
        bottomBar = {
            HabitosBottomBar(
                selected = BottomTab.HABITS,
                onHomeClick = onNavigateHome,
                onHabitsClick = {},
                onDailyClick = onNavigateDaily,
                onReportsClick = onNavigateReports
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FloatingActionButton(
                    onClick = onAddBadHabit,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ) {
                    Icon(Icons.Default.Block, stringResource(R.string.habits_add_bad))
                }
                FloatingActionButton(onClick = onAddGoodHabit) {
                    Icon(Icons.Default.Add, stringResource(R.string.habits_add_good))
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            if (state.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                return@Column
            }

            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Inbox, null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            stringResource(R.string.habits_empty),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                return@Column
            }

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtered, key = { it.id }) { habit ->
                    HabitCard(
                        habit = habit,
                        todayLog = null,
                        onComplete = {},
                        onEdit = {
                            if (habit.type == HabitType.GOOD) onEditGoodHabit(habit.id)
                            else onEditBadHabit(habit.id)
                        },
                        onDelete = { deleteTarget = habit }
                    )
                }
            }
        }
    }
}
