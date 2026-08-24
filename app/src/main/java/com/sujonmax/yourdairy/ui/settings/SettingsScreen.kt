package com.sujonmax.yourdairy.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.security.SecurityManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    security: SecurityManager,
    themeMode: String,
    onThemeModeChange: (String) -> Unit,
    fontScale: Float,
    onFontScaleChange: (Float) -> Unit,
    onBack: () -> Unit,
    onAbout: () -> Unit
) {
    var themeExpanded by remember { mutableStateOf(false) }
    var biometric by remember { mutableStateOf(security.biometricEnabled) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
        )
    }) { padding ->
        Column(
            Modifier.padding(padding).padding(horizontal = 16.dp).verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Appearance", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            ListItem(
                leadingContent = { Icon(Icons.Default.DarkMode, null) },
                headlineContent = { Text("Theme") },
                supportingContent = { Text(themeMode.replaceFirstChar { it.uppercase() }) },
                trailingContent = {
                    androidx.compose.material3.Box {
                        androidx.compose.material3.TextButton(onClick = { themeExpanded = true }) { Text("Change") }
                        DropdownMenu(expanded = themeExpanded, onDismissRequest = { themeExpanded = false }) {
                            listOf("system", "light", "dark").forEach { mode ->
                                DropdownMenuItem(text = { Text(mode.replaceFirstChar { it.uppercase() }) }, onClick = { onThemeModeChange(mode); themeExpanded = false })
                            }
                        }
                    }
                }
            )
            ListItem(
                leadingContent = { Icon(Icons.Default.TextFields, null) },
                headlineContent = { Text("Font size") },
                supportingContent = { Text("${(fontScale * 100).toInt()}%") },
                trailingContent = {
                    Row {
                        androidx.compose.material3.TextButton(onClick = { onFontScaleChange((fontScale - .1f).coerceAtLeast(.8f)) }) { Text("−") }
                        androidx.compose.material3.TextButton(onClick = { onFontScaleChange((fontScale + .1f).coerceAtMost(1.4f)) }) { Text("+") }
                    }
                }
            )

            Spacer(Modifier.height(8.dp))
            Text("Security", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            ListItem(
                leadingContent = { Icon(Icons.Default.Lock, null) },
                headlineContent = { Text("Biometric unlock") },
                supportingContent = { Text("Use fingerprint or device biometric when available") },
                trailingContent = {
                    Switch(checked = biometric, onCheckedChange = { value -> biometric = value; security.setBiometricEnabled(value) })
                }
            )

            Spacer(Modifier.height(8.dp))
            Text("About", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
            ListItem(
                leadingContent = { Icon(Icons.Default.Info, null) },
                headlineContent = { Text("About Dream Diary") },
                supportingContent = { Text("Version 1.0.0 • Create by sujonmax") },
                modifier = Modifier.fillMaxWidth()
            )
            androidx.compose.material3.TextButton(onClick = onAbout, modifier = Modifier.fillMaxWidth()) { Text("Open About") }
        }
    }
}
