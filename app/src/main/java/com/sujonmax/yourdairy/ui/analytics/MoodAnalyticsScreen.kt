package com.sujonmax.yourdairy.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.data.local.entity.NoteEntity

private data class MoodStat(val mood: String, val count: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodAnalyticsScreen(notes: List<NoteEntity>, onBack: () -> Unit) {
    val stats = notes.mapNotNull { it.mood?.trim()?.takeIf(String::isNotEmpty)?.take(2) }
        .groupingBy { it }.eachCount().entries.sortedByDescending { it.value }
    val totalMood = stats.sumOf { it.value }.coerceAtLeast(1)
    val streak = calculateCurrentStreak(notes)

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Mood & Writing Insights") },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") } }
        )
    }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text("Your diary at a glance", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                ListItem(headlineContent = { Text("${notes.size}") }, supportingContent = { Text("Total memories") })
                ListItem(headlineContent = { Text("${stats.size}") }, supportingContent = { Text("Different moods recorded") })
                ListItem(headlineContent = { Text("$streak days 🔥") }, supportingContent = { Text("Current writing streak") })
            }
            item {
                Spacer(Modifier.height(8.dp))
                Text("Mood distribution", style = MaterialTheme.typography.titleMedium)
            }
            if (stats.isEmpty()) {
                item { Text("No mood data yet. Choose a mood when creating a diary entry.", modifier = Modifier.padding(vertical = 16.dp)) }
            } else {
                items(stats.size) { index ->
                    val entry = stats[index]
                    val fraction = entry.value.toFloat() / totalMood
                    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(entry.key)
                            Text("${entry.value} (${(fraction * 100).toInt()}%)")
                        }
                        LinearProgressIndicator(progress = { fraction }, modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
    }
}

private fun calculateCurrentStreak(notes: List<NoteEntity>): Int {
    val days = notes.map { it.createdAt }.map { java.time.Instant.ofEpochMilli(it).atZone(java.time.ZoneId.systemDefault()).toLocalDate() }.toSet()
    var cursor = java.time.LocalDate.now()
    if (!days.contains(cursor)) cursor = cursor.minusDays(1)
    var streak = 0
    while (days.contains(cursor)) { streak++; cursor = cursor.minusDays(1) }
    return streak
}
