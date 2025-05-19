package com.example.app_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.ui.theme.KapianTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            KapianTheme {
                   AppNavigation()
            }
        }
    }
}