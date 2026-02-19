/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.prediction

import org.fcitx.fcitx5.android.input.broadcast.InputBroadcastReceiver
import org.fcitx.fcitx5.android.core.FcitxEvent
import org.fcitx.fcitx5.android.daemon.FcitxConnection
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import org.mechdancer.dependency.manager.ManagedHandler
import org.mechdancer.dependency.manager.managedHandler
import androidx.lifecycle.lifecycleScope
import timber.log.Timber
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import android.view.inputmethod.EditorInfo
import org.fcitx.fcitx5.android.core.CapabilityFlags
import org.fcitx.fcitx5.android.data.RecentlyUsed

class PredictionComponent : UniqueComponent<PredictionComponent>(), Dependent,
    ManagedHandler by managedHandler(), InputBroadcastReceiver {

    private val service: FcitxInputMethodService by manager.inputMethodService()
    
    private val _predictions = MutableStateFlow<List<String>>(emptyList())
    val predictions: StateFlow<List<String>> = _predictions

    // App context awareness
    private var currentPackageName: String? = null
    private val browserPackages = setOf("com.android.chrome", "org.mozilla.firefox", "com.google.android.apps.chrome")

    // Internet aware metadata — user can configure via prefs in the future
    var githubUsername: String = ""

    // Simple Bigram/Trigram model for "next two words"
    private val model = mutableMapOf<String, MutableList<String>>()

    // Emoji mapping
    private val emojiMap = mapOf(
        "fire" to "🔥",
        "lit" to "🔥",
        "love" to "❤️",
        "happy" to "😊",
        "slay" to "💅",
        "cap" to "🧢",
        "vibing" to "🌊",
        "energy" to "✨",
        "cool" to "🆒",
        "skull" to "💀",
        "dead" to "💀",
        "ghost" to "👻",
        "github" to "🐙"
    )

    // Domain history tracker
    private val domainHistory = RecentlyUsed("domains", 20).apply {
        if (items.isEmpty()) {
            insert("github.com")
            insert("google.com")
            insert("stackoverflow.com")
            insert("reddit.com")
        }
    }

    init {
        // Pre-populate with some common Gen Z / modern phrases as requested
        train("how", "is", "it")
        train("that", "is", "lit")
        train("no", "cap", "fr")
        train("low", "key", "vibing")
        train("main", "character", "energy")
        train("slay", "the", "day")
        train("bet", "let's", "go")
    }

    override fun onStartInput(info: EditorInfo, capFlags: CapabilityFlags) {
        currentPackageName = info.packageName
        Timber.d("Input started in package: $currentPackageName")
        
        // Zero-input suggestions for browsers
        if (browserPackages.contains(currentPackageName)) {
            service.lifecycleScope.launch(Dispatchers.Default) {
                val topDomains = domainHistory.items.take(3)
                if (topDomains.isNotEmpty()) {
                    Timber.d("Proactive Suggestions for Browser: $topDomains")
                    _predictions.emit(topDomains)
                }
            }
        } else {
            _predictions.value = emptyList()
        }
    }

    private fun train(w1: String, w2: String, w3: String) {
        model.getOrPut(w1) { mutableListOf() }.add("$w2 $w3")
    }

    override fun onClientPreeditUpdate(data: org.fcitx.fcitx5.android.core.FormattedText) {
        if (data.isEmpty()) {
            // Preedit cleared, likely a word was committed.
            // We could trigger prediction here based on the last committed text.
        }
    }

    suspend fun predictNext(word: String): List<String> {
        val key = word.lowercase().trim()
        if (key.isEmpty()) {
            // Check if we should still show browser favorites
            return if (browserPackages.contains(currentPackageName)) domainHistory.items.take(3) else emptyList()
        }
        
        val results = mutableListOf<String>()

        // 1. Model predictions (Bigram/Trigram)
        val wordPredictions = model[key]
        if (wordPredictions != null) {
            results.addAll(wordPredictions)
        }

        // 2. Emoji predictions
        val emoji = emojiMap[key]
        if (emoji != null) {
            results.add(emoji)
        }

        // 3. Internet Awareness (e.g. GitHub)
        if (key == "github" && githubUsername.isNotEmpty()) {
            results.add("github.com/$githubUsername")
        }

        // 4. Domain History
        domainHistory.items.forEach { domain ->
            if (domain.contains(key, ignoreCase = true)) {
                results.add(domain)
            }
        }

        return results.distinct().take(10)
    }

    // Call this when a candidate is selected in the UI
    fun onWordSelected(word: String) {
        // Improved domain detection:
        // 1. Must contain a dot that is NOT at the very end
        // 2. Must not contain spaces
        // 3. Must be longer than 4 chars (e.g. google.com)
        val trimmed = word.trim()
        val dotIndex = trimmed.indexOf('.')
        if (dotIndex > 0 && dotIndex < trimmed.length - 1 && !trimmed.contains(" ") && trimmed.length > 4) {
            domainHistory.insert(trimmed.lowercase())
            Timber.d("Learned new domain: ${trimmed.lowercase()}")
        }
        
        service.lifecycleScope.launch(Dispatchers.Default) {
            val results = predictNext(word)
            if (results.isNotEmpty()) {
                Timber.d("Predictions for $word: $results")
                _predictions.emit(results)
                // In a real implementation, we would now push these 
                // to a "Suggestions" state in the KawaiiBar or Candidates list
            }
        }
    }
}
