/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

data class Language(val code: String, val displayName: String) {
    companion object {
        val SUPPORTED = listOf(
            Language("en", "English"),
            Language("es", "Spanish"),
            Language("fr", "French"),
            Language("de", "German"),
            Language("it", "Italian"),
            Language("pt", "Portuguese"),
            Language("nl", "Dutch"),
            Language("pl", "Polish"),
            Language("ro", "Romanian"),
            Language("cs", "Czech"),
            Language("sv", "Swedish"),
            Language("da", "Danish"),
            Language("fi", "Finnish"),
            Language("hu", "Hungarian"),
            Language("el", "Greek"),
            Language("bg", "Bulgarian"),
            Language("hr", "Croatian"),
            Language("sk", "Slovak"),
            Language("sl", "Slovenian"),
            Language("lt", "Lithuanian"),
            Language("lv", "Latvian"),
            Language("et", "Estonian"),
            Language("ru", "Russian"),
            Language("uk", "Ukrainian"),
            Language("ar", "Arabic"),
            Language("he", "Hebrew"),
            Language("hi", "Hindi"),
            Language("zh", "Chinese"),
            Language("ja", "Japanese"),
            Language("ko", "Korean"),
            Language("th", "Thai"),
            Language("vi", "Vietnamese"),
            Language("id", "Indonesian"),
            Language("ms", "Malay"),
            Language("tr", "Turkish"),
            Language("fa", "Persian"),
            Language("sw", "Swahili"),
            Language("tl", "Filipino"),
            Language("bn", "Bengali"),
            Language("ur", "Urdu")
        )

        private val byCode = SUPPORTED.associateBy { it.code }

        fun displayName(code: String): String =
            byCode[code]?.displayName ?: code.uppercase()
    }
}
