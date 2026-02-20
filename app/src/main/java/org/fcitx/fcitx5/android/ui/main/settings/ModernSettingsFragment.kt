/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.main.settings

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.RippleDrawable
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.ui.effects.AlexMackBackground
import splitties.dimensions.dp

class ModernSettingsFragment : Fragment() {

    private val prefs by lazy { AppPrefs.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val ctx = requireContext()
        val root = FrameLayout(ctx).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        }

        // Apply Glass Background if supported
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            root.addView(AlexMackBackground(ctx).apply {
                setAppBackground(ctx.packageName)
            }, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        } else {
            root.setBackgroundColor(0xFF121212.toInt())
        }

        val scroll = ScrollView(ctx).apply {
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        }
        root.addView(scroll)

        val content = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ctx.dp(16), ctx.dp(16), ctx.dp(16), ctx.dp(16))
            layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        }
        scroll.addView(content)

        // Header
        content.addView(TextView(ctx).apply {
            text = "FCITX COMPONENT REGISTRY"
            setTextColor(0xFF4CAF50.toInt())
            textSize = 12f
            typeface = Typeface.MONOSPACE
            gravity = Gravity.CENTER
            setPadding(0, ctx.dp(16), 0, ctx.dp(16))
        })

        content.addView(TextView(ctx).apply {
            text = "SYSTEM ARCHITECTURE"
            setTextColor(Color.WHITE)
            textSize = 24f
            typeface = Typeface.create("sans-serif-thin", Typeface.NORMAL)
            setPadding(0, ctx.dp(16), 0, ctx.dp(16))
        })

        // SENSORY Section
        addCategory(content, "01 SENSORY", listOf(
            SettingItem("Haptic Feedback", "Mechanical key press simulation") {},
            SettingItem("Vibration Intensity", "Industrial haptic engine strength") {}
        ))

        // INTELLIGENCE Section
        addCategory(content, "02 INTELLIGENCE", listOf(
            SettingItem("Neural Translation", "On-device ML translation module") {}
        ))

        // PRESENTATION Section
        addCategory(content, "03 PRESENTATION", listOf(
            SettingItem("Glass Morphism", "AlexMack refraction shader") {
                prefs.keyboard.glassKeyboard.setValue(!prefs.keyboard.glassKeyboard.getValue())
            },
            SettingItem("Rainbow Microphone", "Dynamic audio visualization") {
                prefs.keyboard.rainbowMicEnabled.setValue(!prefs.keyboard.rainbowMicEnabled.getValue())
            }
        ))

        content.addView(View(ctx).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ctx.dp(100))
        })

        return root
    }

    private fun addCategory(parent: LinearLayout, title: String, items: List<SettingItem>) {
        val ctx = parent.context
        parent.addView(TextView(ctx).apply {
            text = title
            setTextColor(0xFF00E676.toInt())
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setPadding(ctx.dp(8), ctx.dp(24), ctx.dp(8), ctx.dp(8))
        })

        items.forEach { item ->
            parent.addView(createItemView(ctx, item), LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                topMargin = ctx.dp(8)
            })
        }
    }

    private fun createItemView(ctx: Context, item: SettingItem): View {
        return LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0x1AFFFFFF.toInt())
            setPadding(ctx.dp(12), ctx.dp(12), ctx.dp(12), ctx.dp(12))
            isClickable = true
            isFocusable = true
            background = RippleDrawable(
                android.content.res.ColorStateList.valueOf(Color.WHITE),
                null,
                ColorDrawable(Color.WHITE)
            )
            
            addView(TextView(ctx).apply {
                text = item.title
                setTextColor(Color.WHITE)
                textSize = 16f
            })

            addView(TextView(ctx).apply {
                text = item.description
                setTextColor(0xAAFFFFFF.toInt())
                textSize = 12f
            })
            
            setOnClickListener { item.onClick() }
        }
    }

    data class SettingItem(
        val title: String,
        val description: String,
        val onClick: () -> Unit
    )
}
