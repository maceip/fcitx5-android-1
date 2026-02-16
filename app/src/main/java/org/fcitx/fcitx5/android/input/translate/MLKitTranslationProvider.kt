/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

import android.util.LruCache
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import kotlin.coroutines.resume

class MLKitTranslationProvider : TranslationProvider {

    private val translatorCache = LruCache<String, Translator>(5)

    private fun getTranslator(from: String, to: String): Translator? {
        val mlFrom = LanguageCodeMapper.toMlKit(from) ?: return null
        val mlTo = LanguageCodeMapper.toMlKit(to) ?: return null
        val key = "$mlFrom->$mlTo"
        return translatorCache.get(key) ?: run {
            val options = TranslatorOptions.Builder()
                .setSourceLanguage(mlFrom)
                .setTargetLanguage(mlTo)
                .build()
            val translator = Translation.getClient(options)
            translatorCache.put(key, translator)
            translator
        }
    }

    override suspend fun translate(text: String, from: String, to: String): String {
        val translator = getTranslator(from, to)
            ?: return "Unsupported language pair: $from -> $to"
        return try {
            downloadModelIfNeeded(translator)
            translateText(translator, text)
        } catch (e: Exception) {
            Timber.w(e, "Translation failed")
            when {
                e.message?.contains("download", ignoreCase = true) == true ->
                    "Download failed - check connection"
                else -> "Translation error: ${e.message}"
            }
        }
    }

    private suspend fun downloadModelIfNeeded(translator: Translator) {
        suspendCancellableCoroutine { cont ->
            val conditions = DownloadConditions.Builder().build()
            translator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener { cont.resume(Unit) }
                .addOnFailureListener { e ->
                    cont.resume(Unit) // let translate() handle the error
                    Timber.w(e, "Model download failed")
                }
        }
    }

    private suspend fun translateText(translator: Translator, text: String): String {
        return suspendCancellableCoroutine { cont ->
            translator.translate(text)
                .addOnSuccessListener { result -> cont.resume(result) }
                .addOnFailureListener { e -> cont.resume("Translation error: ${e.message}") }
        }
    }

    fun close() {
        for (i in 0 until translatorCache.size()) {
            translatorCache.snapshot().values.forEach { it.close() }
        }
        translatorCache.evictAll()
    }
}
