/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2026 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.ui.effects

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.os.Build
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.annotation.RequiresApi
import splitties.dimensions.dp
import kotlin.random.Random
import org.fcitx.fcitx5.android.data.prefs.AppPrefs

/**
 * AlexMack Glass Background Effect.
 * Implements a 3D Raymarched Fluted Glass effect with AGSL.
 * Includes "Mock Background" to simulate transparency.
 */
@RequiresApi(Build.VERSION_CODES.TIRAMISU)
class AlexMackBackground(context: Context) : View(context) {

    private var runtimeShader: RuntimeShader? = null
    private val paint = Paint()
    private var startTime = System.currentTimeMillis()

    private var currentBitmap: Bitmap? = null
    private var targetBitmap: Bitmap? = null
    private var transitionProgress = 1.0f
    private var currentPackage: String? = null

    // Controlled animation loop instead of invalidate() in onDraw
    private val animator = ValueAnimator.ofFloat(0f, 1f).apply {
        duration = 16_000L
        repeatCount = ValueAnimator.INFINITE
        interpolator = LinearInterpolator()
        addUpdateListener { invalidate() }
    }

    // Abstract Color-Blurred Brand Motifs
    private val brandColors = mapOf(
        "chrome" to arrayOf("#4285F4", "#EA4335", "#FBBC05", "#34A853"),
        "whatsapp" to arrayOf("#25D366", "#128C7E", "#075E54"),
        "facebook" to arrayOf("#1877F2", "#0056B3", "#FFFFFF"),
        "instagram" to arrayOf("#833AB4", "#C13584", "#FD1D1D", "#E1306C", "#FCAF45"),
        "default" to arrayOf("#1A1A1A", "#333333", "#000000")
    )

    private val shaderCode = """
        uniform float2 resolution;
        uniform float time;
        uniform shader currentBg;
        uniform shader targetBg;
        uniform float transition;
        uniform float frost_level; // (0.0 no frost, 1.0 max frost)
        uniform float dark_mode;

        // ── Standard Perlin-ish Noise ──
        float2 hash(float2 p) {
            p = float2(dot(p, float2(127.1, 311.7)), dot(p, float2(269.5, 183.3)));
            return -1.0 + 2.0 * fract(sin(p) * 43758.5453123);
        }

        float noise(float2 p) {
            float2 i = floor(p); float2 f = fract(p);
            float2 u = f * f * (3.0 - 2.0 * f);
            return mix(mix(dot(hash(i + float2(0.0, 0.0)), f - float2(0.0, 0.0)),
                           dot(hash(i + float2(1.0, 0.0)), f - float2(1.0, 0.0)), u.x),
                       mix(dot(hash(i + float2(0.0, 1.0)), f - float2(0.0, 1.0)),
                           dot(hash(i + float2(1.0, 1.0)), f - float2(1.0, 1.0)), u.x), u.y);
        }

        // ── 3D Raymarching Fluted Glass ──
        float smin(float a, float b, float k) {
            float h = clamp(0.5 + 0.5 * (b - a) / k, 0.0, 1.0);
            return mix(b, a, h) - k * h * (1.0 - h);
        }

        float sdSphere(vec3 p, float r) { return length(p) - r; }

        float scene(vec3 p) {
            float d1 = sdSphere(p - vec3(sin(time * 0.4) * 0.6, cos(time * 0.2) * 0.3, 0.0), 0.45);
            float d2 = sdSphere(p - vec3(cos(time * 0.3) * 0.5, sin(time * 0.5) * 0.2, 0.0), 0.35);
            return smin(d1, d2, 0.6);
        }

        vec3 getNormal(vec3 p) {
            vec2 e = vec2(0.005, 0.0);
            return normalize(vec3(scene(p + e.xyy) - scene(p - e.xyy),
                                  scene(p + e.yxy) - scene(p - e.yxy),
                                  scene(p + e.yyx) - scene(p - e.yyx)));
        }

        half4 main(float2 fragCoord) {
            float2 uv = (fragCoord - 0.5 * resolution) / resolution.y;

            // 1. Raymarching
            vec3 ro = vec3(0.0, 0.0, 2.0);
            vec3 rd = normalize(vec3(uv, -1.0));
            vec3 rp = ro;
            bool hit = false;
            for(int i = 0; i < 28; i++) {
                float dist = scene(rp);
                rp += rd * dist;
                if(dist < 0.01) { hit = true; break; }
            }

            // 2. Refract backgrounds
            float2 bgUv = fragCoord; // Start with screen coordinate
            if (hit) {
                vec3 n = getNormal(rp);
                // Calculate refraction direction based on normal and frost level
                // The 0.06 was a fixed refraction amount, now it's scaled by frost_level
                float2 refractDir = n.xy; // Simplified refraction direction for 2D offset
                bgUv += refractDir * (50.0 * frost_level); // Apply frost_level to the refraction amount
            }
            // Vertical flute ribs
            bgUv.x += sin(fragCoord.x * 0.25) * 0.0015;

            // Constrain coordinates to standard screen space bounds
            bgUv = clamp(bgUv, vec2(0.0), resolution);

            half4 c1 = currentBg.eval(bgUv);
            half4 c2 = targetBg.eval(bgUv);
            half4 bg = mix(c1, c2, transition);

            // 3. Shading
            float3 finalColor = bg.rgb;
            if (hit) {
                vec3 n = getNormal(rp);
                vec3 lightDir = normalize(vec3(1.0, 1.5, 1.0));
                float spec = pow(max(0.0, dot(reflect(-lightDir, n), vec3(0.0, 0.0, 1.0))), 48.0);

                // AlexMack Emerald Highlight
                vec3 highlight = mix(vec3(0.3, 0.85, 0.45), vec3(0.15, 0.35, 0.9), dark_mode);
                finalColor += highlight * spec * 0.5;
                vec3 shadow = mix(vec3(0.0, 0.15, 0.04), vec3(0.0, 0.01, 0.03), dark_mode);
                finalColor = mix(finalColor, shadow, 0.15);
            } else {
                finalColor *= 0.98 + 0.02 * sin(fragCoord.x * 0.4);
            }
            if (dark_mode > 0.5) {
                finalColor *= 0.25; // Darken global scene
            }

            return half4(finalColor, 1.0);
        }
    """.trimIndent()

    init {
        runtimeShader = RuntimeShader(shaderCode)
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (visibility == VISIBLE) animator.start()
    }

    override fun onDetachedFromWindow() {
        animator.cancel()
        super.onDetachedFromWindow()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        if (visibility == VISIBLE && isAttachedToWindow) {
            if (!animator.isRunning) animator.start()
        } else {
            animator.cancel()
        }
    }

    fun setAppBackground(packageName: String?) {
        if (currentPackage == packageName && targetBitmap != null) return

        // Handle rapid transitions — recycle old bitmap to prevent leaks
        val old = currentBitmap
        if (targetBitmap != null) {
            currentBitmap = targetBitmap
        }
        if (old != null && old !== currentBitmap) {
            old.recycle()
        }

        currentPackage = packageName
        targetBitmap = generateBrandMotif(packageName)

        // Subtly animate transition
        ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 800
            addUpdateListener {
                transitionProgress = it.animatedValue as Float
                invalidate()
            }
            start()
        }
    }

    private fun generateBrandMotif(packageName: String?): Bitmap {
        val width = width.takeIf { it > 0 } ?: dp(360).toInt()
        val height = height.takeIf { it > 0 } ?: dp(300).toInt()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val colors = when {
            packageName?.contains("chrome") == true -> brandColors["chrome"]
            packageName?.contains("whatsapp") == true -> brandColors["whatsapp"]
            packageName?.contains("facebook") == true -> brandColors["facebook"]
            packageName?.contains("instagram") == true -> brandColors["instagram"]
            else -> brandColors["default"]
        } ?: brandColors["default"]!!

        // Seeded random based on package name for deterministic blob positions
        val rng = Random(packageName?.hashCode() ?: 0)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Draw abstract blurred gradients instead of UI components
        if (colors.size >= 3) {
            // Three-color gradient base
            val gradient = LinearGradient(0f, 0f, width.toFloat(), height.toFloat(),
                colors.map { Color.parseColor(it) }.toIntArray(),
                null, Shader.TileMode.CLAMP)
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

            // Add some "blurred blobs" for depth
            paint.shader = null
            colors.forEachIndexed { i, color ->
                paint.color = Color.parseColor(color)
                paint.alpha = 40
                val rx = (width * (0.2 + 0.6 * rng.nextDouble())).toFloat()
                val ry = (height * (0.2 + 0.6 * rng.nextDouble())).toFloat()
                canvas.drawCircle(rx, ry, width * 0.4f, paint)
            }
        } else {
            canvas.drawColor(Color.parseColor(colors[0]))
        }

        // Apply heavy box blur to keep it "light touch" and abstract
        return blurBitmap(bitmap)
    }

    private fun blurBitmap(bitmap: Bitmap): Bitmap {
        // Simple scale-down scale-up blur is efficient and gives that 'color wash' look
        val scaledDown = Bitmap.createScaledBitmap(bitmap, 64, 64, true)
        val result = Bitmap.createScaledBitmap(scaledDown, bitmap.width, bitmap.height, true)
        scaledDown.recycle()
        bitmap.recycle()
        return result
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (currentPackage != null) {
            currentBitmap?.recycle()
            targetBitmap?.recycle()
            currentBitmap = null
            targetBitmap = generateBrandMotif(currentPackage)
        }
    }

    override fun onDraw(canvas: Canvas) {
        val shader = runtimeShader ?: return
        val current = currentBitmap ?: targetBitmap ?: return
        val target = targetBitmap ?: return

        val time = (System.currentTimeMillis() - startTime) / 1000f
        shader.setFloatUniform("resolution", width.toFloat(), height.toFloat())
        shader.setFloatUniform("time", time)
        shader.setFloatUniform("transition", transitionProgress)

        // Pass frost level scaled to a 0.0 -> 1.0 float (0-100 from prefs -> 0.0-1.0 for shader)
        val frostValue = AppPrefs.getInstance().keyboard.glassFrostLevel.getValue() / 100f
        shader.setFloatUniform("frost_level", frostValue)
        
        val darkModeValue = if (AppPrefs.getInstance().keyboard.glassDarkMode.getValue()) 1.0f else 0.0f
        shader.setFloatUniform("dark_mode", darkModeValue)

        shader.setInputShader("currentBg", BitmapShader(current, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))
        shader.setInputShader("targetBg", BitmapShader(target, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP))

        paint.shader = shader
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
        // No invalidate() here — animation is driven by the ValueAnimator
    }
}
