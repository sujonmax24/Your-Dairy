package com.sujonmax.yourdairy.ui.media

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import java.io.File

class AudioRecorderController(private val context: Context) {
    private var recorder: VoiceRecorder? = null

    @RequiresPermission(Manifest.permission.RECORD_AUDIO)
    fun start(outputFile: File): Boolean {
        if (recorder != null) return false
        val newRecorder = VoiceRecorder(context.applicationContext, outputFile)
        return if (newRecorder.start()) {
            recorder = newRecorder
            true
        } else {
            false
        }
    }

    fun stop(): File? = recorder?.stop().also { recorder = null }

    fun cancel() {
        recorder?.cancel()
        recorder = null
    }

    fun isRecording(): Boolean = recorder != null
}
