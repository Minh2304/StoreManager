package com.storemanager.afada.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AfadaBluePrimary = Color(0xFF0D47A1)
val AfadaBlueSecondary = Color(0xFF1976D2)
val AfadaOrangeAccent = Color(0xFFFF6D00)
val AfadaGreenSuccess = Color(0xFF2E7D32)
val AfadaRedDebt = Color(0xFFD32F2F)
val AfadaWarningAmber = Color(0xFFF57C00)
val AfadaBackground = Color(0xFFF4F6FA)

private val LightColorScheme = lightColorScheme(
    primary = AfadaBluePrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = AfadaOrangeAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDBC8),
    background = AfadaBackground,
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),
    error = AfadaRedDebt
)

@Composable
fun AfadaStoreTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}

