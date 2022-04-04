package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrmannwood.launcher.R

class TouchTutorialFragment : AbstractGestureWheelTutorialFragment() {

    private interface TouchTutorial {
        var appSelected: Boolean
        fun onBegin()
        fun onDown()
        fun onUp()
        fun onAppSelected(selected: View)
        fun onAppDeselected()
    }

    private val tutorials = listOf(
        object : TouchTutorial {
            override var appSelected: Boolean = false

            override fun onBegin() {
                pushMessage(R.string.nux_touch_tutorial_message)
                gestures.find { it.id ==   R.id.north_container } ?.visibility = View.INVISIBLE
            }

            override fun onDown() {
                pushMessage(R.string.nux_touch_tutorial_message_2)
            }

            override fun onUp() {
                if (appSelected) {
                    nextTutorial()
                } else {
                    popMessage()
                }
            }

            override fun onAppSelected(selected: View) {
                if (selected.id == R.id.north_container) {
                    return
                }
                pushMessage(R.string.nux_touch_tutorial_message_3)
            }

            override fun onAppDeselected() {
                popMessage()
            }
        },
        object : TouchTutorial {
            override var appSelected: Boolean = false
            var messageShown = false

            override fun onBegin() {
                gestures.find { it.id == R.id.north_container } ?.visibility = View.INVISIBLE
                pushMessage(R.string.nux_touch_tutorial_message)
                gestures.filter { it.id != R.id.north_container }.forEach {
                    it.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.add(R.string.nux_customize_tutorial_customize).setOnMenuItemClickListener {
                            nextTutorial()
                            true
                        }
                    }
                }
            }

            override fun onDown() {
                if (!messageShown) {
                    pushMessage(R.string.nux_customize_tutorial_message)
                    messageShown = true
                }
            }

            override fun onUp() {
                if (!appSelected) {
                    popMessage()
                    messageShown = false
                }
            }

            override fun onAppSelected(selected: View) = Unit

            override fun onAppDeselected() = Unit
        }
    )

    private var tutorial: Int = 0

    override fun onViewCreated() {
        tutorials[tutorial].onBegin()
    }

    override fun onDown() {
        tutorials[tutorial].onDown()
    }

    override fun onUp() {
        tutorials[tutorial].onUp()
    }

    override fun onAppSelected(selected: View) {
        tutorials[tutorial].appSelected = true
        tutorials[tutorial].onAppSelected(selected)
    }

    override fun onAppDeselected() {
        tutorials[tutorial].appSelected = false
        tutorials[tutorial].onAppDeselected()
    }

    fun nextTutorial() {
        tutorial++
        if (tutorial < tutorials.size) {
            tutorials[tutorial].onBegin()
        } else {
            next()
        }
    }
}