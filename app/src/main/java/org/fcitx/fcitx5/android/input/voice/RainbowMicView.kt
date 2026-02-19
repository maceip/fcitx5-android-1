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

        // Draw the inner "bright white" mic placeholder or icon
        // For simplicity, just a white circle for now
        paint.shader = null
        paint.color = Color.WHITE
        paint.style = Paint.Style.FILL
        canvas.drawCircle(w / 2f, h / 2f, radius * 0.7f, paint)
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
