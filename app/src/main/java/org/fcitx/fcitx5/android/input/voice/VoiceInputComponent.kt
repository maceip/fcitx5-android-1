/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.voice

import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import org.fcitx.fcitx5.android.data.InputFeedbacks
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.dependency.context
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import org.mechdancer.dependency.manager.ManagedHandler
import org.mechdancer.dependency.manager.managedHandler
import timber.log.Timber

class VoiceInputComponent : UniqueComponent<VoiceInputComponent>(), Dependent,
    ManagedHandler by managedHandler() {

    private val context: Context by manager.context()
    private val service: FcitxInputMethodService by manager.inputMethodService()

    private var speechRecognizer: SpeechRecognizer? = null
    private var sherpaASR: SherpaOnnxASR? = null
    @Volatile private var isListening = false
    private var audioRecordJob: Job? = null
    @Volatile private var initialized = false

    // Pre-initialized AudioRecord for faster startup
    private var audioRecord: AudioRecord? = null
    private val sampleRate = 16000

    var onStartListening: (() -> Unit)? = null
    var onStopListening: (() -> Unit)? = null
    var onError: ((Int) -> Unit)? = null

    /**
     * Must be called after DI resolution (not in init{}).
     * Called lazily on first use or can be kicked off early from InputView.
     */
    @Synchronized
    fun ensureInitialized() {
        if (initialized) return
        initialized = true
        sherpaASR = SherpaOnnxASR(context)
        service.lifecycleScope.launch(Dispatchers.IO) {
            sherpaASR?.init()
        }
        prepareAudioRecord()
    }

    private fun prepareAudioRecord() {
        if (audioRecord != null) return
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        ).apply {
            if (state != AudioRecord.STATE_INITIALIZED) {
                Timber.e("AudioRecord failed to initialize")
            }
        }
    }

    fun startListening() {
        if (isListening) return
        ensureInitialized()

        // Use Sherpa if already initialized; fall back to system ASR otherwise
        if (sherpaASR?.isInitialized() == true) {
            startSherpaListening()
        } else {
            // Don't call sherpaASR?.init() here — it blocks the main thread.
            // Background init from ensureInitialized() will complete eventually.
            startSystemListening()
        }
    }

    private fun startSystemListening() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {}
                    override fun onBeginningOfSpeech() { onStartListening?.invoke() }
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}
                    override fun onEndOfSpeech() {
                        isListening = false
                        onStopListening?.invoke()
                    }
                    override fun onError(error: Int) {
                        isListening = false
                        onStopListening?.invoke()
                        onError?.invoke(error)
                    }
                    override fun onResults(results: Bundle?) {
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        if (!matches.isNullOrEmpty()) {
                            service.commitText(matches[0])
                        }
                    }
                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }
        }
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
        }
        speechRecognizer?.startListening(intent)
        isListening = true
    }

    private fun startSherpaListening() {
        isListening = true
        onStartListening?.invoke()

        audioRecordJob = service.lifecycleScope.launch(Dispatchers.IO) {
            val stream = sherpaASR?.createStream() ?: return@launch
            var lastWordCount = 0

            // Use pre-initialized audioRecord if possible
            if (audioRecord == null || audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                prepareAudioRecord()
            }

            val ar = audioRecord ?: return@launch
            val bufferSize = ar.bufferSizeInFrames
            val buffer = ShortArray(bufferSize)

            try {
                ar.startRecording()
                while (isListening) {
                    val read = ar.read(buffer, 0, buffer.size)
                    if (read > 0) {
                        val samples = FloatArray(read) { buffer[it] / 32768.0f }
                        stream.acceptWaveform(samples, sampleRate)
                        while (sherpaASR?.isReady(stream) == true) {
                            sherpaASR?.decode(stream)
                        }
                        val text = sherpaASR?.getResult(stream) ?: ""
                        if (text.isNotEmpty()) {
                            val currentWordCount = text.trim().split(Regex("\\s+")).size
                            if (currentWordCount > lastWordCount) {
                                lastWordCount = currentWordCount
                                withContext(Dispatchers.Main) {
                                    InputFeedbacks.hapticASR()
                                }
                            }
                            Timber.v("Partial: %s", text)
                        }
                    }
                }
            } finally {
                ar.stop()
                val finalResult = sherpaASR?.getResult(stream) ?: ""
                stream.release()
                if (finalResult.isNotEmpty()) {
                    // NonCancellable ensures this runs even if the coroutine was cancelled
                    withContext(NonCancellable + Dispatchers.Main) {
                        InputFeedbacks.hapticASRCompletion()
                        service.commitText(finalResult)
                    }
                }
            }
        }
    }

    fun stopListening() {
        if (!isListening) return
        speechRecognizer?.stopListening()
        isListening = false
        audioRecordJob?.cancel()
        onStopListening?.invoke()
    }

    fun cancel() {
        speechRecognizer?.cancel()
        isListening = false
        audioRecordJob?.cancel()
        onStopListening?.invoke()
    }

    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        audioRecord?.release()
        audioRecord = null
        sherpaASR = null
        initialized = false
    }
}
