package com.mrmannwood.hexlauncher.home

import android.app.WallpaperManager
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

    private val viewModel: HomeArrangementViewModel by activityViewModels()
    private var sharedPrefs : SharedPreferences? = null
    private var currWidget : String? = null
    private val widgetLocations = mutableMapOf<String, Int>()

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.ArrangementDescription(isLoading)
    }

    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {
        viewModel.widgetLiveData.observe(viewLifecycleOwner) { widget -> currWidget = widget }
        viewModel.preferencesLiveData.observe(viewLifecycleOwner) { prefs ->
            sharedPrefs = prefs
            onLoadingComplete()
        }

        databinder.slot0.setOnClickListener(SlotListener(0))
        databinder.slot1.setOnClickListener(SlotListener(1))
        databinder.slot2.setOnClickListener(SlotListener(2))
        databinder.slot3.setOnClickListener(SlotListener(3))
        databinder.slot4.setOnClickListener(SlotListener(4))
        databinder.slot5.setOnClickListener(SlotListener(5))
        databinder.slot6.setOnClickListener(SlotListener(6))
        databinder.slot7.setOnClickListener(SlotListener(7))
    }

    override fun onWidgetLoaded(widget: String, slot: Int) {
        widgetLocations[widget] = slot
    }

    private fun findWidgetInSlot(slot: Int): String? {
        return widgetLocations.entries.find { it.value == slot }?.key
    }

    private inner class SlotListener(
            val slot: Int
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            val widget = currWidget ?: return

            val widgetInSlot = findWidgetInSlot(slot)
            if (widgetInSlot != null && widgetInSlot != widget) {
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