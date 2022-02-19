package com.mrmannwood.hexlauncher.home

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.content.edit
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerDialog
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding

class HomeArrangementFragment : WidgetHostFragment() {

    private val colorPickerViewModel : ColorPickerViewModel by activityViewModels()

    private lateinit var instructionMessage: TextView
    private lateinit var widgetContainer: FrameLayout
    private var sharedPrefs : SharedPreferences? = null

    private val widgets = mutableMapOf<WidgetDescription, View>()
    private var viewBottom = 0
    private var showParentContextMenu = false

    override val nameForInstrumentation = "HomeArrangementFragment"

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.ArrangementDescription(isLoading = false)
    }

    override fun onWidgetLoaded(widgetView: View?, widgetName: String) {
        val description = allWidgets[widgetName]!!
        if (widgetView != null) {
            widgets[description] = widgetView
            onWidgetShown(description, widgetView)
        } else {
            removeWidget(description)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {
        PreferencesRepository.getPrefs(requireContext()) { sharedPrefs = it }

        widgetContainer = databinder.container
        widgetContainer.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, _ -> viewBottom = bottom }
        widgetContainer.setOnTouchListener(object : View.OnTouchListener {

            private val gestureDetector = GestureDetectorCompat(
                requireContext(),
                object : GestureDetector.SimpleOnGestureListener() {
                    override fun onDown(e: MotionEvent?): Boolean {
                        return true
                    }
                    override fun onLongPress(e: MotionEvent) {
                        super.onLongPress(e)
                        widget = null
                        findTouchedWidget(e.x, e.y)?.let { widget ->
                            showParentContextMenu = false
                            widget.showContextMenu(widget.width / 2f, widget.height / 2f)
                        } ?: run {
                            showParentContextMenu = true
                            widgetContainer.showContextMenu(e.x, e.y)
                        }
                    }
                }
            )
            private var widget: View? = null
            private var yOffset: Float = 0f

            override fun onTouch(v: View, event: MotionEvent): Boolean {
                if (event.actionIndex != 0) return gestureDetector.onTouchEvent(event)
                if (widget != null && event.y + yOffset < 0) return gestureDetector.onTouchEvent(event)

                when(event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        widget = findTouchedWidget(event.x, event.y)?.also { widget ->
                            yOffset = widget.y - event.y
                        }
                    }
                    MotionEvent.ACTION_MOVE -> {
                        widget?.let { widget ->
                            val newY = event.y + yOffset
                            if (newY > 0 && newY < viewBottom - widget.height) {
                                widget.y = newY
                            }
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        widget = null
                    }
                }
                return gestureDetector.onTouchEvent(event)
            }

            private fun findTouchedWidget(x: Float, y: Float) : View? {
                for ((_, widget) in widgets) {
                    if (x < widget.x) continue
                    if (y < widget.y) continue
                    if (x > widget.x + widget.width) continue
                    if (y > widget.y + widget.height) continue
                    return widget
                }
                return null
            }
        })

        widgetContainer.setOnCreateContextMenuListener(object : View.OnCreateContextMenuListener {
            override fun onCreateContextMenu(
                menu: ContextMenu,
                v: View,
                menuInfo: ContextMenu.ContextMenuInfo?
            ) {
                if (!showParentContextMenu) return

                menu.setHeaderTitle(R.string.home_arrangement_widgets_menu_title)
                for (widgetDescription in allWidgets.values) {
                    val item = menu.add(widgetDescription.name)
                        .setOnMenuItemClickListener {
                            if (widgets.contains(widgetDescription)) {
                                removeWidget(widgetDescription)
                            } else {
                                createAndShowWidget(widgetDescription)
                            }
                            true
                        }
                    if (widgets.contains(widgetDescription)) {
                        item.isCheckable = true
                        item.isChecked = true
                    }
                }
            }

            private fun createAndShowWidget(widgetDescription: WidgetDescription) {
                LayoutInflater.from(requireContext())
                    .inflate(widgetDescription.layout, widgetContainer, false).apply {
                        val yPosition = widgets.values.firstOrNull()?.let {
                            if (it.y > widgetContainer.height / 2) {
                                0
                            } else {
                                it.y + it.height
                            }
                        } ?: 0

                        widgets[widgetDescription] = this
                        tag = widgetDescription
                        layoutParams = FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            Gravity.CENTER_HORIZONTAL
                        )
                        this.y = yPosition.toFloat()
                        widgetContainer.addView(this)
                        onWidgetShown(widgetDescription, this)
                    }
            }
        })

        instructionMessage = LayoutInflater.from(requireContext())
            .inflate(R.layout.item_home_arrangement_instruction, databinder.container, true)
            .findViewById(R.id.arrangement_instruction)
        setInstructionText()
        instructionMessage.startAnimation(
            AlphaAnimation(0.0f, 1.0f).apply {
                duration = 1250
                startOffset = 20
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }
        )
    }

    override fun onStop() {
        super.onStop()
        sharedPrefs?.edit {
            allWidgets
                .filter { !widgets.contains(it.value) }
                .map { it.key }
                .forEach { widget ->
                    remove(Widgets.Position.key(widget))
                    remove(Widgets.Color.key(widget))
                }
            widgets.forEach { (widgetDescription, widgetView) ->
                putFloat(Widgets.Position.key(widgetDescription.widget), widgetView.y)
                putInt(Widgets.Color.key(widgetDescription.widget), (widgetView as TextView).currentTextColor)
            }
        }
    }

    private fun removeWidget(widgetDescription: WidgetDescription) {
        widgetContainer.removeView(widgets.remove(widgetDescription))
        setInstructionText()
    }

    private fun onWidgetShown(description: WidgetDescription, widget: View) {
        widget.setOnCreateContextMenuListener { menu, _, _ ->
            menu.setHeaderTitle(description.name)
            menu.add(R.string.home_arrangement_widget_hide).setOnMenuItemClickListener {
                removeWidget(description)
                true
            }
            menu.add(R.string.home_arrangement_widget_color).setOnMenuItemClickListener {
                colorPickerViewModel.colorLiveData.value = (widget as TextView).currentTextColor
                colorPickerViewModel.completionLiveData.value = false

                val colorObserver = Observer<Int> { color ->
                    widget.setTextColor(color)
                }
                val completionObserver = object : Observer<Boolean> {
                    override fun onChanged(complete: Boolean) {
                        if (complete) {
                            colorPickerViewModel.colorLiveData.removeObserver(colorObserver)
                            colorPickerViewModel.completionLiveData.removeObserver(this)
                        }
                    }
                }
                colorPickerViewModel.colorLiveData.observe(viewLifecycleOwner, colorObserver)
                colorPickerViewModel.completionLiveData.observe(viewLifecycleOwner, completionObserver)
                ColorPickerDialog().show(childFragmentManager, null)
                true
            }
        }
        setInstructionText()
    }

    private fun setInstructionText() {
        if (widgets.isEmpty()) {
            instructionMessage.setText(R.string.home_arrangement_tutorial_get_started)
        } else {
            instructionMessage.setText(R.string.home_arrangement_tutorial_customize_widget)
        }
    }

    private val allWidgets = mapOf(
        Widgets.DATE to WidgetDescription(Widgets.DATE, R.string.preferences_home_widgets_date_name, R.layout.widget_date),
        Widgets.TIME to WidgetDescription(Widgets.TIME, R.string.preferences_home_widgets_time_name, R.layout.widget_time)
    )

    private class WidgetDescription(val widget: String, val name: Int, val layout: Int)
}