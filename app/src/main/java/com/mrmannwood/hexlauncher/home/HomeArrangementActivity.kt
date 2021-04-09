package com.mrmannwood.hexlauncher.home

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.hexlauncher.view.makeFullScreen
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListWidgetItemBinding
import java.lang.IllegalArgumentException

class HomeArrangementActivity : AppCompatActivity() {

    private val viewModel : HomeArrangementViewModel by viewModels()
    private lateinit var sharedPrefs : SharedPreferences
    private var currentlySelectedWidget : ListWidgetItemBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_arrangement)
        supportActionBar?.hide()
        makeFullScreen()

        viewModel.preferencesLiveData.observe(this) { prefs ->
            sharedPrefs = prefs
        }

        findViewById<RecyclerView>(R.id.widget_recycler).apply {
            layoutManager = LinearLayoutManager(this@HomeArrangementActivity, RecyclerView.HORIZONTAL, false)
            adapter = Adapter(
                    context = this@HomeArrangementActivity,
                    order = arrayOf(WidgetDescription::class),
                    viewFunc = { R.layout.list_widget_item },
                    bindFunc = { binder, widget ->
                        (binder as ListWidgetItemBinding).widget = getString(widget.nameResource)
                        binder.selected = false
                        binder.root.setOnClickListener {
                            currentlySelectedWidget?.selected = false
                            currentlySelectedWidget = binder
                            binder.selected = true
                            viewModel.widgetLiveData.value = widget.preference
                        }
                        binder.root.setOnCreateContextMenuListener { menu, v, menuInfo ->
                            menu.setHeaderTitle(R.string.home_arrangement_widget_menu_title)
                            menu.add(R.string.home_arrangement_widget_menu_item_show).setOnMenuItemClickListener {
                                AlertDialog.Builder(this@HomeArrangementActivity)
                                        .setTitle(R.string.home_arrangement_widget_show_hide_title)
                                        .setMessage(R.string.home_arrangement_widget_show_hide_message)
                                        .setPositiveButton(R.string.home_arrangement_widget_show_hide_positive) { _, _ -> }
                                        .setNegativeButton(R.string.home_arrangement_widget_show_hide_negative) { _, _ ->
                                            sharedPrefs!!.apply {
                                                edit {
                                                    remove(widget.preference)
                                                    remove(Widgets.Gravity.key(widget.preference))
                                                }
                                            }
                                        }
                                        .show()
                                true
                            }
                            menu.add(R.string.home_arrangement_widget_menu_item_color).setOnMenuItemClickListener {
                                AlertDialog.Builder(this@HomeArrangementActivity)
                                        .setTitle(R.string.home_arrangement_widget_color_title)
                                        .setSingleChoiceItems(
                                                arrayOf(
                                                        getString(R.string.color_name_white),
                                                        getString(R.string.color_name_black),
                                                ),
                                                0
                                        ) { _, choice ->
                                            val color = when (choice) {
                                                0 -> Color.WHITE
                                                1 -> Color.BLACK
                                                else -> throw IllegalArgumentException("Unknown choice: $choice")
                                            }
                                            sharedPrefs.apply {
                                                edit {
                                                    putInt(Widgets.Color.key(widget.preference), color)
                                                }
                                            }
                                        }
                                        .show()
                                true
                            }
                        }
                    }
            ).apply {
                setData(
                        WidgetDescription::class,
                        listOf(
                                WidgetDescription(
                                        R.string.preferences_home_widgets_date_name,
                                        Widgets.DATE
                                ),
                                WidgetDescription(
                                        R.string.preferences_home_widgets_time_name,
                                        Widgets.TIME
                                )
                        )
                )
            }
        }

        supportFragmentManager.beginTransaction()
                .replace(R.id.container, HomeArrangementFragment())
                .commit()
    }

    class WidgetDescription(@StringRes val nameResource: Int, val preference: String)
}