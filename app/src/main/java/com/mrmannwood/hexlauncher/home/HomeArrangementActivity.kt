package com.mrmannwood.hexlauncher.home

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R

class HomeArrangementActivity : AppCompatActivity() {

    companion object {

        private const val WIDGET_KEY = "widget"

        fun Intent.decorateForHomeArrangement(widget: String) : Intent {
            return apply {
                putExtra(WIDGET_KEY, widget)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_arrangement)
        supportActionBar?.hide()
        supportFragmentManager.beginTransaction()
                .replace(
                        R.id.container,
                        HomeArrangementFragment.create(
                                intent.extras!!.getString(WIDGET_KEY)!!
                        )
                )
                .commit()
    }
}