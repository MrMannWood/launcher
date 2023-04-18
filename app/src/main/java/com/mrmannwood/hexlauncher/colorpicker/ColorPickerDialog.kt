package com.mrmannwood.hexlauncher.colorpicker

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.google.android.material.slider.Slider
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.launcher.R

class ColorPickerDialog : DialogFragment(R.layout.dialog_color_picker) {

    private val imagePickerContract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) return@registerForActivityResult
        cpuBoundTaskExecutor.execute {
            val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                ImageDecoder.decodeBitmap(
                    ImageDecoder.createSource(
                        requireContext().contentResolver,
                        uri
                    )
                )
            } else {
                MediaStore.Images.Media.getBitmap(requireContext().contentResolver, uri)
            }.copy(Bitmap.Config.ARGB_8888, true)
            
            IconAdapter.INSTANCE.getPalette(bitmap) { palette ->
                viewModel.colorSuggestionLiveData.postValue(
                    listOfNotNull(
                        palette.vibrantSwatch,
                        palette.lightVibrantSwatch,
                        palette.darkVibrantSwatch,
                        palette.darkMutedSwatch,
                        palette.lightMutedSwatch,
                        palette.mutedSwatch,
                        palette.dominantSwatch,
                    )
                        .sortedBy { it.population }
                        .map { it.rgb }
                        .distinct()
                )
            }
        }
    }


    private var redValue = 0
    private var greenValue = 0
    private var blueValue = 0
    
    private val viewModel: ColorPickerViewModel by activityViewModels()

    private lateinit var redSlider: Slider
    private lateinit var greenSlider: Slider
    private lateinit var blueSlider: Slider
    private lateinit var hexCodeView: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.completionLiveData.value = false
        viewModel.cancellationLiveData.value = false

        (viewModel.colorLiveData.value ?: Color.WHITE).let { color ->
            redValue = Color.red(color)
            greenValue = Color.green(color)
            blueValue = Color.blue(color)
        }

        val swatch = view.findViewById<View>(R.id.controller_swatch)
        swatch.setBackgroundColor(Color.rgb(redValue, greenValue, blueValue))

        view.findViewById<Button>(R.id.image_selector).setOnClickListener {
            imagePickerContract.launch("image/*")
        }
        redSlider = view.findViewById<Slider>(R.id.slider_red).apply {
            value = redValue.toFloat()
            addOnChangeListener { _, value, _ ->
                redValue = value.toInt()
                updateSwatch(swatch)
            }
        }
        greenSlider = view.findViewById<Slider>(R.id.slider_green).apply {
            value = greenValue.toFloat()
            addOnChangeListener { _, value, _ ->
                greenValue = value.toInt()
                updateSwatch(swatch)
            }
        }
        blueSlider = view.findViewById<Slider>(R.id.slider_blue).apply {
            value = blueValue.toFloat()
            addOnChangeListener { _, value, _ ->
                blueValue = value.toInt()
                updateSwatch(swatch)
            }
        }
        hexCodeView = view.findViewById<TextView>(R.id.hex_code).apply {
            text = getHexCode()
        }

        view.findViewById<View>(R.id.button_positive).setOnClickListener {
            viewModel.completionLiveData.value = true
            dismiss()
        }

        view.findViewById<View>(R.id.button_negative).setOnClickListener {
            viewModel.cancellationLiveData.value = true
            viewModel.completionLiveData.value = true
            dismiss()
        }

        val suggestionsContainer = view.findViewById<View>(R.id.suggested_swatch_container)
        val suggestionSwatches = listOf<View>(
            view.findViewById(R.id.swatch_1),
            view.findViewById(R.id.swatch_2),
            view.findViewById(R.id.swatch_3),
            view.findViewById(R.id.swatch_4),
            view.findViewById(R.id.swatch_5)
        )
        suggestionSwatches.forEach {
            it.setOnClickListener {
                val color = (it.background as ColorDrawable).color
                redValue = (color shr 16) and 0xFF
                greenValue = (color shr 8) and 0xFF
                blueValue = (color) and 0xFF
                updateSwatch(swatch)
                true
            }
        }

        viewModel.colorSuggestionLiveData.observe(viewLifecycleOwner) { suggestions ->
            suggestions?.take(suggestionSwatches.size)?.forEachIndexed { i, suggestion ->
                suggestionSwatches[i].visibility = View.VISIBLE
                suggestionSwatches[i].setBackgroundColor(suggestion)
                suggestionSwatches[i].contentDescription = getString(
                    R.string.dialog_color_picker_suggested_swatch_content_description,
                    getString(ColorUtil.getColorNameFromHex(suggestion))
                )
            }
            suggestionsContainer.visibility = if (suggestions.isNullOrEmpty()) {
                View.GONE
            } else {
                View.VISIBLE
            }
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun getColor(): Int = Color.rgb(redValue, greenValue, blueValue)

    private fun updateSwatch(swatch: View) {
        val color = getColor()

        hexCodeView.text = getHexCode()

        redSlider.value = redValue.toFloat()
        greenSlider.value = greenValue.toFloat()
        blueSlider.value = blueValue.toFloat()
        swatch.setBackgroundColor(color)
        viewModel.colorLiveData.value = color
    }

    private fun getHexCode(): String {
        return String.format("#%02X%02X%02X", redValue, greenValue, blueValue)
    }
}
