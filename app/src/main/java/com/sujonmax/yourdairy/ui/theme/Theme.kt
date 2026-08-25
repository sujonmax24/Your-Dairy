package com.sujonmax.yourdairy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isUnspecified

private data class ThemePalette(val lightPrimary: Color,val lightSecondary: Color,val lightTertiary: Color,val lightBackground: Color,val lightSurface: Color,val darkPrimary: Color,val darkSecondary: Color,val darkTertiary: Color,val darkBackground: Color,val darkSurface: Color)

private val palettes = mapOf(
    "classic" to ThemePalette(Color(0xFF6750A4),Color(0xFF625B71),Color(0xFF7D5260),Color(0xFFFBF8FF),Color(0xFFF4F0FA),Color(0xFFD0BCFF),Color(0xFFCCC2DC),Color(0xFFEFB8C8),Color(0xFF151218),Color(0xFF211E24)),
    "ocean" to ThemePalette(Color(0xFF00658A),Color(0xFF4F616B),Color(0xFF006874),Color(0xFFF4FAFC),Color(0xFFEAF5F8),Color(0xFF7DD0F0),Color(0xFFB7CBD5),Color(0xFF5DD8E7),Color(0xFF071419),Color(0xFF101D22)),
    "forest" to ThemePalette(Color(0xFF496A45),Color(0xFF58624F),Color(0xFF386A42),Color(0xFFF7FAF3),Color(0xFFEEF4EA),Color(0xFFA8D39F),Color(0xFFC0CDB4),Color(0xFF9DD6A4),Color(0xFF101510),Color(0xFF192018)),
    "rose" to ThemePalette(Color(0xFF9A405D),Color(0xFF755761),Color(0xFF80506A),Color(0xFFFFF8F9),Color(0xFFFCEFF2),Color(0xFFFFB0C9),Color(0xFFE5BDC8),Color(0xFFF3B6D2),Color(0xFF1B1115),Color(0xFF26191E)),
    "sunset" to ThemePalette(Color(0xFF9A4600),Color(0xFF725B4F),Color(0xFF805500),Color(0xFFFFF9F4),Color(0xFFFFF0E5),Color(0xFFFFB77D),Color(0xFFE1C1B0),Color(0xFFF4C66D),Color(0xFF1B110A),Color(0xFF271A12)),
    "midnight" to ThemePalette(Color(0xFF415F91),Color(0xFF565F71),Color(0xFF6A5778),Color(0xFFF8F9FF),Color(0xFFEEF1FA),Color(0xFFA9C7FF),Color(0xFFC0C8DA),Color(0xFFD7B9E8),Color(0xFF0D111A),Color(0xFF171C28)),
    "lavender" to ThemePalette(Color(0xFF69548C),Color(0xFF635A70),Color(0xFF79536F),Color(0xFFFBF8FF),Color(0xFFF3EDFA),Color(0xFFD6BAFF),Color(0xFFCBC1D9),Color(0xFFF0B9D8),Color(0xFF17121D),Color(0xFF211B28)),
    "coffee" to ThemePalette(Color(0xFF795548),Color(0xFF6D5E58),Color(0xFF795548),Color(0xFFFFF8F4),Color(0xFFF6EDE8),Color(0xFFE7BFA9),Color(0xFFD2C2BA),Color(0xFFE6BFAE),Color(0xFF18110E),Color(0xFF241A16))
)

private val backgrounds = mapOf(
    "default" to Pair(Color(0xFFFBF8FF),Color(0xFF151218)),
    "sky" to Pair(Color(0xFFEAF6FF),Color(0xFF101A24)),
    "mint" to Pair(Color(0xFFEAF8F1),Color(0xFF101A15)),
    "peach" to Pair(Color(0xFFFFF0E8),Color(0xFF21130D)),
    "rose" to Pair(Color(0xFFFFEEF5),Color(0xFF211119)),
    "lavender" to Pair(Color(0xFFF2EDFF),Color(0xFF181321)),
    "night" to Pair(Color(0xFFE9EEFF),Color(0xFF080E1A)),
    "paper" to Pair(Color(0xFFFFF9EA),Color(0xFF1D1810))
)

@Composable
fun YourDairyTheme(darkTheme: Boolean = isSystemInDarkTheme(),themeName: String = "classic",backgroundName: String = "default",fontScale: Float = 1f,content: @Composable () -> Unit) {
    val palette = palettes[themeName] ?: palettes.getValue("classic")
    val bg = backgrounds[backgroundName] ?: backgrounds.getValue("default")
    val colors = if (darkTheme) darkColorScheme(primary=palette.darkPrimary,secondary=palette.darkSecondary,tertiary=palette.darkTertiary,background=bg.second,surface=palette.darkSurface,surfaceContainer=palette.darkSurface) else lightColorScheme(primary=palette.lightPrimary,secondary=palette.lightSecondary,tertiary=palette.lightTertiary,background=bg.first,surface=palette.lightSurface,surfaceContainer=palette.lightSurface)
    val base = androidx.compose.material3.Typography()
    fun scale(style: TextStyle) = style.copy(fontSize=if(style.fontSize.isUnspecified) style.fontSize else style.fontSize*fontScale,lineHeight=if(style.lineHeight.isUnspecified) style.lineHeight else style.lineHeight*fontScale)
    val typography=base.copy(displayLarge=scale(base.displayLarge),displayMedium=scale(base.displayMedium),displaySmall=scale(base.displaySmall),headlineLarge=scale(base.headlineLarge),headlineMedium=scale(base.headlineMedium),headlineSmall=scale(base.headlineSmall),titleLarge=scale(base.titleLarge),titleMedium=scale(base.titleMedium),titleSmall=scale(base.titleSmall),bodyLarge=scale(base.bodyLarge),bodyMedium=scale(base.bodyMedium),bodySmall=scale(base.bodySmall),labelLarge=scale(base.labelLarge),labelMedium=scale(base.labelMedium),labelSmall=scale(base.labelSmall))
    MaterialTheme(colorScheme=colors,typography=typography,content=content)
}
