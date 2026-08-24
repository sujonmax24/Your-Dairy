package com.sujonmax.yourdairy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF6750A4),
    secondary = Color(0xFF625B71),
    tertiary = Color(0xFF7D5260)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    tertiary = Color(0xFFEFB8C8)
)

@Composable
fun YourDairyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: Float = 1f,
    content: @Composable () -> Unit
) {
    val base = androidx.compose.material3.Typography()
    fun scale(style: TextStyle) = style.copy(
        fontSize = if (style.fontSize.isUnspecified) style.fontSize else style.fontSize * fontScale,
        lineHeight = if (style.lineHeight.isUnspecified) style.lineHeight else style.lineHeight * fontScale
    )
    val typography = base.copy(
        displayLarge = scale(base.displayLarge), displayMedium = scale(base.displayMedium), displaySmall = scale(base.displaySmall),
        headlineLarge = scale(base.headlineLarge), headlineMedium = scale(base.headlineMedium), headlineSmall = scale(base.headlineSmall),
        titleLarge = scale(base.titleLarge), titleMedium = scale(base.titleMedium), titleSmall = scale(base.titleSmall),
        bodyLarge = scale(base.bodyLarge), bodyMedium = scale(base.bodyMedium), bodySmall = scale(base.bodySmall),
        labelLarge = scale(base.labelLarge), labelMedium = scale(base.labelMedium), labelSmall = scale(base.labelSmall)
    )
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = typography,
        content = content
    )
}
