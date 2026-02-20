/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.voice

import android.content.Context
import com.k2fsa.sherpa.onnx.*
import timber.log.Timber
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import java.io.File

class SherpaOnnxASR(private val context: Context) {

    private var recognizer: OnlineRecognizer? = null
    
    // The specific NeMo model name
    private val modelPath = "models/sherpa-onnx-nemo-streaming-fast-conformer-ctc-en-80ms"
    
    fun isInitialized() = recognizer != null

    fun init(): Boolean {
        if (isInitialized()) return true
        try {
            val assetPackManager = AssetPackManagerFactory.getInstance(context)
            val packLocation = assetPackManager.getPackLocation("asr_model")
            
            // Priority: Asset Pack (PAD) -> Internal Storage (FilesDir) -> Assets (Legacy)
            val modelDir = if (packLocation != null) {
                File(packLocation.assetsPath(), modelPath).absolutePath
            } else {
                val internal = File(context.filesDir, modelPath)
                if (internal.exists()) internal.absolutePath else modelPath
            }

            // Verify some key file exist before trying to init
            val modelFile = if (File(modelDir).isAbsolute) File(modelDir, "model.onnx") else null
            val inAssets = modelFile == null || !modelFile.exists()

            val config = OnlineRecognizerConfig(
                featConfig = FeatureConfig(
                    sampleRate = 16000,
                    featureDim = 80
                ),
                modelConfig = OnlineModelConfig(
                    neMoCtc = OnlineNeMoCtcModelConfig(
                        model = if (inAssets) "$modelDir/model.onnx" else modelFile!!.absolutePath
                    ),
                    tokens = if (inAssets) "$modelDir/tokens.txt" else File(modelDir, "tokens.txt").absolutePath,
                    numThreads = 4,
                    debug = false
                ),
                decodingMethod = "greedy_search"
            )
            
            recognizer = if (inAssets) {
                OnlineRecognizer(context.assets, config)
            } else {
                OnlineRecognizer(null, config)
            }
            Timber.i("Sherpa-ONNX ASR initialized from ${if (inAssets) "Assets" else modelDir}")
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
