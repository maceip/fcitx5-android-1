/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

interface TranslationProvider {
    suspend fun translate(text: String, from: String, to: String): String
}

class StubTranslationProvider : TranslationProvider {
    override suspend fun translate(text: String, from: String, to: String): String {
        return "[$to] $text"
    }
}
