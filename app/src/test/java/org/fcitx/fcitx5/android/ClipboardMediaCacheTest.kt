/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android

import org.fcitx.fcitx5.android.data.clipboard.ClipboardMediaCache
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class ClipboardMediaCacheTest {

    @get:Rule
    val tempFolder = TemporaryFolder()

    /**
     * Real scenario: user copies many images over time, cache grows past 50MB.
     * evictCache must delete oldest files first and stop as soon as under limit.
     * Also verifies edge cases: empty dir, zero limit.
     */
    @Test
    fun evictCache_deletesOldestFilesFirst() {
        val dir = tempFolder.newFolder("cache")

        // Empty dir should not crash
        ClipboardMediaCache.evictCache(dir, 100)

        // Create 5 files, 100KB each = 500KB total, with ascending timestamps
        (1..5).forEach { i ->
            File(dir, "file$i.png").also {
                it.writeBytes(ByteArray(100 * 1024))
                it.setLastModified(1000L * i)
            }
        }

        // Evict to 250KB: must delete 3 oldest (file1-3), keep 2 newest (file4-5)
        ClipboardMediaCache.evictCache(dir, 250L * 1024)
        val remaining = dir.listFiles()!!.map { it.name }.toSet()
        assertEquals("Should keep exactly 2 files", 2, remaining.size)
        assertFalse("Oldest file should be evicted", "file1.png" in remaining)
        assertTrue("Newest file should survive", "file5.png" in remaining)

        // Under limit: nothing more deleted
        ClipboardMediaCache.evictCache(dir, 250L * 1024)
        assertEquals("No further eviction when under limit", 2, dir.listFiles()!!.size)

        // Zero limit: delete everything
        ClipboardMediaCache.evictCache(dir, 0)
        assertEquals("Zero limit should empty the dir", 0, dir.listFiles()!!.size)
    }

    /**
     * Real scenario: app starts, DB has some entries with media paths but
     * some cached files no longer have DB references (e.g. entry was deleted
     * while app was killed). cleanOrphanFiles removes unreferenced files
     * and keeps referenced ones.
     */
    @Test
    fun cleanOrphanFiles_removesUnreferencedKeepsReferenced() {
        val dir = tempFolder.newFolder("cache")

        // Empty dir should not crash
        ClipboardMediaCache.cleanOrphanFiles(dir, setOf("/some/path"))

        // Mix of referenced and orphaned files
        val keep1 = File(dir, "keep1.png").also { it.createNewFile() }
        val keep2 = File(dir, "keep2.png").also { it.createNewFile() }
        File(dir, "orphan1.png").also { it.createNewFile() }
        File(dir, "orphan2.png").also { it.createNewFile() }

        val validPaths = setOf(keep1.absolutePath, keep2.absolutePath)
        ClipboardMediaCache.cleanOrphanFiles(dir, validPaths)

        val remaining = dir.listFiles()!!.map { it.name }.toSet()
        assertEquals("Should keep only referenced files", setOf("keep1.png", "keep2.png"), remaining)

        // Empty valid set: everything is orphaned
        ClipboardMediaCache.cleanOrphanFiles(dir, emptySet())
        assertEquals("Empty valid set should delete all", 0, dir.listFiles()!!.size)
    }
}
