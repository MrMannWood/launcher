package com.example.testapp.launcher

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.testapp.R
import kotlinx.android.synthetic.main.activity_icon_details.*
import timber.log.Timber

class IconDetailsActivity : AppCompatActivity() {

    companion object {
        private const val PACKAGE_NAME_KEY = "package_name"

        fun buildIntent(activity: Activity, app: AppInfo) : Intent  {
            return Intent(activity, IconDetailsActivity::class.java).apply {
                putExtra(PACKAGE_NAME_KEY, app.packageName)
            }
        }
    }

    private val viewModel: LauncherViewModel by viewModels()

    private lateinit var progress: ProgressBar
    private lateinit var appName: TextView
    private lateinit var icon: ImageView
    private lateinit var foreground: ImageView
    private lateinit var background: ImageView
    private lateinit var backgroundColor: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_details)

        progress = findViewById(R.id.progress_bar)
        appName = findViewById(R.id.name)
        icon = findViewById(R.id.icon)
        foreground = findViewById(R.id.foreground)
        background = findViewById(R.id.background)
        backgroundColor = findViewById(R.id.background_color)

        startObservingLiveData(intent.getStringExtra("package_name")!!)
    }

    private fun startObservingLiveData(packageName: String) {
        Toast.makeText(this, "Looking for $packageName", Toast.LENGTH_LONG).show()

        viewModel.apps.observe(this, Observer {
            it.onSuccess { apps ->
                for (app in apps) {
                    if(app.packageName == packageName){
                        showIconDetails(app)
                        break
                    }
                }
            }
            it.onFailure { error ->
                Timber.e(error)
                throw Exception("Oh No!", error)
            }
            progress.visibility = View.GONE
        })
    }

    private fun showIconDetails(app: AppInfo) {
        val text = StringBuilder("AppName: ${app.label}")

        appName.text = app.label
        icon.setImageDrawable(app.icon)
        if(app.icon is AdaptiveIconDrawable) {
            foreground.setImageDrawable(app.icon.foreground)
            background.setImageDrawable(app.icon.background)
        }
        (app.icon is AdaptiveIconDrawable).apply {
            text.append("\nIcon: is ${if(!this) "not " else ""}adaptive")
            foreground.visibility = if(this) View.VISIBLE else View.INVISIBLE
            background.visibility = if(this) View.VISIBLE else View.INVISIBLE
        }
        backgroundColor.setBackgroundColor(app.backgroundColor)
        text.append("\nBackground color: 0x${Integer.toHexString(app.backgroundColor)}")

        console.text = text.toString()
    }
}