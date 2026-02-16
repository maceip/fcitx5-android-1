/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android

import org.fcitx.fcitx5.android.input.translate.Language
import org.fcitx.fcitx5.android.input.translate.LanguageCodeMapper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TranslateTest {

    /**
     * Real scenario: developer adds a new language to SUPPORTED but forgets
     * to add the ML Kit mapping, or accidentally duplicates a code/name.
     * This single test catches all three mistakes at once.
     */
    @Test
    fun supportedLanguageList_isConsistentAndFullyMapped() {
        val langs = Language.SUPPORTED

        // No duplicate codes (would silently shadow a language in the picker)
        val codes = langs.map { it.code }
        assertEquals("Duplicate language codes found", codes.size, codes.toSet().size)

        // No duplicate display names (would confuse users in the picker)
        val names = langs.map { it.displayName }
        assertEquals("Duplicate display names found", names.size, names.toSet().size)

        // Every supported language must have an ML Kit mapping,
        // otherwise translation silently fails at runtime
        langs.forEach { lang ->
            assertNotNull(
                "${lang.code} (${lang.displayName}) is in SUPPORTED but missing from LanguageCodeMapper",
                LanguageCodeMapper.toMlKit(lang.code)
            )
        }
    }

    /**
     * Language.displayName falls back to uppercased code for unknown languages.
     * This is the only non-trivial logic in the Language class.
     */
    @Test
    fun unknownLanguageCode_fallsBackToUppercase() {
        assertEquals("XYZ", Language.displayName("xyz"))
        assertEquals("QQ", Language.displayName("qq"))
    }
}
