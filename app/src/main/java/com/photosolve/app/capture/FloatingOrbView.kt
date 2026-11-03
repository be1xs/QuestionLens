package com.photosolve.app.capture

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.view.View
import kotlin.random.Random

/** Translucent frosted finish without sampling or continually capturing other apps. */
class FloatingOrbView(context: Context) : View(context) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val scan = Path()
    private val grain = FloatArray(GRAIN_COUNT * 2)
    private var glass: LinearGradient? = null

    init {
        contentDescription = "点击识屏，拖动移动悬浮球"
        isClickable = true
        isFocusable = true
        val mask = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(Color.WHITE)
        }
        background = RippleDrawable(ColorStateList.valueOf(Color.argb(42, 56, 94, 136)), null, mask)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        val edge = minOf(w, h).toFloat()
        glass = LinearGradient(0f, 0f, edge, edge,
            intArrayOf(Color.argb(235, 255, 255, 255), Color.argb(185, 228, 238, 249), Color.argb(215, 245, 249, 255)),
            null, Shader.TileMode.CLAMP)
        val random = Random(29)
        repeat(GRAIN_COUNT) { index ->
            grain[index * 2] = random.nextFloat() * w
            grain[index * 2 + 1] = random.nextFloat() * h
        }
        val left = edge * 0.32f
        val right = edge * 0.68f
        val corner = edge * 0.10f
        scan.reset()
        scan.moveTo(left + corner, left); scan.lineTo(left, left); scan.lineTo(left, left + corner)
        scan.moveTo(right - corner, left); scan.lineTo(right, left); scan.lineTo(right, left + corner)
        scan.moveTo(left, right - corner); scan.lineTo(left, right); scan.lineTo(left + corner, right)
        scan.moveTo(right - corner, right); scan.lineTo(right, right); scan.lineTo(right, right - corner)
        scan.moveTo(edge * 0.43f, edge * 0.5f); scan.lineTo(edge * 0.57f, edge * 0.5f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val edge = minOf(width, height).toFloat()
        val center = edge / 2
        val radius = center - resources.displayMetrics.density * 2
        paint.style = Paint.Style.FILL
        paint.shader = null
        paint.color = Color.argb(28, 27, 43, 62)
        canvas.drawCircle(center, center + resources.displayMetrics.density, radius + resources.displayMetrics.density, paint)
        paint.color = Color.WHITE
        paint.shader = glass
        canvas.drawCircle(center, center, radius, paint)
        paint.shader = null
        paint.color = Color.argb(24, 255, 255, 255)
        paint.strokeWidth = resources.displayMetrics.density * 0.6f
        val checkpoint = canvas.save()
        canvas.clipPath(Path().apply { addCircle(center, center, radius, Path.Direction.CW) })
        canvas.drawPoints(grain, paint)
        canvas.restoreToCount(checkpoint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = resources.displayMetrics.density
        paint.color = Color.argb(210, 255, 255, 255)
        canvas.drawCircle(center, center, radius - paint.strokeWidth / 2, paint)
        paint.color = Color.rgb(48, 74, 106)
        paint.strokeWidth = resources.displayMetrics.density * 1.7f
        paint.strokeCap = Paint.Cap.ROUND
        paint.strokeJoin = Paint.Join.ROUND
        canvas.drawPath(scan, paint)
        if (isPressed) {
            paint.style = Paint.Style.FILL
            paint.color = Color.argb(25, 56, 94, 136)
            canvas.drawCircle(center, center, radius, paint)
        }
    }

    override fun setPressed(pressed: Boolean) {
        super.setPressed(pressed)
        invalidate()
    }

    private companion object { const val GRAIN_COUNT = 180 }
}
