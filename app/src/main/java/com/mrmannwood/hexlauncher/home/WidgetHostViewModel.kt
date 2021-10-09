package com.mrmannwood.hexlauncher.home

import android.app.Application
import android.graphics.Color
import androidx.annotation.LayoutRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.asLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.FloatExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.IntExtractor
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref
import com.mrmannwood.launcher.R

class WidgetHostViewModel(application: Application) : AndroidViewModel(application) {
    val widgetsLiveData = listOf<LiveData<WidgetPlacement>>(
        WidgetLiveData(application, Widgets.TIME, R.layout.widget_time),
        WidgetLiveData(application, Widgets.DATE, R.layout.widget_date)
    )

    class WidgetPlacement(
        val widget: String,
        @LayoutRes val layout: Int,
        val yPosition: Float?,
        val color: Int?,
        val loaded: Boolean
    )

    private class WidgetLiveData(
        application: Application,
        widget: String,
        @LayoutRes layout: Int
    ) : MediatorLiveData<WidgetPlacement>() {
        init {
            var posSet = false
            var colorSet = false
            addSource(watchPref(application, Widgets.Position.key(widget), FloatExtractor).asLiveData()) { yPos ->
                value = WidgetPlacement(widget, layout, yPos, value?.color, colorSet)
                posSet = true
            }
            addSource(watchPref(application, Widgets.Color.key(widget), IntExtractor).asLiveData()) { color ->
                value = WidgetPlacement(widget, layout, value?.yPosition, color ?: Color.WHITE, posSet)
                colorSet = true
            }
        }
    }
}