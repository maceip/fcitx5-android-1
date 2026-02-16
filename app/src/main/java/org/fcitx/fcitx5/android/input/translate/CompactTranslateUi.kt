/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.horizontalLayout
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.verticalLayout
import splitties.views.dsl.core.wrapContent
import splitties.views.padding

class CompactTranslateUi(
    override val ctx: Context,
    private val theme: Theme
) : Ui {

    val sourceLangButton = Button(ctx).apply {
        text = "EN"
        setTextColor(theme.keyTextColor)
        setBackgroundColor(theme.barColor)
        textSize = 11f
        isAllCaps = true
        setPadding(ctx.dp(4), 0, ctx.dp(4), 0)
        minHeight = ctx.dp(28)
        minimumHeight = ctx.dp(28)
    }

    val swapButton = ImageButton(ctx).apply {
        setImageResource(R.drawable.ic_baseline_swap_horiz_24)
        setColorFilter(theme.altKeyTextColor)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        contentDescription = ctx.getString(R.string.translate_swap)
    }

    val targetLangButton = Button(ctx).apply {
        text = "ES"
        setTextColor(theme.keyTextColor)
        setBackgroundColor(theme.barColor)
        textSize = 11f
        isAllCaps = true
        setPadding(ctx.dp(4), 0, ctx.dp(4), 0)
        minHeight = ctx.dp(28)
        minimumHeight = ctx.dp(28)
    }

    val closeButton = ImageButton(ctx).apply {
        setImageResource(R.drawable.ic_baseline_close_24)
        setColorFilter(theme.altKeyTextColor)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        contentDescription = ctx.getString(R.string.back_to_keyboard)
    }

    private val langBar = horizontalLayout {
        gravity = Gravity.CENTER_VERTICAL
        add(sourceLangButton, lParams(0, wrapContent) { weight = 1f })
        add(swapButton, lParams(ctx.dp(32), ctx.dp(28)))
        add(targetLangButton, lParams(0, wrapContent) { weight = 1f })
        add(closeButton, lParams(ctx.dp(32), ctx.dp(28)))
    }

    val sourceInput = EditText(ctx).apply {
        hint = ctx.getString(R.string.translate_source_hint)
        setHintTextColor(theme.altKeyTextColor)
        setTextColor(theme.keyTextColor)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        textSize = 13f
        isSingleLine = true
        padding = ctx.dp(4)
        isFocusable = true
        isFocusableInTouchMode = true
    }

    val translateButton = Button(ctx).apply {
        text = ctx.getString(R.string.translate_action)
        setTextColor(theme.keyTextColor)
        setBackgroundColor(theme.genericActiveBackgroundColor)
        textSize = 12f
        isAllCaps = false
        minHeight = ctx.dp(30)
        minimumHeight = ctx.dp(30)
    }

    val outputText = TextView(ctx).apply {
        setTextColor(theme.keyTextColor)
        setBackgroundColor(android.graphics.Color.TRANSPARENT)
        textSize = 13f
        isSingleLine = true
        padding = ctx.dp(4)
    }

    val sendButton = Button(ctx).apply {
        text = ctx.getString(R.string.translate_send)
        setTextColor(theme.keyTextColor)
        setBackgroundColor(theme.genericActiveBackgroundColor)
        textSize = 12f
        isAllCaps = false
        minHeight = ctx.dp(30)
        minimumHeight = ctx.dp(30)
    }

    private val inputRow = horizontalLayout {
        gravity = Gravity.CENTER_VERTICAL
        add(sourceInput, lParams(0, wrapContent) { weight = 1f })
        add(translateButton, lParams(wrapContent, wrapContent) {
            marginStart = ctx.dp(2)
        })
    }

    private val outputRow = horizontalLayout {
        gravity = Gravity.CENTER_VERTICAL
        add(outputText, lParams(0, wrapContent) { weight = 1f })
        add(sendButton, lParams(wrapContent, wrapContent) {
            marginStart = ctx.dp(2)
        })
    }

    override val root: View = verticalLayout {
        padding = ctx.dp(4)
        setBackgroundColor(theme.barColor)
        add(langBar, lParams(matchParent, wrapContent))
        add(View(ctx).apply {
            setBackgroundColor(theme.dividerColor)
        }, lParams(matchParent, ctx.dp(1)) {
            topMargin = ctx.dp(2)
            bottomMargin = ctx.dp(2)
        })
        add(inputRow, lParams(matchParent, wrapContent))
        add(outputRow, lParams(matchParent, wrapContent) {
            topMargin = ctx.dp(2)
        })
    }
}
