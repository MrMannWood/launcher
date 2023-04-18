package com.mrmannwood.hexlauncher.home

import android.app.Application
import android.graphics.Color
import androidx.annotation.LayoutRes
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.settings.Preference
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor.IntExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
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
        val xPosition: Float?,
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
            PreferencesRepository.getPrefs(application) { repo ->
                addSource(
                    repo.watchPref(
                        Widgets.Position.key(widget),
                        WidgetPositionExtractor
                    )
                ) { pos ->
                    val xPos = pos?.first
                    val yPos = pos?.second
                    value = WidgetPlacement(widget, layout, yPos, xPos, value?.color, colorSet)
                    posSet = true
                }
                addSource(repo.watchPref(Widgets.Color.key(widget), IntExtractor)) { color ->
                    value = WidgetPlacement(
                        widget,
                        layout,
                        value?.yPosition,
                        value?.xPosition,
                        color ?: Color.WHITE,
                        posSet
                    )
                    colorSet = true
                }
            }
        }
    }

    private object WidgetPositionExtractor : PreferenceExtractor<Pair<Float?, Float>?> {
        override fun getValue(pref: Preference<*>): Pair<Float?, Float>? {
            val raw = PreferenceExtractor.StringExtractor.getValue(pref)
            val split = raw.split(",")
            if (split.size != 2) return null
            val xPos = split[0].toFloatOrNull()
            val yPos = split[1].toFloatOrNull() ?: return null
            return Pair(xPos, yPos)
        }
    }
}
