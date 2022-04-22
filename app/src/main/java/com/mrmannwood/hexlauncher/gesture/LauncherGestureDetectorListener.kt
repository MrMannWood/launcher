package com.mrmannwood.hexlauncher.gesture

import android.view.GestureDetector
import android.view.MotionEvent
import kotlin.math.abs

class LauncherGestureDetectorListener(
    private val listener: GestureListener
) : GestureDetector.SimpleOnGestureListener() {

    interface GestureListener {
        fun onSwipeLeft()
        fun onSwipeRight()
        fun onSwipeUp()
        fun onSwipeDown()
        fun onLongPress(x: Float, y: Float)
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        if (abs(velocityX) > abs(velocityY)) {
            // horizontal
            if (velocityX < 0) {
                listener.onSwipeLeft()
            } else {
                listener.onSwipeRight()
            }
            return true
        } else if (abs(velocityY) > abs(velocityX)) {
            // vertical
            if (velocityY < 0) {
                listener.onSwipeUp()
            } else {
                listener.onSwipeDown()
            }
            return true
        }
        return super.onFling(e1, e2, velocityX, velocityY)
    }

    override fun onLongPress(e: MotionEvent) {
        super.onLongPress(e)
        listener.onLongPress(e.x, e.y)
    }
}
