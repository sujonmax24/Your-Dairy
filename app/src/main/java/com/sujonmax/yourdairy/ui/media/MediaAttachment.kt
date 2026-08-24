package com.sujonmax.yourdairy.ui.media

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Small, reusable media attachment actions for the diary editor.
 * The actual attachment persistence is intentionally kept outside the UI.
 */
@Composable
fun MediaAttachmentActions(
    onImageSelected: (Uri) -> Unit,
    onAudioSelected: (Uri) -> Unit,
    onLocationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.dp(8f))) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(androidx.compose.ui.unit.dp(8f))) {
            Button(onClick = { /* Image picker is wired by the editor host. */ }, modifier = Modifier.weight(1f)) {
                Text("Image")
            }
            Button(onClick = { /* Audio recorder is wired by the editor host. */ }, modifier = Modifier.weight(1f)) {
                Text("Voice")
            }
            Button(onClick = onLocationRequested, modifier = Modifier.weight(1f)) {
                Text("Location")
            }
        }
    }
}
