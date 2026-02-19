/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.bar.ui.idle

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp
import splitties.views.dsl.core.Ui
import splitties.views.dsl.core.add
import splitties.views.dsl.core.horizontalMargin
import splitties.views.dsl.core.lParams
import splitties.views.dsl.core.matchParent
import splitties.views.dsl.core.textView
import splitties.views.dsl.core.wrapContent
import splitties.views.horizontalPadding
import splitties.views.onClick
import org.fcitx.fcitx5.android.utils.borderlessRippleDrawable

class PredictionSuggestionsUi(override val ctx: Context, private val theme: Theme) : Ui {

    var onSuggestionClick: ((String) -> Unit)? = null

    private val container = LinearLayout(ctx).apply {
        orientation = LinearLayout.HORIZONTAL
        gravity = Gravity.CENTER_VERTICAL
        horizontalPadding = dp(8)
    }

    override val root: View = container

    fun setSuggestions(suggestions: List<String>) {
        container.removeAllViews()
        suggestions.forEach { suggestion ->
            container.add(textView {
                text = suggestion
                setTextColor(theme.candidateTextColor)
                textSize = 14f
                gravity = Gravity.CENTER
                horizontalPadding = dp(12)
                background = borderlessRippleDrawable(theme.keyPressHighlightColor, ctx.dp(20))
                onClick { onSuggestionClick?.invoke(suggestion) }
            }, LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT).apply {
                horizontalMargin = ctx.dp(4)
            })
        }
    }
}
