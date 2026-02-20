package org.fcitx.fcitx5.android.input

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
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

class QuickSettingsUi(
    override val ctx: Context,
    private val theme: Theme,
    val onThemeClick: () -> Unit,
    val onClipboardClick: () -> Unit,
    val onSettingsClick: () -> Unit,
    var onCloseClick: () -> Unit
) : Ui {

    private fun squircle(color: Int, radius: Float = 12f) = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = ctx.dp(radius).toFloat()
        setColor(color)
    }

    private fun createGridButton(iconResId: Int, labelResId: Int, onClick: () -> Unit): View {
        return verticalLayout {
            gravity = Gravity.CENTER
            val iconBtn = ImageButton(ctx).apply {
                setImageResource(iconResId)
                setColorFilter(theme.keyTextColor)
                background = squircle(theme.keyBackgroundColor, 16f)
                setPadding(dp(16), dp(16), dp(16), dp(16))
                setOnClickListener { onClick() }
                elevation = dp(2).toFloat()
            }
            val label = TextView(ctx).apply {
                setText(labelResId)
                setTextColor(theme.keyTextColor)
                textSize = 12f
                gravity = Gravity.CENTER
                setPadding(0, dp(8), 0, 0)
            }
            add(iconBtn, lParams(dp(56), dp(56)))
            add(label, lParams(wrapContent, wrapContent))
        }
    }

    val closeButton = ImageButton(ctx).apply {
        setImageResource(R.drawable.ic_baseline_close_24)
        setColorFilter(theme.keyTextColor)
        background = squircle(Color.TRANSPARENT)
        setOnClickListener { onCloseClick() }
    }

    private val topBar = horizontalLayout {
        gravity = Gravity.CENTER_VERTICAL
        padding = dp(8)
        
        val title = TextView(ctx).apply {
            text = "Quick Settings"
            setTextColor(theme.keyTextColor)
            textSize = 16f
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            gravity = Gravity.CENTER_VERTICAL
        }
        
        add(title, lParams(0, wrapContent) { weight = 1f })
        add(closeButton, lParams(wrapContent, wrapContent))
    }

    private val gridLayout = horizontalLayout {
        gravity = Gravity.CENTER
        
        add(createGridButton(R.drawable.ic_baseline_palette_24, R.string.theme, onThemeClick), lParams { weight = 1f })
        add(createGridButton(R.drawable.ic_baseline_content_paste_24, R.string.clipboard, onClipboardClick), lParams { weight = 1f })
        add(createGridButton(R.drawable.ic_baseline_settings_24, R.string.advanced, onSettingsClick), lParams { weight = 1f })
    }

    override val root = verticalLayout {
        // Obscure everything behind it using an opaque tinted altKey background
        background = squircle(theme.altKeyBackgroundColor, 0f)
        isClickable = true
        isFocusable = true
        
        add(topBar, lParams(matchParent, wrapContent))
        add(gridLayout, lParams(matchParent, matchParent))
    }
}
