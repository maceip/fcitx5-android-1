/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

import android.app.AlertDialog
import android.view.View
import android.view.WindowManager
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.input.FcitxInputMethodService
import org.fcitx.fcitx5.android.input.dependency.context
import org.fcitx.fcitx5.android.input.dependency.inputMethodService
import org.fcitx.fcitx5.android.input.dependency.theme
import org.mechdancer.dependency.Dependent
import org.mechdancer.dependency.UniqueComponent
import org.mechdancer.dependency.manager.ManagedHandler
import org.mechdancer.dependency.manager.managedHandler

class TranslateBarComponent : UniqueComponent<TranslateBarComponent>(), Dependent,
    ManagedHandler by managedHandler() {

    private val context by manager.context()
    private val theme by manager.theme()
    private val service: FcitxInputMethodService by manager.inputMethodService()

    private val provider: TranslationProvider = MLKitTranslationProvider()
    private val prefs = AppPrefs.getInstance().internal

    private var sourceLang = prefs.translateSourceLang.getValue()
    private var targetLang = prefs.translateTargetLang.getValue()

    private var translateJob: Job? = null

    val ui by lazy {
        CompactTranslateUi(context, theme).apply {
            root.visibility = View.GONE

            sendButton.setOnClickListener {
                val translated = outputText.text?.toString()
                if (!translated.isNullOrBlank() && translated != "...") {
                    service.commitText(translated)
                }
            }

            translateButton.setOnClickListener {
                performTranslation()
            }

            swapButton.setOnClickListener { swapLanguages() }

            closeButton.setOnClickListener { hide() }

            sourceLangButton.setOnClickListener {
                showLanguagePicker(sourceLang) { selected ->
                    if (selected.code == targetLang) {
                        swapLanguages()
                    } else {
                        sourceLang = selected.code
                        sourceLangButton.text = selected.code.uppercase()
                        persistLangPrefs()
                        retranslateIfNeeded()
                    }
                }
            }

            targetLangButton.setOnClickListener {
                showLanguagePicker(targetLang) { selected ->
                    if (selected.code == sourceLang) {
                        swapLanguages()
                    } else {
                        targetLang = selected.code
                        targetLangButton.text = selected.code.uppercase()
                        persistLangPrefs()
                        retranslateIfNeeded()
                    }
                }
            }

            sourceInput.setOnEditorActionListener { _, _, _ ->
                performTranslation()
                true
            }
        }
    }

    val isVisible: Boolean
        get() = ui.root.visibility == View.VISIBLE

    fun toggle() {
        if (isVisible) hide() else show()
    }

    fun show() {
        ui.sourceLangButton.text = sourceLang.uppercase()
        ui.targetLangButton.text = targetLang.uppercase()
        ui.root.visibility = View.VISIBLE
        ui.root.requestLayout()
    }

    fun hide() {
        translateJob?.cancel()
        ui.root.visibility = View.GONE
        ui.root.requestLayout()
    }

    private fun showLanguagePicker(currentCode: String, onSelected: (Language) -> Unit) {
        val languages = Language.SUPPORTED
        val names = languages.map { it.displayName }.toTypedArray()
        val checkedIndex = languages.indexOfFirst { it.code == currentCode }.coerceAtLeast(0)

        val dialog = AlertDialog.Builder(context)
            .setTitle(R.string.translate_pick_language)
            .setSingleChoiceItems(names, checkedIndex) { dlg, which ->
                onSelected(languages[which])
                dlg.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        dialog.window?.let { win ->
            win.setType(WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG)
            win.attributes = win.attributes.also {
                it.token = ui.root.windowToken
            }
        }
        dialog.show()
    }

    private fun swapLanguages() {
        val tmp = sourceLang
        sourceLang = targetLang
        targetLang = tmp
        persistLangPrefs()
        ui.sourceLangButton.text = sourceLang.uppercase()
        ui.targetLangButton.text = targetLang.uppercase()
        retranslateIfNeeded()
    }

    private fun persistLangPrefs() {
        prefs.translateSourceLang.setValue(sourceLang)
        prefs.translateTargetLang.setValue(targetLang)
    }

    private fun retranslateIfNeeded() {
        val currentInput = ui.sourceInput.text?.toString()
        if (!currentInput.isNullOrBlank()) {
            performTranslation()
        }
    }

    private fun performTranslation() {
        val text = ui.sourceInput.text?.toString() ?: return
        if (text.isBlank()) return
        translateJob?.cancel()
        ui.outputText.text = "..."
        translateJob = service.lifecycleScope.launch {
            val result = provider.translate(text, sourceLang, targetLang)
            ui.outputText.text = result
        }
    }
}
