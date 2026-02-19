/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.voice

import android.content.Context
import android.content.res.AssetManager
import com.k2fsa.sherpa.onnx.*
import timber.log.Timber
import java.io.File

class SherpaOnnxASR(private val context: Context) {

    private var recognizer: OnlineRecognizer? = null
    
    // The specific NeMo model provided by the user
    private val modelPath = "models/sherpa-onnx-nemo-streaming-fast-conformer-ctc-en-80ms"
    
    fun isInitialized() = recognizer != null

    fun init(): Boolean {
        if (isInitialized()) return true
        try {
            val assetManager = context.assets
            // Try to find in assets first, then fallback to files
            val inAssets = assetManager.list("models")?.contains("sherpa-onnx-nemo-streaming-fast-conformer-ctc-en-80ms") == true
            
            val modelDir = if (inAssets) modelPath else {
                // FALLBACK: Look in app's internal files dir if not in assets
                File(context.filesDir, modelPath).absolutePath
            }

            val config = OnlineRecognizerConfig(
                featConfig = FeatureConfig(
                    sampleRate = 16000,
                    featureDim = 80
                ),
                modelConfig = OnlineModelConfig(
                    neMoCtc = OnlineNeMoCtcModelConfig(
                        model = "$modelDir/model.onnx"
                    ),
                    tokens = "$modelDir/tokens.txt",
                    numThreads = 4,
                    debug = false
                ),
                decodingMethod = "greedy_search"
            )
            
            recognizer = if (inAssets) {
                OnlineRecognizer(assetManager, config)
            } else {
                OnlineRecognizer(null, config)
            }
            Timber.i("Sherpa-ONNX ASR (NeMo CTC) initialized successfully")
            return true
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize Sherpa-ONNX")
            return false
        }
    }

    fun createStream(): OnlineStream? {
        return recognizer?.createStream()
    }

    fun decode(stream: OnlineStream) {
        recognizer?.decode(stream)
    }

    fun isReady(stream: OnlineStream): Boolean {
        return recognizer?.isReady(stream) == true
    }

    fun getResult(stream: OnlineStream): String {
        return recognizer?.getResult(stream)?.text ?: ""
    }
}
