package com.mrmannwood.hexlauncher.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.launcher.R

class HomeArrangementFragment : Fragment(R.layout.fragment_home_arrangement) {

    companion object {
        private const val WIDGET_KEY = "widget"

        fun create(widget: String) : HomeArrangementFragment {
            return HomeArrangementFragment().apply {
                arguments = Bundle().apply {
                    putString(WIDGET_KEY, widget)
                }
            }
        }
    }

    private val viewModel: HomeArrangementViewModel by activityViewModels()
    private var sharedPrefs : SharedPreferences? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.preferencesLiveData.observe(viewLifecycleOwner) { prefs ->
            sharedPrefs = prefs
            getView()?.findViewById<View>(R.id.progress_bar_container)?.visibility = View.GONE
            getView()?.findViewById<View>(R.id.slot_container)?.visibility = View.VISIBLE
            getView()?.findViewById<View>(R.id.hide_button)?.visibility = View.VISIBLE
        }

        val widget = requireArguments().getString(WIDGET_KEY)!!

        view.findViewById<View>(R.id.slot_1).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_1))
        view.findViewById<View>(R.id.slot_2).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_2))
        view.findViewById<View>(R.id.slot_3).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_3))
        view.findViewById<View>(R.id.slot_4).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_4))
        view.findViewById<View>(R.id.slot_5).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_5))
        view.findViewById<View>(R.id.slot_6).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_6))
        view.findViewById<View>(R.id.slot_7).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_7))
        view.findViewById<View>(R.id.slot_8).setOnClickListener(SlotListener(widget, PreferenceKeys.Home.Slots.SLOT_8))

        view.findViewById<View>(R.id.hide_button).setOnClickListener {
            sharedPrefs!!.apply {
                edit { remove(widget) }
                requireActivity().finish()
            }
        }
    }

    private inner class SlotListener(
            val widget: String,
            val slot: String
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            sharedPrefs!!.apply {
                edit { putInt(widget, id) }
                requireActivity().finish()
            }
        }
    }
}