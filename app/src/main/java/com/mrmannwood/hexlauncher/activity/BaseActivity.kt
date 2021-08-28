package com.mrmannwood.hexlauncher.activity

import android.content.res.Resources
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.hexlauncher.font.FontHelper
import com.mrmannwood.launcher.R

open class BaseActivity : AppCompatActivity() {

    override fun getTheme(): Resources.Theme {
        val t = super.getTheme()
        if (FontHelper.useAtkinsonHyperlegible) {
            t.applyStyle(R.style.AtkinsonHyperlegible, false)
        }
        return t
    }

    private var usedHyperlegibleFontOnCreate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        usedHyperlegibleFontOnCreate = FontHelper.useAtkinsonHyperlegible
        super.onCreate(savedInstanceState)
    }

    override fun onStart() {
        super.onStart()
        if (usedHyperlegibleFontOnCreate != FontHelper.useAtkinsonHyperlegible) {
            forceActivityRestart()
        }
    }

    fun forceActivityRestart() {
        recreate()
    }
}