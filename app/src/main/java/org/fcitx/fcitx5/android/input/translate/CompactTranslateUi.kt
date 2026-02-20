/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.translate

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
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

    private fun squircle(color: Int, radius: Float = 8f) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = ctx.dp(radius).toFloat()
        setColor(color)
    }

    private fun borderSquircle(color: Int, borderColor: Int, radius: Float = 24f) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = ctx.dp(radius).toFloat()
        setColor(color)
        setStroke(ctx.dp(1), borderColor)
    }

    val sourceLangButton = Button(ctx).apply {
        text = "EN"
        setTextColor(theme.keyTextColor)
        background = squircle(theme.keyBackgroundColor, 16f)
        textSize = 12f
        isAllCaps = false
        minHeight = dp(36)
        minimumHeight = dp(36)
    }

    val swapButton = ImageButton(ctx).apply {
        setImageResource(R.drawable.ic_baseline_swap_horiz_24)
        setColorFilter(theme.keyTextColor)
        background = squircle(Color.TRANSPARENT)
        contentDescription = ctx.getString(R.string.translate_swap)
        setPadding(dp(4), dp(4), dp(4), dp(4))
    }

    val targetLangButton = Button(ctx).apply {
        text = "ES"
        setTextColor(theme.keyTextColor)
        background = squircle(theme.keyBackgroundColor, 16f)
        textSize = 12f
        isAllCaps = false
        minHeight = dp(36)
        minimumHeight = dp(36)
    }

    val closeButton = ImageButton(ctx).apply {
        setImageResource(R.drawable.ic_baseline_arrow_back_24)
        setColorFilter(theme.keyTextColor)
        background = squircle(theme.keyBackgroundColor, 24f) // Fully rounded circle
        contentDescription = ctx.getString(R.string.back_to_keyboard)
        elevation = dp(2).toFloat()
    }

    val sourceInput = EditText(ctx).apply {
        hint = ctx.getString(R.string.translate_source_hint)
        setHintTextColor(theme.altKeyTextColor)
        setTextColor(theme.keyTextColor)
        // 1px border pill shape
        background = borderSquircle(theme.keyBackgroundColor, theme.dividerColor, 24f)
        textSize = 14f
        isSingleLine = true
        setPadding(dp(16), dp(8), dp(16), dp(8))
        isFocusable = true
        isFocusableInTouchMode = true
    }

    override val root: View = horizontalLayout {
        padding = dp(8)
        gravity = Gravity.CENTER_VERTICAL
        
        add(closeButton, lParams(dp(40), dp(40)))
        
        add(sourceInput, lParams(0, dp(40)) {
            weight = 1f
            marginStart = dp(8)
            marginEnd = dp(8)
        })
        
        add(sourceLangButton, lParams(wrapContent, dp(36)))
        add(swapButton, lParams(dp(32), dp(32)))
        add(targetLangButton, lParams(wrapContent, dp(36)))
    }
}
