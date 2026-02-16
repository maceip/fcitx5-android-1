/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

import com.google.mlkit.nl.translate.TranslateLanguage

object LanguageCodeMapper {
    private val mapping = mapOf(
        "en" to TranslateLanguage.ENGLISH,
        "es" to TranslateLanguage.SPANISH,
        "fr" to TranslateLanguage.FRENCH,
        "de" to TranslateLanguage.GERMAN,
        "it" to TranslateLanguage.ITALIAN,
        "pt" to TranslateLanguage.PORTUGUESE,
        "nl" to TranslateLanguage.DUTCH,
        "pl" to TranslateLanguage.POLISH,
        "ro" to TranslateLanguage.ROMANIAN,
        "cs" to TranslateLanguage.CZECH,
        "sv" to TranslateLanguage.SWEDISH,
        "da" to TranslateLanguage.DANISH,
        "fi" to TranslateLanguage.FINNISH,
        "hu" to TranslateLanguage.HUNGARIAN,
        "el" to TranslateLanguage.GREEK,
        "bg" to TranslateLanguage.BULGARIAN,
        "hr" to TranslateLanguage.CROATIAN,
        "sk" to TranslateLanguage.SLOVAK,
        "sl" to TranslateLanguage.SLOVENIAN,
        "lt" to TranslateLanguage.LITHUANIAN,
        "lv" to TranslateLanguage.LATVIAN,
        "et" to TranslateLanguage.ESTONIAN,
        "ru" to TranslateLanguage.RUSSIAN,
        "uk" to TranslateLanguage.UKRAINIAN,
        "ar" to TranslateLanguage.ARABIC,
        "he" to TranslateLanguage.HEBREW,
        "hi" to TranslateLanguage.HINDI,
        "zh" to TranslateLanguage.CHINESE,
        "ja" to TranslateLanguage.JAPANESE,
        "ko" to TranslateLanguage.KOREAN,
        "th" to TranslateLanguage.THAI,
        "vi" to TranslateLanguage.VIETNAMESE,
        "id" to TranslateLanguage.INDONESIAN,
        "ms" to TranslateLanguage.MALAY,
        "tr" to TranslateLanguage.TURKISH,
        "fa" to TranslateLanguage.PERSIAN,
        "sw" to TranslateLanguage.SWAHILI,
        "tl" to TranslateLanguage.TAGALOG,
        "bn" to TranslateLanguage.BENGALI,
        "ur" to TranslateLanguage.URDU
    )

    fun toMlKit(code: String): String? = mapping[code]

    fun isSupported(code: String): Boolean = code in mapping
}
