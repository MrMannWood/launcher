package com.mrmannwood.hexlauncher.nux

import android.graphics.ColorMatrixColorFilter
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mrmannwood.hexlauncher.measureScreen
import com.mrmannwood.launcher.R
import kotlin.math.pow

abstract class AbstractGestureWheelTutorialFragment : Fragment(R.layout.fragment_nux_gesture_wheel_tutorial)  {

    companion object {
        private val NEGATIVE = floatArrayOf(
            -1.0f, 0f, 0f, 0f, 255f, // red
            0f, -1.0f, 0f, 0f, 255f, // green
            0f, 0f, -1.0f, 0f, 255f, // blue
            0f, 0f, 0f, 1.0f, 0f  // alpha
        )
    }

    private var edgeExclusionZone: Int = 0
    private var screenWidth: Int = 0

    protected open val allowContextMenu: Boolean = false

    protected lateinit var gestures: List<ImageView>
    protected lateinit var message: TextView

    protected abstract fun onViewCreated()
    protected abstract fun onDown()
    protected abstract fun onUp()
    protected abstract fun onAppSelected(selected: View)
    protected abstract fun onAppDeselected()

    protected fun next() {
        (parentFragment as NUXHostFragment).next()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        edgeExclusionZone = resources.getDimension(R.dimen.edge_exclusion_zone).toInt()
        screenWidth = measureScreen(requireActivity())

        val gestureContainer = view.findViewById<View>(R.id.gesture_container)
        message = view.findViewById(R.id.nux_gesture_wheel_message)

        gestures = listOf(
            gestureContainer.findViewById(R.id.north_west_container),
            gestureContainer.findViewById(R.id.north_container),
            gestureContainer.findViewById(R.id.north_east_container),
            gestureContainer.findViewById(R.id.west_container),
            gestureContainer.findViewById(R.id.east_container),
            gestureContainer.findViewById(R.id.south_west_container),
            gestureContainer.findViewById(R.id.south_container),
            gestureContainer.findViewById(R.id.south_east_container),
        )

        view.setOnTouchListener(makeTouchListener(
            gestureContainer = gestureContainer,
            onDown = ::onDown,
            onUp = ::onUp,
            onAppSelected = ::onAppSelected,
            onAppDeselected = ::onAppDeselected
        ))

        onViewCreated()
    }

    private fun makeTouchListener(
        gestureContainer: View,
        onDown: () -> Unit,
        onUp: () -> Unit,
        onAppSelected: (selected: View) -> Unit,
        onAppDeselected: () -> Unit
    ) : View.OnTouchListener {
        val longPressTime = (ViewConfiguration.getLongPressTimeout() * 1.5).toLong()

        return object : View.OnTouchListener {

            private var showContextMenuRunnable: Runnable? = null
            private var showGestureDetailsContextMenuRunnable: Runnable? = null

            private lateinit var downPosition: PointF
            private var ignoreEvent: Boolean = false
            private var lastPosition: PointF = PointF()
            private var lastAction: Int = MotionEvent.ACTION_UP
            private var currentlyActive: ImageView? = null
            private var showingItemContextMenu = false

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                if (ignoreEvent && me.action != MotionEvent.ACTION_DOWN) {
                    return false
                }

                when (me.action) {
                    MotionEvent.ACTION_DOWN -> {
                        ignoreEvent = me.rawX > screenWidth - edgeExclusionZone
                        if (ignoreEvent) return false

                        onDown()
                        showingItemContextMenu = false
                        downPosition = PointF(me.rawX, me.rawY)
                        gestureContainer.x = me.x - gestureContainer.width / 2
                        gestureContainer.y = me.y - gestureContainer.height / 2
                        gestureContainer.visibility = View.VISIBLE
                        showContextMenuRunnable = makeShowContextMenuRunnable(gestureContainer, me.x, me.y).also {
                            view?.postDelayed(it, longPressTime)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        onUp()
                        stoppedTouchingView()
                        showContextMenuRunnable?.let { view?.removeCallbacks(it) }
                        gestureContainer.visibility = View.GONE
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentlyActive?.let { current ->
                            if (!isActive(me, current)) {
                                stoppedTouchingView()
                            }
                        }
                        if (currentlyActive == null) {
                            gestures.filter { isActive(me, it) }.forEach {
                                stoppedTouchingView()
                                startedTouchingView(it)
                            }
                        }
                    }
                }
                downPosition.let { dp ->
                    showContextMenuRunnable?.let { r ->
                        if (20 < Math.sqrt(
                                (me.rawX - dp.x).toDouble().pow(2) + (me.rawY - dp.y).toDouble()
                                    .pow(2)
                            )
                        ) {
                            view?.removeCallbacks(r)
                        }
                    }
                }
                lastAction = me.action
                lastPosition.x = me.rawX
                lastPosition.y = me.rawY
                return true
            }

            private fun makeShowContextMenuRunnable(gestureContainer: View, x: Float, y: Float) = Runnable {
                if (allowContextMenu) {
                    stoppedTouchingView()
                    view?.showContextMenu(x, y)
                    gestureContainer.visibility = View.GONE
                }
            }

            private fun makeShowGestureDetailsContextMenuRunnable(v: View) = Runnable {
                if (lastAction == MotionEvent.ACTION_UP) return@Runnable
                if (!getViewLocation(v).contains(
                        lastPosition.x.toInt(),
                        lastPosition.y.toInt()
                    )
                ) return@Runnable
                v.showContextMenu(v.width.toFloat() / 2, v.height.toFloat() / 2)
                showingItemContextMenu = true
            }

            val xy = intArrayOf(0, 0)
            val rect = Rect()
            private fun getViewLocation(v: View): Rect {
                v.getLocationOnScreen(xy)
                rect.top = xy[1]
                rect.left = xy[0]
                rect.bottom = rect.top + v.height
                rect.right = rect.left + v.width
                return rect
            }

            private fun isActive(me: MotionEvent, v: View): Boolean {
                if (v.visibility != View.VISIBLE) return false
                val rect = getViewLocation(v)
                return rect.contains(me.rawX.toInt(), me.rawY.toInt()) ||
                        doLinesIntersect(
                            me.rawX,
                            me.rawY,
                            downPosition.x,
                            downPosition.y,
                            rect.left,
                            rect.top,
                            rect.left,
                            rect.top + rect.height()
                        ) ||
                        doLinesIntersect(
                            me.rawX,
                            me.rawY,
                            downPosition.x,
                            downPosition.y,
                            rect.left,
                            rect.top,
                            rect.left + rect.width(),
                            rect.top
                        ) ||
                        doLinesIntersect(
                            me.rawX,
                            me.rawY,
                            downPosition.x,
                            downPosition.y,
                            rect.left + rect.width(),
                            rect.top,
                            rect.left + rect.width(),
                            rect.top + rect.height()
                        ) ||
                        doLinesIntersect(
                            me.rawX,
                            me.rawY,
                            downPosition.x,
                            downPosition.y,
                            rect.left,
                            rect.top + rect.height(),
                            rect.left + rect.width(),
                            rect.top + rect.height()
                        )
            }

            private fun doLinesIntersect(
                line1x1: Float,
                line1y1: Float,
                line1x2: Float,
                line1y2: Float,
                line2x1: Int,
                line2y1: Int,
                line2x2: Int,
                line2y2: Int
            ): Boolean {

                val s1_x = line1x2 - line1x1
                val s1_y = line1y2 - line1y1
                val s2_x = line2x2 - line2x1
                val s2_y = line2y2 - line2y1

                val s =
                    (-s1_y * (line1x1 - line2x1) + s1_x * (line1y1 - line2y1)) / (-s2_x * s1_y + s1_x * s2_y)
                val t =
                    (s2_x * (line1y1 - line2y1) - s2_y * (line1x1 - line2x1)) / (-s2_x * s1_y + s1_x * s2_y)

                return s >= 0 && s <= 1 && t >= 0 && t <= 1
            }

            private fun startedTouchingView(v: ImageView) {
                currentlyActive = v
                v.colorFilter = ColorMatrixColorFilter(NEGATIVE)
                showGestureDetailsContextMenuRunnable = makeShowGestureDetailsContextMenuRunnable(v)
                v.postDelayed(showGestureDetailsContextMenuRunnable, longPressTime)
                onAppSelected(v)
            }

            private fun stoppedTouchingView() {
                currentlyActive?.let { current ->
                    showGestureDetailsContextMenuRunnable?.let { current.removeCallbacks(it) }
                    current.colorFilter = null
                }
                currentlyActive = null
                onAppDeselected()
            }
        }
    }
}