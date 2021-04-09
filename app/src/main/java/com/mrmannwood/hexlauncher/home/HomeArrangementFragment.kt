package com.mrmannwood.hexlauncher.home

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeArrangementBinding

class HomeArrangementFragment : Fragment() {

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
    private lateinit var databinder : FragmentHomeArrangementBinding
    private var sharedPrefs : SharedPreferences? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        databinder = DataBindingUtil.inflate(inflater, R.layout.fragment_home_arrangement, container, false)
        databinder.adapter = HomeViewDatabindingAdapter(requireActivity().application)
        databinder.description = HomeViewDescription.ArrangementDescription(isLoading = true)
        return databinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.preferencesLiveData.observe(viewLifecycleOwner) { prefs ->
            sharedPrefs = prefs
            databinder.description = HomeViewDescription.ArrangementDescription(isLoading = false)
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
                edit { remove(widget) }
                requireActivity().finish()
            }
        }
    }

    private inner class SlotListener(
            val widget: String,
            val slot: Int
    ) : View.OnClickListener {
        override fun onClick(v: View?) {
            sharedPrefs!!.apply {
                edit { putInt(widget, slot) }
                requireActivity().finish()
            }
        }
    }
}