/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android

import org.fcitx.fcitx5.android.data.clipboard.db.ClipboardEntry
import org.fcitx.fcitx5.android.input.clipboard.ClipboardAdapter.Companion.excerptText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ClipboardTest {

    /**
     * Real scenario: user copies a huge stacktrace/log dump.
     * excerptText must truncate both the number of lines AND each line's length,
     * and per-line truncation must not eat into other lines' budgets.
     */
    @Test
    fun excerptText_longMultilineInput() {
        val longLine = "x".repeat(200)
        val input = "short\n$longLine\nmedium_len\nfourth\nfifth_should_be_cut"

        val result = excerptText(input, lines = 4, chars = 10)
        val lines = result.lines()

        // Line 1: short enough, passes through
        assertEquals("short", lines[0])
        // Line 2: 200 chars truncated to 10
        assertEquals("x".repeat(10), lines[1])
        // Line 3: "medium_len" is exactly 10 chars
        assertEquals("medium_len", lines[2])
        // Line 4: included (at the limit)
        assertEquals("fourth", lines[3])
        // Line 5: must NOT appear (over line limit)
        assertTrue("5th line should be excluded", !result.contains("fifth"))

        // Also verify chars is per-line: 3 short lines all under limit all appear
        val threeShort = "aaa\nbbb\nccc"
        val shortResult = excerptText(threeShort, chars = 5)
        assertTrue(shortResult.contains("aaa") && shortResult.contains("bbb") && shortResult.contains("ccc"))
    }

    /**
     * Real scenario: user copies a password, clipboard shows masked preview.
     * No original characters may leak. Multi-line masked text loses newlines
     * (current behavior: mask branch uses append() not appendLine()).
     */
    @Test
    fun excerptText_maskedPasswordNeverLeaks() {
        val B = ClipboardEntry.BULLET

        // Single-line password
        val pw = "P@ssw0rd!123"
        val masked = excerptText(pw, mask = true)
        assertEquals(B.repeat(12), masked)
        assertTrue("No original chars in output", masked.none { it.isLetterOrDigit() })

        // Multi-line sensitive data: newlines are dropped in mask mode
        val multiline = "secret\ntoken\nkey"
        val maskedMulti = excerptText(multiline, mask = true)
        assertTrue("No original chars in output", maskedMulti.none { it.isLetterOrDigit() })
        // Documents current behavior: lines concatenated without separator
        assertEquals(B.repeat(6) + B.repeat(5) + B.repeat(3), maskedMulti)

        // Mask + truncation: long password still capped at chars limit
        val longPw = "x".repeat(200)
        val maskedLong = excerptText(longPw, mask = true, chars = 50)
        assertEquals(B.repeat(50), maskedLong)
    }

    /**
     * excerptText uses appendLine() for non-masked lines that have a following
     * newline, and append() for the final line. This tests the exact output
     * format since ClipboardEntryUi displays it directly.
     */
    @Test
    fun excerptText_newlineHandling() {
        // Single line: no trailing newline
        assertEquals("abc", excerptText("abc"))

        // Two lines: first gets \n from appendLine, second doesn't
        assertEquals("abc\ndef", excerptText("abc\ndef"))

        // Newline at exact chars boundary: both lines should appear
        val atBoundary = excerptText("abc\ndef", chars = 3)
        assertTrue(atBoundary.contains("abc") && atBoundary.contains("def"))

        // All-newlines input: should not crash
        val allNewlines = excerptText("\n\n\n", lines = 2)
        assertTrue(allNewlines.length <= 4)
    }
}
