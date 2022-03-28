package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class SettingsTutorialFragment : AbstractGestureWheelTutorialFragment() {

    override val allowContextMenu: Boolean = true

    override fun onViewCreated() {
        gestures.find { it.id ==   R.id.north_container } ?.visibility = View.INVISIBLE
        message.setText(R.string.nux_settings_message)
        requireView().setOnCreateContextMenuListener { menu, v, _ ->
            menu.add(R.string.nux_settings_tutorial_settings).setOnMenuItemClickListener {
                (parentFragment as NUXHostFragment).next()
                true
            }
        }
    }

    override fun onDown() = Unit

    override fun onUp() = Unit

    override fun onAppSelected(selected: View) = Unit

    override fun onAppDeselected() = Unit
}