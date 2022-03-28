package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class TouchTutorialFragment : AbstractGestureWheelTutorialFragment() {

    private var down = false
    private var appSelected = false

    override fun onViewCreated() {
        message.setText(R.string.nux_touch_tutorial_message)
    }

    override fun onDown() {
        down = true
        message.setText(R.string.nux_touch_tutorial_message_2)
    }

    override fun onUp() {
        down = false
        if (!appSelected) {
            message.setText(R.string.nux_touch_tutorial_message)
        } else {
            next()
        }
    }

    override fun onAppSelected(selected: View) {
        if (selected.id == R.id.north_container) {
            return
        }
        appSelected = true
        message.setText(R.string.nux_touch_tutorial_message_3)
    }

    override fun onAppDeselected() {
        appSelected = false
        if (down) {
            message.setText(R.string.nux_touch_tutorial_message_2)
        }
    }
}