package com.example.app_android.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_android.ui.screens.loadingScreen
import com.example.app_android.ui.screens.loginScreen
import com.example.app_android.ui.screens.MainScreen
import com.example.app_android.ui.screens.registerScreen
import com.example.app_android.ui.screens.settingsScreen
import com.example.app_android.ui.viewmodels.SharedViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading"){
            loadingScreen(navController)
        }
        composable("login"){
            loginScreen(navController)
        }
        composable("register"){
            registerScreen(navController)
        }
        composable("main"){
            MainScreen(navController, sharedViewModel)
        }
        composable("settings"){
            settingsScreen(navController, sharedViewModel)
        }
    }
}