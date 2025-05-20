package com.example.app_android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.ui.theme.KapianTheme
import com.google.firebase.Firebase
import com.google.firebase.database.database

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.database.setPersistenceEnabled(true)

        setContent {
            KapianTheme {
                   AppNavigation()
            }
        }
    }
}