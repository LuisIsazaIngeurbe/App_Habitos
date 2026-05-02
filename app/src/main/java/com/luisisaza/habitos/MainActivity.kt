package com.luisisaza.habitos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.luisisaza.habitos.presentation.ui.navigation.HabitosNavGraph
import com.luisisaza.habitos.presentation.ui.theme.HabitosTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val app = applicationContext as HabitosApp
            val systemDark = isSystemInDarkTheme()
            val storedDarkMode by app.sessionManager.isDarkMode.collectAsState(initial = null)
            val darkTheme = storedDarkMode ?: systemDark

            HabitosTheme(darkTheme = darkTheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    HabitosNavGraph(navController = navController)
                }
            }
        }
    }
}
