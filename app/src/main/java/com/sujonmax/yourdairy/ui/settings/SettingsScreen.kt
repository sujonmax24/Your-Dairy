package com.sujonmax.yourdairy.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.BuildConfig
import com.sujonmax.yourdairy.security.SecurityManager

private data class ThemeOption(val id: String, val name: String, val emoji: String, val color: Color)

private val themeOptions = listOf(
    ThemeOption("classic", "Classic Purple", "💜", Color(0xFF6750A4)),
    ThemeOption("ocean", "Ocean Blue", "🌊", Color(0xFF00658A)),
    ThemeOption("forest", "Forest Green", "🌿", Color(0xFF496A45)),
    ThemeOption("rose", "Rose Pink", "🌸", Color(0xFF9A405D)),
    ThemeOption("sunset", "Sunset", "🌅", Color(0xFF9A4600)),
    ThemeOption("midnight", "Midnight", "🌙", Color(0xFF415F91)),
    ThemeOption("lavender", "Lavender", "🪻", Color(0xFF69548C)),
    ThemeOption("coffee", "Coffee", "☕", Color(0xFF795548))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    security: SecurityManager,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    themeName: String,
    onThemeNameChange: (String) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    var themeExpanded by remember { mutableStateOf(false) }
    var biometric by remember { mutableStateOf(security.biometricEnabled) }
    val selectedTheme = themeOptions.firstOrNull { it.id == themeName } ?: themeOptions.first()

    Scaffold(topBar = {
        TopAppBar(title = { Text("Settings") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        })
    }) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Appearance", style = MaterialTheme.typography.titleMedium)

            ListItem(
                leadingContent = { Icon(Icons.Default.DarkMode, null) },
                headlineContent = { Text("Light / Dark mode") },
                supportingContent = { Text(themeMode.replaceFirstChar { it.uppercase() }) },
                trailingContent = {
                    Box {
                        TextButton(onClick = { themeExpanded = true }) { Text("Change") }
                        DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                            listOf("system", "light", "dark").forEach { mode ->
                                DropdownMenuItem(
                                    text = { Text(mode.replaceFirstChar { it.uppercase() }) },
                                    onClick = { onThemeModeChange(mode); themeExpanded = false }
                                )
                            }
                        }
                    }
                }
            )

            ListItem(
                leadingContent = { Icon(Icons.Default.Palette, null) },
                headlineContent = { Text("Diary theme") },
                supportingContent = { Text("${selectedTheme.emoji} ${selectedTheme.name}") },
                trailingContent = { Text("Choose below") }
            )

            Column(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeOptions.take(4).forEach { option -> ThemeCard(option, option.id == themeName) { onThemeNameChange(option.id) } }
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    themeOptions.drop(4).forEach { option -> ThemeCard(option, option.id == themeName) { onThemeNameChange(option.id) } }
                }
            }

            Spacer(Modifier.height(8.dp))
            ListItem(
                leadingContent = { Icon(Icons.Default.TextFields, null) },
                headlineContent = { Text("Font size") },
                supportingContent = { Text("${(fontScale * 100).toInt()}%") },
                trailingContent = {
                    Row {
                        TextButton(onClick = { onFontScaleChange((fontScale - .1f).coerceAtLeast(.8f)) }) { Text("−") }
                        TextButton(onClick = { onFontScaleChange((fontScale + .1f).coerceAtMost(1.4f)) }) { Text("+") }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            Text("Security", style = MaterialTheme.typography.titleMedium)
            ListItem(
                leadingContent = { Icon(Icons.Default.Lock, null) },
                headlineContent = { Text("Biometric unlock") },
                supportingContent = { Text("Use fingerprint or device biometric when available") },
                trailingContent = { Switch(checked = biometric, onCheckedChange = { value -> biometric = value; security.setBiometricEnabled(value) }) }
            )

            Spacer(Modifier.height(8.dp))
            Text("About", style = MaterialTheme.typography.titleMedium)
            ListItem(
                leadingContent = { Icon(Icons.Default.Info, null) },
                headlineContent = { Text("App version") },
                supportingContent = { Text("Version ${BuildConfig.VERSION_NAME} • Created by sujonmax") },
                modifier = Modifier.fillMaxWidth()
            )
            TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("Open About") }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun RowScope.ThemeCard(option: ThemeOption, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(14.dp),
        tonalElevation = if (selected) 5.dp else 1.dp
    ) {
        Column(Modifier.padding(8.dp), horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
            Box(
                Modifier.size(46.dp).clip(RoundedCornerShape(12.dp)).background(option.color),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                Text(option.emoji)
                if (selected) Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.height(5.dp))
            Text(option.name, style = MaterialTheme.typography.labelSmall, maxLines = 1)
        }
    }
}
