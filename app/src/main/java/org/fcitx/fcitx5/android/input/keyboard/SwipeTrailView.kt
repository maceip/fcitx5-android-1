/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2025 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import org.fcitx.fcitx5.android.data.theme.Theme
import splitties.dimensions.dp

class SwipeTrailView(context: Context, val theme: Theme) : View(context) {

    init {
        isClickable = false
        isFocusable = false
    }

    private val path = Path()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = dp(8f).toFloat()
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        color = theme.keyTextColor // Default to theme color
        alpha = 128 // Transparent
    }

    private val trailPoints = mutableListOf<PointF>()

    fun handleTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                path.reset()
                path.moveTo(event.x, event.y)
                trailPoints.clear()
                trailPoints.add(PointF(event.x, event.y))
            }
            MotionEvent.ACTION_MOVE -> {
                path.lineTo(event.x, event.y)
                trailPoints.add(PointF(event.x, event.y))
                invalidate()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                path.reset()
                trailPoints.clear()
                invalidate()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        if (!path.isEmpty) {
            canvas.drawPath(path, paint)
        }
    }
}
