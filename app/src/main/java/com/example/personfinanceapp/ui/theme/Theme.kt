package com.example.personfinanceapp.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

val PastelOrangePrimary = Color(0xFFFFAB91)
val PastelOrangeSecondary = Color(0xFFFFCCBC)
val PastelOrangeTertiary = Color(0xFFFFE0B2)

private val DarkColorScheme = darkColorScheme(
    primary = PastelOrangePrimary,
    secondary = PastelOrangeSecondary,
    tertiary = PastelOrangeTertiary
)

private val LightColorScheme = lightColorScheme(
    primary = PastelOrangePrimary,
    secondary = PastelOrangeSecondary,
    tertiary = PastelOrangeTertiary
)

@Composable
fun PersonfinanceappTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false for testing
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
