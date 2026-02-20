/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.voice

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp

class RainbowMicView(context: Context, val theme: Theme) : View(context) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(4f).toFloat()
    }

    private var rotationAngle = 0f
    private val animator = ValueAnimator.ofFloat(0f, 360f).apply {
        duration = 2000
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener {
            rotationAngle = it.animatedValue as Float
            invalidate()
        }
    }

    private val rainbowColors = intArrayOf(
        Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
    )

    private var gradient: SweepGradient? = null
    
    // Load the actual microphone drawable instead of a placeholder circle
    private val micDrawable = ContextCompat.getDrawable(context, R.drawable.ic_baseline_keyboard_voice_24)?.apply {
        // Tint the microphone to match the active keyboard text color
        colorFilter = PorterDuffColorFilter(theme.keyTextColor, PorterDuff.Mode.SRC_IN)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradient = SweepGradient(w / 2f, h / 2f, rainbowColors, null)
    }

    override fun onDraw(canvas: Canvas) {
        val w = width.toFloat()
        val h = height.toFloat()
        val radius = (Math.min(w, h) / 2f) - paint.strokeWidth

        canvas.save()
        canvas.rotate(rotationAngle, w / 2f, h / 2f)
        paint.shader = gradient
        canvas.drawCircle(w / 2f, h / 2f, radius, paint)
        canvas.restore()

        // Draw the mic icon in the center, scaled to fit comfortably within the radius
        micDrawable?.let { drawable ->
            val iconSize = (radius * 1.2f).toInt()
            val left = ((w - iconSize) / 2f).toInt()
            val top = ((h - iconSize) / 2f).toInt()
            drawable.setBounds(left, top, left + iconSize, top + iconSize)
            drawable.draw(canvas)
        }
    }

    fun startAnimation() {
        if (!animator.isRunning) animator.start()
        visibility = VISIBLE
    }

    fun stopAnimation() {
        animator.cancel()
        visibility = GONE
    }
}
