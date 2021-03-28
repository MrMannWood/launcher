package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.MainThread
import androidx.core.content.edit
import androidx.preference.PreferenceManager

@MainThread
class Preferences private constructor(private val prefs: SharedPreferences) {

    companion object {

        private var instance : Preferences? = null

        fun getPrefs(context: Context) : Preferences {
            var prefs = instance
            if (prefs == null) {
                if (instance == null) {
                    instance = Preferences(PreferenceManager.getDefaultSharedPreferences(context.applicationContext))
                }
                prefs = instance!!
            }
            return prefs
        }
    }

    fun remove(key: String) {
        prefs.edit { remove(key) }
    }

    fun setString(key: String, value: String) {
        prefs.edit { putString(key, value) }
    }

    fun setBoolean(key: String, value: Boolean) {
        prefs.edit { putBoolean(key, value) }
    }

    fun apply(action: SharedPreferences.Editor.() -> Unit) {
        prefs.edit { action(this) }
    }

    fun getString(key: String, default: String) : String = prefs.getString(key, default)!!

    fun getString(key: String) : String? = prefs.getString(key, null)

    fun getBoolean(key: String, default: Boolean = false) : Boolean = prefs.getBoolean(key, default)

    fun checkExists(key: String) : Boolean = prefs.contains(key)
}