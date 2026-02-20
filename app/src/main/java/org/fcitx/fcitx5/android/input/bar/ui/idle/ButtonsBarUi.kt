/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar.ui.idle

import android.content.Context
import androidx.annotation.DrawableRes
import com.google.android.flexbox.AlignItems
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.bar.ui.ToolButton
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.view
import android.view.View

class ButtonsBarUi(override val ctx: Context, private val theme: Theme) : Ui {

    override val root = view(::FlexboxLayout) {
        alignItems = AlignItems.CENTER
        justifyContent = JustifyContent.SPACE_AROUND
    }

    private fun toolButton(@DrawableRes icon: Int) = ToolButton(ctx, icon, theme).also {
        val size = ctx.dp(40)
        root.addView(it, FlexboxLayout.LayoutParams(size, size))
    }

    val undoButton = toolButton(R.drawable.ic_baseline_undo_24).apply {
        contentDescription = ctx.getString(R.string.undo)
        visibility = View.GONE
    }

    val redoButton = toolButton(R.drawable.ic_baseline_redo_24).apply {
        contentDescription = ctx.getString(R.string.redo)
        visibility = View.GONE
    }

    val cursorMoveButton = toolButton(R.drawable.ic_cursor_move).apply {
        contentDescription = ctx.getString(R.string.text_editing)
        visibility = View.GONE
    }

    val stickerButton = toolButton(R.drawable.ic_baseline_library_books_24).apply {
        contentDescription = "Stickers"
        alpha = 0.3f
    }

    val gifButton = toolButton(R.drawable.ic_baseline_photo_library_24).apply {
        contentDescription = "GIF"
        alpha = 0.3f
    }

    val clipboardButton = toolButton(R.drawable.ic_baseline_content_paste_24).apply {
        contentDescription = ctx.getString(R.string.clipboard)
    }

    val translateButton = toolButton(R.drawable.ic_translate_24).apply {
        contentDescription = ctx.getString(R.string.translate)
        visibility = View.GONE
    }

    val smileyButton = toolButton(R.drawable.ic_baseline_tag_faces_24).apply {
        contentDescription = "Emoticon"
    }

    val voiceButton = toolButton(R.drawable.ic_baseline_keyboard_voice_24).apply {
        contentDescription = "Voice"
    }

    val moreButton = toolButton(R.drawable.ic_baseline_more_horiz_24).apply {
        contentDescription = ctx.getString(R.string.status_area)
        visibility = View.GONE
    }

}
