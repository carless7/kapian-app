// KapianTheme.kt
package com.example.app_android.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

// Definició de colors de marca
val KapianBlue = Color(0xFF165FBD)
val KapianSecondary = Color(0xFF2E2E2E)
val KapianAccent = Color(0xFFF9A825)
val KapianLightBackground = Color(0xFFFFFFFF)
val KapianDarkBackground = Color(0xFF121212)

// Esquema per mode clar
private val LightColorScheme = lightColorScheme(
    primary = KapianBlue,
    secondary = KapianSecondary,
    tertiary = KapianAccent,
    background = KapianLightBackground,
    surface = KapianLightBackground,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.Black,
    onSurface = Color.Black,
)

// Esquema per mode fosc
private val DarkColorScheme = darkColorScheme(
    primary = KapianBlue,
    secondary = KapianSecondary,
    tertiary = KapianAccent,
    background = KapianDarkBackground,
    surface = KapianDarkBackground,
    error = Color.Red,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun KapianTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // activable en Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Mantindrem la tipografia actual, però es pot personalitzar més si cal
        content = content
    )
}
