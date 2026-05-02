package com.luisisaza.habitos.presentation.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.luisisaza.habitos.HabitosApp
import com.luisisaza.habitos.presentation.ui.screens.auth.BiometricSetupScreen
import com.luisisaza.habitos.presentation.ui.screens.auth.LoginScreen
import com.luisisaza.habitos.presentation.ui.screens.auth.RegisterScreen
import com.luisisaza.habitos.presentation.ui.screens.auth.PinLockScreen
import com.luisisaza.habitos.presentation.ui.screens.daily.DailyReviewScreen
import com.luisisaza.habitos.presentation.ui.screens.habits.BadHabitFormScreen
import com.luisisaza.habitos.presentation.ui.screens.habits.HabitDetailScreen
import com.luisisaza.habitos.presentation.ui.screens.habits.HabitFormScreen
import com.luisisaza.habitos.presentation.ui.screens.habits.HabitListScreen
import com.luisisaza.habitos.presentation.ui.screens.home.HomeScreen
import com.luisisaza.habitos.presentation.ui.screens.profile.ProfileScreen
import com.luisisaza.habitos.presentation.ui.screens.profile.SettingsScreen
import com.luisisaza.habitos.presentation.ui.screens.reports.ReportsScreen

private fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
fun HabitosNavGraph(navController: NavHostController) {
    val context = LocalContext.current
    val app = context.applicationContext as HabitosApp

    val pinEnabled by app.sessionManager.isPinEnabled.collectAsState(initial = null)
    val loggedUserId by app.sessionManager.loggedUserId.collectAsState(initial = null)

    val startDestination = Routes.SPLASH

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            LaunchedEffect(pinEnabled, loggedUserId) {
                if (pinEnabled == null) return@LaunchedEffect
                // Always go through LOGIN. PIN/biometric is an additional gate
                // before login when previously configured by the user.
                val dest = when {
                    pinEnabled == true -> Routes.PIN_LOCK
                    else -> Routes.LOGIN
                }
                navController.navigate(dest) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
            androidx.compose.material3.Surface(
                modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                color = androidx.compose.material3.MaterialTheme.colorScheme.background
            ) {
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator()
                }
            }
        }

        composable(Routes.PIN_LOCK) {
            PinLockScreen(
                onUnlocked = {
                    // After PIN/biometric success, user must still enter
                    // username + password on LoginScreen.
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.PIN_LOCK) { inclusive = true }
                    }
                },
                onUsePinFallback = {}
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Routes.BIOMETRIC_SETUP) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.BIOMETRIC_SETUP) {
            BiometricSetupScreen(
                onComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.BIOMETRIC_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToHabits = { navController.navigateTab(Routes.HABITS) },
                onNavigateToDaily = { navController.navigateTab(Routes.DAILY_REVIEW) },
                onNavigateToReports = { navController.navigateTab(Routes.REPORTS) },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.HABITS) {
            HabitListScreen(
                onAddGoodHabit = { navController.navigate(Routes.habitForm()) },
                onAddBadHabit = { navController.navigate(Routes.badHabitForm()) },
                onEditGoodHabit = { id -> navController.navigate(Routes.habitForm(id)) },
                onEditBadHabit = { id -> navController.navigate(Routes.badHabitForm(id)) },
                onHabitDetail = { id -> navController.navigate(Routes.habitDetail(id)) },
                onNavigateHome = { navController.navigateTab(Routes.HOME) },
                onNavigateDaily = { navController.navigateTab(Routes.DAILY_REVIEW) },
                onNavigateReports = { navController.navigateTab(Routes.REPORTS) },
                onNavigateProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(
            route = Routes.HABIT_FORM,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val habitId = backStack.arguments?.getLong("habitId")?.takeIf { it > 0 }
            HabitFormScreen(
                editHabitId = habitId,
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.BAD_HABIT_FORM,
            arguments = listOf(
                navArgument("habitId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStack ->
            val habitId = backStack.arguments?.getLong("habitId")?.takeIf { it > 0 }
            BadHabitFormScreen(
                editHabitId = habitId,
                onSaved = { navController.popBackStack() },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.HABIT_DETAIL,
            arguments = listOf(navArgument("habitId") { type = NavType.LongType })
        ) { backStack ->
            val habitId = backStack.arguments!!.getLong("habitId")
            HabitDetailScreen(
                habitId = habitId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DAILY_REVIEW) {
            DailyReviewScreen(
                onNavigateHome = { navController.navigateTab(Routes.HOME) },
                onNavigateHabits = { navController.navigateTab(Routes.HABITS) },
                onNavigateReports = { navController.navigateTab(Routes.REPORTS) },
                onNavigateProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.REPORTS) {
            ReportsScreen(
                onNavigateHome = { navController.navigateTab(Routes.HOME) },
                onNavigateHabits = { navController.navigateTab(Routes.HABITS) },
                onNavigateDaily = { navController.navigateTab(Routes.DAILY_REVIEW) },
                onNavigateProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
