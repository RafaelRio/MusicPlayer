package com.example.musicplayer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.SystemUiController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// Esquema de colores para el tema claro
val LightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF63B8FF),
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
)

// Esquema de colores para el tema oscuro
val DarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF005ACF),
    onPrimary = Color.White,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
)


@Composable
fun MusicPlayerTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (useDarkTheme) DarkColorScheme else LightColorScheme
    rememberSystemUiController().setSystemBarsColor(Color.White)
    val systemUiController: SystemUiController = rememberSystemUiController()
    systemUiController.setSystemBarsColor(colors.primary)
    MaterialTheme(
        colorScheme = colors,
        typography = Typography, // Puedes usar la tipografía predeterminada o definir la tuya
        shapes = Shapes(), // Puedes usar las formas predeterminadas o definir las tuyas
        content = content
    )
}