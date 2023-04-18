package com.mrmannwood.hexlauncher.nux

import android.view.View
import com.mrmannwood.launcher.R

class TouchTutorialFragment : AbstractGestureWheelTutorialFragment() {

    private interface TouchTutorial {
        var appSelected: Boolean
        fun onBegin()
        fun onDown() = Unit
        fun onUp() = Unit
        fun onAppSelected(selected: View) = Unit
        fun onAppDeselected() = Unit
    }

    private val tutorials = listOf(
        object : TouchTutorial {
            override var appSelected: Boolean = false

            override fun onBegin() {
                pushMessage(R.string.nux_touch_tutorial_message)
                gestures.filter { it.id == R.id.north_container || it.id == R.id.south_container }
                    .forEach { it.visibility = View.INVISIBLE }
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
                gestures.filter { it.id == R.id.north_container || it.id == R.id.south_container }
                    .forEach { it.visibility = View.INVISIBLE }
                pushMessage(R.string.nux_touch_tutorial_message)
                gestures.filter { it.id != R.id.north_container }.forEach {
                    it.setOnCreateContextMenuListener { menu, _, _ ->
                        menu.add(R.string.nux_customize_tutorial_customize)
                            .setOnMenuItemClickListener {
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
        },
        object : TouchTutorial {
            override var appSelected: Boolean = false

            override fun onBegin() {
                gestures.filter { it.id == R.id.north_container || it.id == R.id.south_container }
                    .forEach { it.visibility = View.INVISIBLE }
                allowContextMenu = true
                pushMessage(R.string.nux_settings_message)
                requireView().setOnCreateContextMenuListener { menu, _, _ ->
                    menu.add(R.string.nux_settings_tutorial_settings).setOnMenuItemClickListener {
                        nextTutorial()
                        true
                    }
                }
            }
        },
        object : TouchTutorial {
            override var appSelected: Boolean = false

            override fun onBegin() {
                gestures.find { it.id == R.id.north_container }?.visibility = View.VISIBLE
                gestures.find { it.id == R.id.south_container }?.visibility = View.INVISIBLE
                pushMessage(R.string.nux_swipe_message)
            }

            override fun onUp() {
                if (appSelected) {
                    nextTutorial()
                }
            }
        },
    )

    private var tutorialIterator = tutorials.iterator()
    private var tutorial: TouchTutorial? = tutorialIterator.next()

    override fun onViewCreated() {
        tutorial?.onBegin()
    }

    override fun onDown() {
        tutorial?.onDown()
    }

    override fun onUp() {
        tutorial?.onUp()
    }

    override fun onAppSelected(selected: View) {
        tutorial?.appSelected = true
        tutorial?.onAppSelected(selected)
    }

    override fun onAppDeselected() {
        tutorial?.appSelected = false
        tutorial?.onAppDeselected()
    }

    fun nextTutorial() {
        if (tutorialIterator.hasNext()) {
            tutorial = tutorialIterator.next()
            tutorial?.onBegin()
        } else {
            next()
        }
    }
}
