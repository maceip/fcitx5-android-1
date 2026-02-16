/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.clipboard

import android.content.Context
import android.net.Uri
import timber.log.Timber
import java.io.File
import java.util.UUID

object ClipboardMediaCache {

    private const val DIR_NAME = "clipboard_media"
    internal const val MAX_CACHE_BYTES = 50L * 1024 * 1024 // 50MB

    private lateinit var cacheDir: File

    fun init(context: Context) {
        cacheDir = File(context.cacheDir, DIR_NAME)
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    fun cacheMedia(context: Context, uri: Uri): String? {
        return try {
            if (!::cacheDir.isInitialized) init(context)
            val ext = guessExtension(context, uri)
            val fileName = "${UUID.randomUUID()}$ext"
            val destFile = File(cacheDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: return null
            evictIfNeeded()
            destFile.absolutePath
        } catch (e: Exception) {
            Timber.w(e, "Failed to cache media from $uri")
            null
        }
    }

    private fun guessExtension(context: Context, uri: Uri): String {
        val mime = context.contentResolver.getType(uri) ?: return ".bin"
        return when {
            mime.contains("png") -> ".png"
            mime.contains("jpeg") || mime.contains("jpg") -> ".jpg"
            mime.contains("gif") -> ".gif"
            mime.contains("webp") -> ".webp"
            mime.contains("bmp") -> ".bmp"
            else -> ".bin"
        }
    }

    private fun evictIfNeeded() {
        if (!::cacheDir.isInitialized) return
        evictCache(cacheDir, MAX_CACHE_BYTES)
    }

    fun deleteFile(path: String) {
        try {
            File(path).delete()
        } catch (e: Exception) {
            Timber.w(e, "Failed to delete media cache file: $path")
        }
    }

    fun cleanOrphans(validPaths: Set<String>) {
        if (!::cacheDir.isInitialized) return
        cleanOrphanFiles(cacheDir, validPaths)
    }

    /**
     * Evict oldest files from [dir] until total size is at or below [maxBytes].
     */
    internal fun evictCache(dir: File, maxBytes: Long) {
        val files = dir.listFiles() ?: return
        var totalSize = files.sumOf { it.length() }
        if (totalSize <= maxBytes) return
        val sorted = files.sortedBy { it.lastModified() }
        for (file in sorted) {
            if (totalSize <= maxBytes) break
            val size = file.length()
            if (file.delete()) {
                totalSize -= size
            }
        }
    }

    /**
     * Delete every file in [dir] whose absolute path is not in [validPaths].
     */
    internal fun cleanOrphanFiles(dir: File, validPaths: Set<String>) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.absolutePath !in validPaths) {
                file.delete()
            }
        }
    }
}
