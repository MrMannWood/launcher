package com.mrmannwood.hexlauncher.home

import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding

abstract class WidgetHostFragment : Fragment() {

    private val viewModel: WidgetHostViewModel by activityViewModels()

    private lateinit var databinder : FragmentHomeBinding
    private lateinit var slots : List<FrameLayout>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        databinder = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        databinder.adapter = HomeViewDatabindingAdapter(requireActivity().application)
        databinder.description = makeDescription(isLoading = true)
        return databinder.root
    }

    final override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        onViewCreated(databinder, savedInstanceState)
        slots = listOf(
                databinder.slot0,
                databinder.slot1,
                databinder.slot2,
                databinder.slot3,
                databinder.slot4,
                databinder.slot5,
                databinder.slot6,
                databinder.slot7,
        )

        viewModel.timeWidgetLiveData.observe(
                viewLifecycleOwner,
                WidgetLiveDataObserver(PreferenceKeys.Home.Widgets.TIME, R.layout.widget_time)
        )
        viewModel.dateWidgetLiveData.observe(
                viewLifecycleOwner,
                WidgetLiveDataObserver(PreferenceKeys.Home.Widgets.DATE, R.layout.widget_date)
        )
    }

    open fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {}

    open fun onWidgetLoaded(widget: String, slot: Int) {}

    fun onLoadingComplete() {
        databinder.description = makeDescription(false)
    }

    fun isLoading(): Boolean = databinder.description!!.isLoading()

    abstract fun makeDescription(isLoading: Boolean) : HomeViewDescription

    private inner class WidgetLiveDataObserver(
            private val widgetName: String,
            @LayoutRes private  val widgetLayout: Int
    ) : Observer<Pair<Int?, Int?>> {

        private var widgetView : View? = null

        override fun onChanged(value: Pair<Int?, Int?>) {
            val slot = value.first
            val gravity = value.second
            if (slot == null || gravity == null) {
                hide()
            } else {
                show(slot, gravity)
                onWidgetLoaded(widgetName, slot)
            }
        }

        private fun show(slot: Int, gravity: Int) {
            var view = widgetView
            if (view != null) {
                (view.parent as? ViewGroup)?.removeView(view)
            } else {
                view = LayoutInflater.from(requireContext()).inflate(widgetLayout, slots[slot], false)
                widgetView = view
            }
            view!!.layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_HORIZONTAL or when (gravity) {
                        PreferenceKeys.Home.Widgets.Gravity.TOP -> Gravity.TOP
                        PreferenceKeys.Home.Widgets.Gravity.MIDDLE -> Gravity.CENTER_VERTICAL
                        PreferenceKeys.Home.Widgets.Gravity.BOTTOM -> Gravity.BOTTOM
                        else -> throw IllegalArgumentException("Unknown gravity: $gravity")
                    }.toInt()
            )
            slots[slot].addView(view)
        }

        private fun hide() {
            (widgetView?.parent as? ViewGroup)?.removeView(widgetView)
        }
    }
}