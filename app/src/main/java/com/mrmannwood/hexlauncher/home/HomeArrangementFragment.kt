package com.mrmannwood.hexlauncher.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding

class HomeArrangementFragment : WidgetHostFragment() {

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
    private val widgetLocations = mutableMapOf<Int, String>()

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.ArrangementDescription(isLoading)
    }

    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {
        viewModel.preferencesLiveData.observe(viewLifecycleOwner) { prefs ->
            sharedPrefs = prefs
            onLoadingComplete()
        }

        val widget = requireArguments().getString(WIDGET_KEY)!!

        databinder.slot0.setOnClickListener(SlotListener(widget, 0))
        databinder.slot1.setOnClickListener(SlotListener(widget, 1))
        databinder.slot2.setOnClickListener(SlotListener(widget, 2))
        databinder.slot3.setOnClickListener(SlotListener(widget, 3))
        databinder.slot4.setOnClickListener(SlotListener(widget, 4))
        databinder.slot5.setOnClickListener(SlotListener(widget, 5))
        databinder.slot6.setOnClickListener(SlotListener(widget, 6))
        databinder.slot7.setOnClickListener(SlotListener(widget, 7))

        databinder.hideButton.setOnClickListener {
            sharedPrefs!!.apply {
                edit {
                    remove(widget)
                    remove(Widgets.Gravity.key(widget))
                }
                requireActivity().finish()
            }
        }
    }

    override fun onWidgetLoaded(widget: String, slot: Int) {
        widgetLocations[slot] = widget
    }

    private inner class SlotListener(
            val widget: String,
            val slot: Int
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            if (widgetLocations[slot] != null && widgetLocations[slot] != widget) {
                Toast.makeText(requireContext(), R.string.home_arrangement_slot_already_filled_toast, Toast.LENGTH_SHORT).show()
                return
            }

            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.home_arrangement_slot_position_title)
                    .setItems(
                            arrayOf(
                                    resources.getString(R.string.home_arrangement_slot_position_top),
                                    resources.getString(R.string.home_arrangement_slot_position_middle),
                                    resources.getString(R.string.home_arrangement_slot_position_bottom),
                            )
                    ) { _, which ->
                        val gravity = when (which) {
                            0 -> Widgets.Gravity.TOP
                            1 -> Widgets.Gravity.MIDDLE
                            2 -> Widgets.Gravity.BOTTOM
                            else -> throw IllegalArgumentException("Unknown selection: $which")
                        }
                        sharedPrefs!!.apply {
                            edit {
                                putInt(widget, slot)
                                putInt(Widgets.Gravity.key(widget), gravity)
                            }
                        }
                    }
                    .show()
        }
    }
}