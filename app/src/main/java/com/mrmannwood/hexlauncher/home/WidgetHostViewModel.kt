package com.mrmannwood.hexlauncher.home

import android.graphics.Color
import androidx.annotation.LayoutRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.FloatExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.IntExtractor
import com.mrmannwood.launcher.R

class WidgetHostViewModel : ViewModel() {
    val widgetsLiveData = listOf<LiveData<WidgetPlacement>>(
        WidgetLiveData(Widgets.TIME, R.layout.widget_time),
        WidgetLiveData(Widgets.DATE, R.layout.widget_date)
    )

    class WidgetPlacement(
        val widget: String,
        @LayoutRes val layout: Int,
        val yPosition: Float?,
        val color: Int?,
        val loaded: Boolean
    )

    private class WidgetLiveData(widget: String, @LayoutRes layout: Int) : MediatorLiveData<WidgetPlacement>() {
        init {
            var posSet = false
            var colorSet = false
            addSource(PreferenceLiveData(Widgets.Position.key(widget), FloatExtractor)) { yPos ->
                value = WidgetPlacement(widget, layout, yPos, value?.color, colorSet)
                posSet = true
            }
            addSource(PreferenceLiveData(Widgets.Color.key(widget), IntExtractor)) { color ->
                value = WidgetPlacement(widget, layout, value?.yPosition, color ?: Color.WHITE, posSet)
                colorSet = true
            }
        }
    }
}