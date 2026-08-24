package com.sujonmax.yourdairy.ui.media

import android.media.MediaRecorder
import android.os.Build
import java.io.File

class VoiceRecorder(private val outputFile: File) {
    private var recorder: MediaRecorder? = null

    fun start(): Boolean {
        if (recorder != null) return false
        return runCatching {
            val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(outputFile.parentFile?.let { outputFile.context } ?: throw IllegalStateException("No context"))
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
        }.isSuccess
    }

    fun stop(): File? {
        val r = recorder ?: return null
        return runCatching {
            r.stop()
            r.reset()
            r.release()
            recorder = null
            outputFile
        }.getOrElse {
            r.release()
            recorder = null
            outputFile.delete()
            null
        }
    }

    fun cancel() {
        recorder?.run {
            runCatching { stop() }
            reset()
            release()
        }
        recorder = null
        outputFile.delete()
    }
}
