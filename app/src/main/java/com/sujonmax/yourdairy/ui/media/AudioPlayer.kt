package com.sujonmax.yourdairy.ui.media

import android.media.MediaPlayer
import android.net.Uri

class AudioPlayer {
    private var player: MediaPlayer? = null

    fun play(uri: Uri, onCompleted: () -> Unit = {}) {
        stop()
        player = MediaPlayer().apply {
            setDataSource(uri.toString())
            setOnCompletionListener {
                release()
                player = null
                onCompleted()
            }
            prepare()
            start()
        }
    }

    fun stop() {
        player?.let { p ->
            runCatching { if (p.isPlaying) p.stop() }
            p.release()
        }
        player = null
    }
}
