package com.example.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = LeeBlueLight,
    onPrimary = Color(0xFF002F6C),
    primaryContainer = Color(0xFF004494),
    onPrimaryContainer = LeeBlueContainer,
    secondary = LeeTeal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF005047),
    onSecondaryContainer = LeeTealContainer,
    tertiary = LeeViolet,
    background = LeeBackgroundDark,
    onBackground = LeeTextPrimaryDark,
    surface = LeeSurfaceDark,
    onSurface = LeeTextPrimaryDark,
    surfaceVariant = LeeSurfaceVariantDark,
    onSurfaceVariant = LeeTextSecondaryDark,
    outline = LeeOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = LeeBlue,
    onPrimary = Color.White,
    primaryContainer = LeeBlueContainer,
    onPrimaryContainer = LeeOnBlueContainer,
    secondary = LeeTeal,
    onSecondary = Color.White,
    secondaryContainer = LeeTealContainer,
    onSecondaryContainer = Color(0xFF003831),
    tertiary = LeeViolet,
    background = LeeBackgroundLight,
    onBackground = LeeTextPrimaryLight,
    surface = LeeSurfaceLight,
    onSurface = LeeTextPrimaryLight,
    surfaceVariant = LeeSurfaceVariantLight,
    onSurfaceVariant = LeeTextSecondaryLight,
    outline = LeeOutlineLight
)

@Composable
fun LeeTalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep consistent branding colors by default
    content: @Composable () -> Unit,
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
        typography = Typography,
        content = content
    )
}
