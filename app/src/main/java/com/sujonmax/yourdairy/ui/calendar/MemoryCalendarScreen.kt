package com.sujonmax.yourdairy.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sujonmax.yourdairy.data.local.entity.NoteEntity
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@Composable
fun MemoryCalendarScreen(
    notes: List<NoteEntity>,
    onOpenNote: (NoteEntity) -> Unit,
    onBack: () -> Unit
) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    val noteDates = remember(notes) { notes.groupBy(::noteDate) }
    val selectedNotes = selectedDate?.let { noteDates[it].orEmpty() }.orEmpty()
    val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
    val totalCells = ((firstDayOffset + month.lengthOfMonth() + 6) / 7) * 7

    Column(Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            IconButton(onClick = onBack) { Icon(Icons.Default.Close, contentDescription = "Close calendar") }
            Text("Memory Calendar", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            IconButton(onClick = { month = month.minusMonths(1) }) { Icon(Icons.Default.ChevronLeft, contentDescription = "Previous month") }
            IconButton(onClick = { month = month.plusMonths(1) }) { Icon(Icons.Default.ChevronRight, contentDescription = "Next month") }
        }

        Text(monthLabel(month), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(12.dp))

        val weekDays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach { day -> Text(day, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold) }
        }
        Spacer(Modifier.height(6.dp))

        repeat(totalCells / 7) { week ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { dayOfWeek ->
                    val index = week * 7 + dayOfWeek
                    val dayNumber = index - firstDayOffset + 1
                    if (dayNumber in 1..month.lengthOfMonth()) {
                        val date = month.atDay(dayNumber)
                        CalendarDay(
                            date = date,
                            notes = noteDates[date].orEmpty(),
                            selected = date == selectedDate,
                            modifier = Modifier.weight(1f),
                            onClick = { selectedDate = date }
                        )
                    } else {
                        Box(Modifier.weight(1f).size(48.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        selectedDate?.let { date ->
            Text("Memories on $date", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            if (selectedNotes.isEmpty()) {
                Text("No memory saved on this date.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(selectedNotes, key = { it.id }) { note ->
                        Surface(onClick = { onOpenNote(note) }, modifier = Modifier.fillMaxWidth(), tonalElevation = 2.dp) {
                            Column(Modifier.padding(14.dp)) {
                                Text(note.title.ifBlank { "Untitled" }, fontWeight = FontWeight.Bold)
                                if (!note.mood.isNullOrBlank()) Text(note.mood.take(2))
                                Text(note.content.ifBlank { "No content" }, maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        } ?: Text("Tap a date to explore your memories.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    notes: List<NoteEntity>,
    selected: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val background = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    Box(
        modifier = modifier.size(48.dp).padding(2.dp).background(background, MaterialTheme.shapes.small).clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(date.dayOfMonth.toString(), fontWeight = if (notes.isNotEmpty()) FontWeight.Bold else FontWeight.Normal)
            if (notes.isNotEmpty()) {
                val mood = notes.firstNotNullOfOrNull { it.mood?.take(2)?.takeIf(String::isNotBlank) }
                Text(mood ?: "•", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

private fun noteDate(note: NoteEntity): LocalDate =
    Instant.ofEpochMilli(note.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()

private fun monthLabel(month: YearMonth): String =
    month.month.name.lowercase().replaceFirstChar { it.uppercase() } + " ${month.year}"
