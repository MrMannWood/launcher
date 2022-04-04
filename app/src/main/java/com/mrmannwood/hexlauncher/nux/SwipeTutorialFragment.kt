package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class SwipeTutorialFragment : AbstractGestureWheelTutorialFragment() {

    private var selected = false

    override fun onViewCreated() {
        pushMessage(R.string.nux_swipe_message)
    }

    override fun onDown() = Unit

    override fun onUp() {
        if (selected) {
            next()
        }
    }

    override fun onAppSelected(v: View) {
        selected = true
    }

    override fun onAppDeselected() {
        selected = false
    }
}