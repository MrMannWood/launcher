package com.mrmannwood.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.min
import kotlin.math.sqrt

class HexagonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val hexagonPath = Path()

    init {
    }

    fun setRadius(radius: Float) {
        calculatePath(radius)
    }

    private fun calculatePath(radius: Float) {
        val halfRadius = radius / 2f
        val triangleHeight = (sqrt(3.0) * halfRadius).toFloat()
        val centerX = measuredWidth / 2f
        val centerY = measuredHeight / 2f

        hexagonPath.apply {
            reset();
            moveTo(centerX, centerY + radius);
            lineTo(centerX - triangleHeight, centerY + halfRadius);
            lineTo(centerX - triangleHeight, centerY - halfRadius);
            lineTo(centerX, centerY - radius);
            lineTo(centerX + triangleHeight, centerY - halfRadius);
            lineTo(centerX + triangleHeight, centerY + halfRadius);
            close();
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        canvas.clipPath(hexagonPath)
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
        super.onDraw(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)
        setMeasuredDimension(width, height)
        calculatePath(min(width / 2f, height / 2f) - 10f)
    }

}