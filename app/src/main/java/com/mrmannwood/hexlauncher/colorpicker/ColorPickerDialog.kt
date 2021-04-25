package com.mrmannwood.hexlauncher.colorpicker

import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.slider.Slider
import com.mrmannwood.launcher.R

class ColorPickerDialog : DialogFragment(R.layout.dialog_color_picker) {

    private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0

    private val viewModel : ColorPickerViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.completionLiveData.value = false

        (viewModel.colorLiveData.value ?: Color.WHITE).let { color ->
            redValue = Color.red(color)
            greenValue = Color.green(color)
            blueValue = Color.blue(color)
        }

        val swatch = view.findViewById<View>(R.id.swatch)
        swatch.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue))

        view.findViewById<Slider>(R.id.slider_red).apply {
            value = redValue.toFloat()
            addOnChangeListener { _, value, _ ->
                redValue = value.toInt()
                updateSwatch(swatch)
            }
        }
        view.findViewById<Slider>(R.id.slider_green).apply {
            value = greenValue.toFloat()
            addOnChangeListener { _, value, _ ->
                greenValue = value.toInt()
                updateSwatch(swatch)
            }
        }
        view.findViewById<Slider>(R.id.slider_blue).apply {
            value = blueValue.toFloat()
            addOnChangeListener { _, value, _ ->
                blueValue = value.toInt()
                updateSwatch(swatch)
            }
        }

        view.findViewById<View>(R.id.button_positive).setOnClickListener {
            viewModel.completionLiveData.value = true
            dismiss()
        }
    }

    private fun getColor() : Int = Color.rgb(redValue, greenValue, blueValue)

    private fun updateSwatch(swatch: View) {
        getColor().let { color ->
            swatch.setBackgroundColor(color)
            viewModel.colorLiveData.value = color
        }
    }
}