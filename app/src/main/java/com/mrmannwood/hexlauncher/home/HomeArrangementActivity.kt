package com.mrmannwood.hexlauncher.home

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.view.makeFullScreen
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListWidgetItemBinding

class HomeArrangementActivity : AppCompatActivity() {

    private val homeArrangementViewModel : HomeArrangementViewModel by viewModels()

    private var currentlySelectedWidget : ListWidgetItemBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_arrangement)
        supportActionBar?.hide()
        makeFullScreen()

        findViewById<RecyclerView>(R.id.widget_recycler).apply {
            layoutManager = LinearLayoutManager(this@HomeArrangementActivity, RecyclerView.HORIZONTAL, false)
            adapter = Adapter(
                    context = this@HomeArrangementActivity,
                    order = arrayOf(Int::class),
                    viewFunc = { R.layout.list_widget_item },
                    bindFunc = { binder, widget ->
                        (binder as ListWidgetItemBinding).widget = getString(widget)
                        binder.selected = false
                        binder.root.setOnClickListener {
                            currentlySelectedWidget?.selected = false
                            currentlySelectedWidget = binder
                            binder.selected = true
                            homeArrangementViewModel.widgetLiveData.value = when (widget) {
                                R.string.preferences_home_widgets_date_name -> Widgets.DATE
                                R.string.preferences_home_widgets_time_name -> Widgets.TIME
                                else -> throw IllegalArgumentException("Unknown widget $widget")
                            }
                        }
                    }
            ).apply {
                setData(
                        Int::class,
                        listOf(
                                R.string.preferences_home_widgets_date_name,
                                R.string.preferences_home_widgets_time_name
                        )
                )
            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeArrangementFragment())
                .commit()
    }
}