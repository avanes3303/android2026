package com.example.myapplication.presentation.common

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.example.myapplication.R

class CircularProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var progress: Int = 0
    private var trackColor: Int = 0
    private var strokeWidth: Float = 0f

    private val arcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private val trackPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
    }

    private val arcRect = RectF()

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.CircularProgressView, defStyleAttr, 0)
        progress = a.getInt(R.styleable.CircularProgressView_progress, 0)
        trackColor = a.getColor(
            R.styleable.CircularProgressView_trackColor,
            ContextCompat.getColor(context, R.color.on_surface_variant)
        )
        strokeWidth = a.getDimension(
            R.styleable.CircularProgressView_strokeWidth,
            resources.getDimension(R.dimen.circular_progress_stroke)
        )
        a.recycle()

        trackPaint.strokeWidth = strokeWidth
        trackPaint.color = trackColor
        arcPaint.strokeWidth = strokeWidth
        textPaint.textSize = resources.getDimension(R.dimen.circular_progress_text)
    }

    fun setProgress(value: Int) {
        progress = value.coerceIn(0, 100)
        arcPaint.color = progressColor()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val desiredSize = resources.getDimensionPixelSize(R.dimen.circular_progress_size)
        val width = resolveSize(desiredSize, widthMeasureSpec)
        val height = resolveSize(desiredSize, heightMeasureSpec)
        val size = minOf(width, height)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        val half = strokeWidth / 2f
        arcRect.set(half, half, width - half, height - half)

        // 1. Фоновый трек (полный круг)
        canvas.drawArc(arcRect, 0f, 360f, false, trackPaint)

        // 2. Дуга прогресса
        arcPaint.color = progressColor()
        val sweepAngle = progress * 3.6f
        canvas.drawArc(arcRect, -90f, sweepAngle, false, arcPaint)

        // 3. Текст процента по центру
        textPaint.color = arcPaint.color
        val text = "$progress%"
        val x = width / 2f
        val y = height / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(text, x, y, textPaint)
    }

    private fun progressColor(): Int = when {
        progress >= 80 -> ContextCompat.getColor(context, R.color.completion_high)
        progress >= 50 -> ContextCompat.getColor(context, R.color.completion_medium)
        else -> ContextCompat.getColor(context, R.color.completion_low)
    }
}
