package com.mobicom.s18.domanais.joshua.beybladextournamentmanager.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Defining the color schemes for both light and dark themes.
// The app is designed with a dark-first approach to fit the gaming aesthetic.
private val DarkColorScheme = darkColorScheme(
    primary = XtremeBlue,
    secondary = BurstGold,
    tertiary = MidGray,
    background = DarkMetal,
    surface = MidGray,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = LightGray,
    onBackground = LightGray,
    onSurface = LightGray,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = XtremeBlue,
    secondary = BurstGold,
    tertiary = MidGray,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Color.White,
    onSecondary = DarkText,
    onTertiary = LightGray,
    onBackground = DarkText,
    onSurface = DarkText,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun BeybladeXTournamentManagerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // This effect changes the system status bar color to match the app's theme.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
