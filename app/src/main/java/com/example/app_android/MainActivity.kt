package com.example.app_android

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import com.example.app_android.navigation.AppNavigation
import com.example.app_android.ui.theme.KapianTheme
import com.google.firebase.Firebase
import com.google.firebase.database.database
import com.example.app_android.notifications.FirebaseMessagingUtils

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Firebase.database.setPersistenceEnabled(true)

        setContent {
            KapianTheme {
                   AppNavigation()
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                1001
            )
        }
        FirebaseMessagingUtils.updateFcmToken()
    }
}