package com.mrmannwood.hexlauncher.textentrydialog

import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.mrmannwood.launcher.R

class TextEntryDialog : DialogFragment(R.layout.dialog_text_entry) {

    private val viewModel : TextEntryDialogViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val textEntry = view.findViewById<EditText>(R.id.text_input)

        view.findViewById<View>(R.id.button_positive).setOnClickListener {
            viewModel.completionLiveData.value = textEntry.text.toString()
            dismiss()
        }

        view.findViewById<View>(R.id.button_negative).setOnClickListener {
            viewModel.completionLiveData.value = null
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}