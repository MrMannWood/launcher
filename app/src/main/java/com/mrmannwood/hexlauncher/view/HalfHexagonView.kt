package com.mrmannwood.hexlauncher.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.PorterDuff
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.sqrt

class HalfHexagonView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    private val hexagonPath = Path()

    private fun calculatePath(radius: Int) {
        val halfRadius = radius / 2f
        val triangleHeight = (sqrt(3.0) * halfRadius).toFloat()
        val centerX = measuredWidth * 1f
        val centerY = measuredHeight / 2f

        hexagonPath.apply {
            reset()
            moveTo(centerX, centerY + radius)
            lineTo(centerX - triangleHeight, centerY + halfRadius)
            lineTo(centerX - triangleHeight, centerY - halfRadius)
            lineTo(centerX, centerY - radius)
            close()
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
        calculatePath(width)
    }

}