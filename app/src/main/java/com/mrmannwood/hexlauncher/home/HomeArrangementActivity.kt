package com.mrmannwood.hexlauncher.home

import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatSpinner
import androidx.appcompat.widget.Toolbar
import androidx.core.content.edit
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrmannwood.hexlauncher.settings.PreferenceKeys.Home.Widgets
import com.mrmannwood.launcher.R

class HomeArrangementActivity : AppCompatActivity() {

    private val viewModel: HomeArrangementViewModel by viewModels()
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_arrangement)
        setSupportActionBar(
            findViewById<Toolbar>(R.id.toolbar).apply {
                title = ""
                forceOverflowIconColor(this, Color.WHITE)
            }
        )

        viewModel.preferencesLiveData.observe(this) { prefs ->
            sharedPrefs = prefs
        }

        val data = listOf(
            WidgetDescription(
                R.string.preferences_home_widgets_date_name,
                Widgets.DATE
            ),
            WidgetDescription(
                R.string.preferences_home_widgets_time_name,
                Widgets.TIME
            )
        )

        findViewById<AppCompatSpinner>(R.id.widget_select).apply {
            adapter = ArrayAdapter(
                this@HomeArrangementActivity,
                R.layout.spinner_item_widget,
                data.map { getString(it.nameResource) }
            ).apply {
                setDropDownViewResource(R.layout.spinner_item_widget_dropdown)
            }
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    viewModel.widgetLiveData.value = null
                }

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    viewModel.widgetLiveData.value = data[position].preference
                }
            }
        }

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeArrangementFragment())
            .commit()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menu.add(R.string.home_arrangement_widget_menu_item_show).setOnMenuItemClickListener {
            val widget = viewModel.widgetLiveData.value ?: return@setOnMenuItemClickListener false
            MaterialAlertDialogBuilder(this@HomeArrangementActivity)
                .setTitle(R.string.home_arrangement_widget_show_hide_title)
                .setMessage(R.string.home_arrangement_widget_show_hide_message)
                .setPositiveButton(R.string.home_arrangement_widget_show_hide_positive) { _, _ -> }
                .setNegativeButton(R.string.home_arrangement_widget_show_hide_negative) { _, _ ->
                    sharedPrefs!!.apply {
                        edit {
                            remove(widget)
                            remove(Widgets.Gravity.key(widget))
                        }
                    }
                }
                .show()
            true
        }
        menu.add(R.string.home_arrangement_widget_color_title).setOnMenuItemClickListener {
            val widget = viewModel.widgetLiveData.value ?: return@setOnMenuItemClickListener false
            MaterialAlertDialogBuilder(this@HomeArrangementActivity)
                .setTitle(R.string.home_arrangement_widget_color_title)
                .setSingleChoiceItems(
                    arrayOf(
                        getString(R.string.color_name_white),
                        getString(R.string.color_name_black),
                    ),
                    when (sharedPrefs.getInt(Widgets.Color.key(widget), Color.WHITE)) {
                        Color.WHITE -> 0
                        Color.BLACK -> 1
                        else -> throw IllegalArgumentException("Unknown color: ${sharedPrefs.getInt(Widgets.Color.key(widget), Color.WHITE)}")
                    }
                ) { _, choice ->
                    val color = when (choice) {
                        0 -> Color.WHITE
                        1 -> Color.BLACK
                        else -> throw IllegalArgumentException("Unknown choice: $choice")
                    }
                    sharedPrefs.apply {
                        edit {
                            putInt(Widgets.Color.key(widget), color)
                        }
                    }
                }
                .show()
            true
        }
        return true
    }

    private fun forceOverflowIconColor(toolbar: Toolbar, @ColorInt color: Int) {
        val icon = toolbar.overflowIcon ?: return
        toolbar.overflowIcon = DrawableCompat.wrap(icon).apply {
            DrawableCompat.setTint(this, color)
            DrawableCompat.setTintMode(this, PorterDuff.Mode.SRC_IN)
        }
    }

    class WidgetDescription(@StringRes val nameResource: Int, val preference: String)
}