package com.sarworks.proemicalculator.presentation.ui.theme

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
import com.sarworks.proemicalculator.presentation.mvi.EmiContract

// Design Tokens for specialized components (Savings, etc)
val LightSlate = Color(0xFFF8FAFC)
val SoftGreen = Color(0xFFF0FFF4)
val EmeraldGreenText = Color(0xFF2E7D32)

private fun getLightColorScheme(theme: EmiContract.AppTheme) = when(theme) {
    EmiContract.AppTheme.DEEP_BLUE -> lightColorScheme(
        primary = Color(0xFF1A365D),
        onPrimary = Color.White,
        background = LightSlate,
        surface = Color.White,
        primaryContainer = Color(0xFF1A365D).copy(alpha = 0.1f)
    )
    EmiContract.AppTheme.EMERALD -> lightColorScheme(
        primary = Color(0xFF2E7D32),
        onPrimary = Color.White,
        background = Color(0xFFF7FEE7),
        surface = Color.White,
        primaryContainer = Color(0xFF2E7D32).copy(alpha = 0.1f)
    )
    EmiContract.AppTheme.CRIMSON -> lightColorScheme(
        primary = Color(0xFFD32F2F),
        onPrimary = Color.White,
        background = Color(0xFFFFF5F5),
        surface = Color.White,
        primaryContainer = Color(0xFFD32F2F).copy(alpha = 0.1f)
    )
    EmiContract.AppTheme.SLATE -> lightColorScheme(
        primary = Color(0xFF475569),
        onPrimary = Color.White,
        background = Color(0xFFF1F5F9),
        surface = Color.White,
        primaryContainer = Color(0xFF475569).copy(alpha = 0.1f)
    )
    EmiContract.AppTheme.PURPLE -> lightColorScheme(
        primary = Color(0xFF6B46C1),
        onPrimary = Color.White,
        background = Color(0xFFFAF5FF),
        surface = Color.White,
        primaryContainer = Color(0xFF6B46C1).copy(alpha = 0.1f)
    )
}

private val DarkBackground = Color(0xFF0F172A)
private val DarkSurface = Color(0xFF1E293B)

private fun getDarkColorScheme(theme: EmiContract.AppTheme) = when (theme) {
    EmiContract.AppTheme.DEEP_BLUE -> darkColorScheme(
        primary = Color(0xFF90B4E8),
        onPrimary = Color(0xFF0B1F3A),
        primaryContainer = Color(0xFF1A365D).copy(alpha = 0.35f),
        background = DarkBackground,
        surface = DarkSurface
    )
    EmiContract.AppTheme.EMERALD -> darkColorScheme(
        primary = Color(0xFF86EFAC),
        onPrimary = Color(0xFF052E16),
        primaryContainer = Color(0xFF2E7D32).copy(alpha = 0.35f),
        background = DarkBackground,
        surface = DarkSurface
    )
    EmiContract.AppTheme.CRIMSON -> darkColorScheme(
        primary = Color(0xFFFCA5A5),
        onPrimary = Color(0xFF450A0A),
        primaryContainer = Color(0xFFD32F2F).copy(alpha = 0.35f),
        background = DarkBackground,
        surface = DarkSurface
    )
    EmiContract.AppTheme.SLATE -> darkColorScheme(
        primary = Color(0xFFCBD5E1),
        onPrimary = Color(0xFF0F172A),
        primaryContainer = Color(0xFF475569).copy(alpha = 0.35f),
        background = DarkBackground,
        surface = DarkSurface
    )
    EmiContract.AppTheme.PURPLE -> darkColorScheme(
        primary = Color(0xFFC4B5FD),
        onPrimary = Color(0xFF2E1065),
        primaryContainer = Color(0xFF6B46C1).copy(alpha = 0.35f),
        background = DarkBackground,
        surface = DarkSurface
    )
}

@Composable
fun ProEmiCalculatorTheme(
    appTheme: EmiContract.AppTheme = EmiContract.AppTheme.DEEP_BLUE,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) getDarkColorScheme(appTheme) else getLightColorScheme(appTheme)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
