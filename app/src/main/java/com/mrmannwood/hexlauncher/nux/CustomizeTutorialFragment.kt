package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class CustomizeTutorialFragment : AbstractGestureWheelTutorialFragment() {

    override fun onViewCreated() {
        message.setText(R.string.nux_customize_tutorial_message)
        gestures.filter { it.id != R.id.north_container }.forEach {
            it.setOnCreateContextMenuListener { menu, v, _ ->
                menu.add(R.string.nux_customize_tutorial_customize).setOnMenuItemClickListener {
                    next()
                    true
                }
            }
        }
    }

    override fun onDown() = Unit

    override fun onUp() = Unit

    override fun onAppSelected(selected: View) = Unit

    override fun onAppDeselected() = Unit
}