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
import androidx.compose.ui.unit.dp

/**
 * Reusable media attachment actions for the diary editor.
 * The actual picker/recorder implementation stays in the editor host.
 */
@Composable
fun MediaAttachmentActions(
    onImageSelected: (Uri) -> Unit,
    onAudioSelected: (Uri) -> Unit,
    onLocationRequested: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { /* Image picker is wired by the editor host. */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Image")
            }
            Button(
                onClick = { /* Audio recorder is wired by the editor host. */ },
                modifier = Modifier.weight(1f)
            ) {
                Text("Voice")
            }
            Button(
                onClick = onLocationRequested,
                modifier = Modifier.weight(1f)
            ) {
                Text("Location")
            }
        }
    }
}
