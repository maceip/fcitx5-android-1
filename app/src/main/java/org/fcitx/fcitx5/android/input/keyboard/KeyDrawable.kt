/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2024 Fcitx5 for Android Contributors
 */

package org.fcitx.fcitx5.android.input.keyboard

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.graphics.drawable.LayerDrawable
import androidx.annotation.ColorInt

fun radiusDrawable(
    r: Float, @ColorInt
    color: Int = Color.WHITE
): Drawable = GradientDrawable().apply {
    setColor(color)
    cornerRadius = r
}

fun insetRadiusDrawable(
    hInset: Int,
    vInset: Int,
    r: Float = 0f,
    @ColorInt color: Int = Color.WHITE
): Drawable = InsetDrawable(
    radiusDrawable(r, color),
    hInset, vInset, hInset, vInset
)

fun insetOvalDrawable(
    hInset: Int,
    vInset: Int,
    @ColorInt color: Int = Color.WHITE
): Drawable = InsetDrawable(
    GradientDrawable().apply {
        shape = GradientDrawable.OVAL
        setColor(color)
    },
    hInset, vInset, hInset, vInset
)

fun shadowedKeyBackgroundDrawable(
    @ColorInt bkgColor: Int,
    @ColorInt shadowColor: Int,
    radius: Float,
    shadowWidth: Int,
    hMargin: Int,
    vMargin: Int
): Drawable = LayerDrawable(
    arrayOf(
        radiusDrawable(radius, shadowColor),
        radiusDrawable(radius, bkgColor),
    )
).apply {
    setLayerInset(0, hMargin, vMargin, hMargin, vMargin - shadowWidth)
    setLayerInset(1, hMargin, vMargin, hMargin, vMargin)
}

fun borderedKeyBackgroundDrawable(
    @ColorInt bkgColor: Int,
    @ColorInt shadowColor: Int,
    radius: Float,
    strokeWidth: Int,
    hMargin: Int,
    vMargin: Int
): Drawable = LayerDrawable(
    arrayOf(
        GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = radius
            setColor(bkgColor)
            setStroke(strokeWidth, shadowColor)
        }
    )
).apply {
    setLayerInset(0, hMargin, vMargin, hMargin, vMargin)
}

fun glassBubbleBackgroundDrawable(
    smokeMode: Boolean,
    radius: Float,
    hMargin: Int,
    vMargin: Int
): Drawable {
    // Determine the base inner fill based on "smoke mode"
    val fillColor = if (smokeMode) Color.parseColor("#44001122") else Color.parseColor("#22FFFFFF")
    
    // The main glassy body
    val body = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        setColor(fillColor)
        
        // Emulate the bright specular rim lighting seen on glass bubbles
        setStroke(2, Color.parseColor("#88FFFFFF"))
    }
    
    // The top reflection highlight (simulating the bubble volume)
    val highlightColor = if (smokeMode) Color.parseColor("#33FFFFFF") else Color.parseColor("#66FFFFFF")
    val highlight = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        // Top-down gradient fading out
        colors = intArrayOf(highlightColor, Color.TRANSPARENT, Color.TRANSPARENT)
        orientation = GradientDrawable.Orientation.TOP_BOTTOM
    }

    return LayerDrawable(arrayOf(body, highlight)).apply {
        setLayerInset(0, hMargin, vMargin, hMargin, vMargin)
        // Inset the highlight slightly to sit "inside" the bubble's rim
        setLayerInset(1, hMargin + 4, vMargin + 4, hMargin + 4, vMargin + 20)
    }
}

fun glassBubbleLedHighlightDrawable(
    @ColorInt color: Int,
    radius: Float,
    hMargin: Int,
    vMargin: Int,
    scopeDrawable: Drawable? = null
): Drawable {
    val highlight = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        cornerRadius = radius
        gradientType = GradientDrawable.RADIAL_GRADIENT
        // Adjust radial radius based on assumed typical key sizes, fading out.
        gradientRadius = 150f 
        colors = intArrayOf(color, Color.TRANSPARENT)
    }
    
    val layers = if (scopeDrawable != null) {
        arrayOf(scopeDrawable, highlight)
    } else {
        arrayOf(highlight)
    }
    
    return LayerDrawable(layers).apply {
        if (scopeDrawable != null) {
            // Center the scope icon within the key bounds, with a bit of padding so it's not massive
            setLayerInset(0, hMargin + 32, vMargin + 8, hMargin + 32, vMargin + 8)
            setLayerInset(1, hMargin, vMargin, hMargin, vMargin)
        } else {
            setLayerInset(0, hMargin, vMargin, hMargin, vMargin)
        }
    }
}
