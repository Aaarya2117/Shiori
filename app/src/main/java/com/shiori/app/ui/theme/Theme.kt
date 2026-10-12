package com.shiori.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PatientPrimary,
    onPrimary = PatientOnPrimary,
    primaryContainer = PatientPrimaryContainer,
    background = PatientBackground,
    surface = PatientSurface,
    onBackground = HighContrastText,
    onSurface = HighContrastText
)

@Composable
fun ShioriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
