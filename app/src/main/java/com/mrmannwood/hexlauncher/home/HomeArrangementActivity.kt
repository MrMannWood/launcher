package com.mrmannwood.hexlauncher.home

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R

class HomeArrangementActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_arrangement)
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, HomeArrangementFragment())
            .commit()
    }
}