package com.sujonmax.yourdairy.ui.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Reusable attachment actions. Hosts provide the actual picker/recorder flows. */
@Composable
fun MediaAttachmentActions(
    onImageRequested: () -> Unit,
    onAudioRequested: () -> Unit,
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
            Button(onClick = onImageRequested, modifier = Modifier.weight(1f)) {
                Text("Image")
            }
            Button(onClick = onAudioRequested, modifier = Modifier.weight(1f)) {
                Text("Voice")
            }
            Button(onClick = onLocationRequested, modifier = Modifier.weight(1f)) {
                Text("Location")
            }
        }
    }
}
