package com.sujonmax.yourdairy.ui.media

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(
    context: Context,
    private val outputFile: File
) {
    private val appContext = context.applicationContext
    private var recorder: MediaRecorder? = null

    fun start(): Boolean {
        if (recorder != null) return false
        return runCatching {
            outputFile.parentFile?.mkdirs()
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(appContext)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setOutputFile(outputFile.absolutePath)
            r.prepare()
            r.start()
            recorder = r
        }.onFailure {
            recorder?.release()
            recorder = null
            outputFile.delete()
        }.isSuccess
    }

    fun stop(): File? {
        val r = recorder ?: return null
        return try {
            r.stop()
            r.reset()
            r.release()
            recorder = null
            outputFile.takeIf { it.exists() && it.length() > 0L }
        } catch (_: RuntimeException) {
            runCatching { r.reset() }
            r.release()
            recorder = null
            outputFile.delete()
            null
        }
    }

    fun cancel() {
        recorder?.let { r ->
            runCatching { r.stop() }
            runCatching { r.reset() }
            r.release()
        }
        recorder = null
        outputFile.delete()
    }
}
