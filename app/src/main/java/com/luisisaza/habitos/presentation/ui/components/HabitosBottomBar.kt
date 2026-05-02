package com.luisisaza.habitos.presentation.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.luisisaza.habitos.R

enum class BottomTab { HOME, HABITS, DAILY, REPORTS }

@Composable
fun HabitosBottomBar(
    selected: BottomTab,
    onHomeClick: () -> Unit,
    onHabitsClick: () -> Unit,
    onDailyClick: () -> Unit,
    onReportsClick: () -> Unit
) {
    NavigationBar {
        NavigationBarItem(
            selected = selected == BottomTab.HOME,
            onClick = onHomeClick,
            icon = { Icon(Icons.Default.Home, null) },
            label = { Text(stringResource(R.string.nav_home)) }
        )
        NavigationBarItem(
            selected = selected == BottomTab.HABITS,
            onClick = onHabitsClick,
            icon = { Icon(Icons.Default.List, null) },
            label = { Text(stringResource(R.string.nav_habits)) }
        )
        NavigationBarItem(
            selected = selected == BottomTab.DAILY,
            onClick = onDailyClick,
            icon = { Icon(Icons.Default.CheckCircle, null) },
            label = { Text(stringResource(R.string.nav_daily)) }
        )
        NavigationBarItem(
            selected = selected == BottomTab.REPORTS,
            onClick = onReportsClick,
            icon = { Icon(Icons.Default.BarChart, null) },
            label = { Text(stringResource(R.string.nav_reports)) }
        )
    }
}
