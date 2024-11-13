package com.example.personfinanceapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

//light mode colors
val PastelOrangePrimary = Color(0xFFFFAB91)
val PastelOrangeSecondary = Color(0xFFFFCCBC)
val PastelOrangeTertiary = Color(0xFFFFE0B2)

// Dark Mode Colors
val DarkPastelOrangePrimary = Color(0xFF8D6E63)
val DarkPastelOrangeSecondary = Color(0xFFBCAAA4)
val DarkPastelOrangeTertiary = Color(0xFFD7CCC8)
val BackgroundDark = Color(0xFF1C1B1F)
val SurfaceDark = Color(0xFF2D2C32)
val OnPrimaryDark = Color(0xFFE0E0E0)

private val DarkColorScheme = darkColorScheme(
    primary = DarkPastelOrangePrimary,
    secondary = DarkPastelOrangeSecondary,
    tertiary = DarkPastelOrangeTertiary,
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = OnPrimaryDark
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
