package com.mrmannwood.hexlauncher.view

import android.graphics.Rect
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import androidx.core.util.forEach
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.sqrt

/**
 * Heavily used as a reference:
 * https://github.com/devunwired/recyclerview-playground/blob/299515e0cfe4caea78eaf7ba12f7c9cf926b6063/app/src/main/java/com/example/android/recyclerplayground/layout/FixedGridLayoutManager.java#L316
 */
class HexagonalGridLayoutManager(private val corner: Corner): RecyclerView.LayoutManager() {

    enum class Corner(val top: Boolean, val left: Boolean) {
        TOP_LEFT(true, true),
        TOP_RIGHT(true, false),
        BOTTOM_LEFT(false, true),
        BOTTOM_RIGHT(false, false)
    }

    private var positions =  Array(8) { Rect(-1, -1, -1, -1) }
    private var viewCache = SparseArray<View>(positions.size)

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        return RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun canScrollHorizontally() = false
    override fun canScrollVertically() = false
    override fun supportsPredictiveItemAnimations() = false

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        val itemCount = itemCount
        if (itemCount == 0) {
            detachAndScrapAttachedViews(recycler)
            return
        }
        if (childCount == 0 && state.isPreLayout) return

        if (childCount == 0) {
            //first or empty layout, remeasure
            initPositions(recycler)
            recycler.setViewCacheSize(positions.size)
        } else {
            for (i in 0 until childCount) viewCache.put(i, getChildAt(i))
            viewCache.forEach { _, view -> detachView(view) }
        }

        positions.forEachIndexed { idx, rect ->
            if (idx >= itemCount) return@forEachIndexed

            var view = viewCache.get(idx)
            if (view == null) {
                view = recycler.getViewForPosition(idx)
                addView(view)
                measureChildWithMargins(view, 0, 0)
                layoutDecorated(view, rect.left, rect.top, rect.right, rect.bottom)
            } else {
                recycler.bindViewToPosition(view, idx)
                attachView(view)
                viewCache.remove(idx)
            }
        }

        viewCache.forEach { _, view -> recycler.recycleView(view) }
        viewCache.clear()
    }

    private fun initPositions(recycler: RecyclerView.Recycler) {

        val scrap = recycler.getViewForPosition(0)
        addView(scrap)
        measureChildWithMargins(scrap, 0, 0)

        val decoratedWidth = getDecoratedMeasuredWidth(scrap)
        val decoratedHeight = getDecoratedMeasuredHeight(scrap)

        val verticalOffset = getVerticalOffset(decoratedHeight)
        val horizontalOffset = getHorizontalOffset(decoratedHeight)

        detachAndScrapView(scrap, recycler)

        var current = positions[0]
        current.bottom = getStartingYPosition(decoratedHeight)
        current.right = getStartingXPosition(decoratedWidth)

        current = positions[1]
        current.bottom = positions[0].bottom - verticalOffset
        current.right = positions[0].right - horizontalOffset

        current = positions[2]
        current.bottom = positions[0].bottom
        current.right = positions[0].right - horizontalOffset * 2

        current = positions[3]
        current.bottom = positions[1].bottom - verticalOffset
        current.right = positions[0].right

        current = positions[4]
        current.bottom = positions[1].bottom - verticalOffset
        current.right = positions[1].right - horizontalOffset

        current = positions[5]
        current.bottom = positions[1].bottom
        current.right = positions[1].right - horizontalOffset * 2

        current = positions[6]
        current.bottom = positions[0].bottom
        current.right = positions[2].right - horizontalOffset * 2

        current = positions[7]
        current.bottom = positions[3].bottom - verticalOffset
        current.right = positions[1].right

        positions.forEach {
            it.top = it.bottom - decoratedHeight
            it.left = it.right - decoratedWidth
        }
    }

    private fun getStartingYPosition(height: Int): Int {
        return if (corner.top) height else getVerticalSpace()
    }

    private fun getStartingXPosition(width: Int): Int {
        return if (corner.left) width else getHorizontalSpace()
    }

    private fun getHorizontalSpace() = width - paddingLeft - paddingRight

    private fun getVerticalSpace() = height - paddingTop - paddingBottom

    private fun getVerticalOffset(height: Int): Int {
        val sideLength = height / 2
        val offset = height - sideLength / 2
        return if (corner.top) -1 * offset else offset
    }

    private fun getHorizontalOffset(height: Int): Int {
        val sideLength = height / 2
        val offset = (sqrt(3.0) / 2 * sideLength).toInt()
        return if (corner.left) -1 * offset else offset
    }
}