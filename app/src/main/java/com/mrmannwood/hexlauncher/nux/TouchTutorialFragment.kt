package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class TouchTutorialFragment : AbstractGestureWheelTutorialFragment() {

    private var down = false
    private var appSelected = false

    override fun onViewCreated() {
        message.setText(R.string.nux_touch_tutorial_message)
        floatingMessage.setText(R.string.nux_touch_tutorial_message_2)
        gestures.find { it.id ==   R.id.north_container } ?.visibility = View.INVISIBLE
    }

    override fun onDown() {
        down = true
        message.visibility = View.INVISIBLE
        floatingMessage.visibility = View.VISIBLE
    }

    override fun onUp() {
        down = false
        if (!appSelected) {
            message.visibility = View.VISIBLE
            floatingMessage.visibility = View.INVISIBLE
        } else {
            next()
        }
    }

    override fun onAppSelected(selected: View) {
        if (selected.id == R.id.north_container) {
            return
        }
        appSelected = true
        floatingMessage.setText(R.string.nux_touch_tutorial_message_3)
    }

    override fun onAppDeselected() {
        appSelected = false
        if (down) {
            floatingMessage.setText(R.string.nux_touch_tutorial_message_2)
        }
    }
}