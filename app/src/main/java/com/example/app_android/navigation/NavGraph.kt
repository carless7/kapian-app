package com.example.app_android.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_android.ui.screens.LoadingScreen
import com.example.app_android.ui.screens.LoginScreen
import com.example.app_android.ui.screens.MainScreen
import com.example.app_android.ui.screens.RegisterScreen
import com.example.app_android.ui.screens.SettingsScreen
import com.example.app_android.viewmodel.LoginViewModel
import com.example.app_android.viewmodel.SharedViewModel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val sharedViewModel: SharedViewModel = viewModel()
    val loginViewModel: LoginViewModel = viewModel()

    NavHost(navController = navController, startDestination = "loading") {
        composable("loading"){
            LoadingScreen(navController)
        }
        composable("login"){
            LoginScreen(navController, loginViewModel)
        }
        composable("register"){
            RegisterScreen(navController)
        }
        composable("main"){
            MainScreen(navController, sharedViewModel)
        }
        composable("settings"){
            SettingsScreen(navController, sharedViewModel, loginViewModel)
        }
    }
}