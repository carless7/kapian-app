package com.example.app_android.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.app_android.auth.AuthenticationManager
import com.example.app_android.ui.screens.LoadingScreen
import com.example.app_android.ui.screens.LoginScreen
import com.example.app_android.ui.screens.MainScreen
import com.example.app_android.ui.screens.RegisterScreen
import com.example.app_android.ui.screens.SettingsScreen
import com.example.app_android.viewmodel.UploadViewmodel

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val authManager = AuthenticationManager(context)
    val viewmodel: UploadViewmodel = viewModel()


    NavHost(navController = navController, startDestination = "loading") {
        composable("loading") {
            LoadingScreen(
                navController = navController
            )
        }
        composable("login") {
            LoginScreen(
                navController = navController,
                authManager = authManager
            )
        }
        composable("register") {
            RegisterScreen(
                navController = navController,
                authManager = authManager
            )
        }

        composable("main") {
            MainScreen(
                navController = navController,
                viewModel = viewmodel
            )
        }
        composable("settings") {
            SettingsScreen(
                navController = navController,
                authManager = authManager,
                viewModel = viewmodel
            )
        }

    }
}