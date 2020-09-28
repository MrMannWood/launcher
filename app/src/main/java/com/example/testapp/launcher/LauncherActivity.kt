package com.example.testapp.launcher

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.testapp.HandleBackPressed
import com.example.testapp.R

class LauncherActivity : AppCompatActivity() {

    private val viewModel: LauncherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        viewModel.apps.observe(this, Observer {
            it.exceptionOrNull()?.let {
                Toast.makeText(this, "Unable to load Apps", Toast.LENGTH_LONG).show()
            }
        })

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, HomeFragment())
                .commit()
        }
    }

    override fun onBackPressed() {
        val handled = supportFragmentManager.findFragmentById(R.id.container)?.let { fragment ->
            if (fragment is HandleBackPressed) {
                fragment.handleBackPressed()
            } else {
                false
            }
        } ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }
}