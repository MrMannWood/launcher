package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.mrmannwood.hexlauncher.executors.diskExecutor

object PreferencesRepository {

    @Volatile private var prefs: SharedPreferences? = null

    fun getPrefs(context: Context, callback: (SharedPreferences) -> Unit) {
        val appContext = context.applicationContext
        val sp = prefs
        if (sp != null) {
            callback(sp)
        } else {
            diskExecutor.execute {
                val sp = prefs
                if (sp != null) {
                    callback(sp)
                } else {
                    val sp: SharedPreferences = synchronized(this@PreferencesRepository) {
                        var sp = prefs
                        if (sp != null) {
                            sp
                        } else {
                            sp = PreferenceManager.getDefaultSharedPreferences(appContext)
                            prefs = sp
                            convert(sp)
                            sp
                        }
                    }
                    callback(sp)
                }
            }
        }
    }
    
    private fun convert(prefs: SharedPreferences) {
        prefs.edit {
            arrayOf("home_widget_date_position", "home_widget_time_position")
                .mapNotNull { key ->
                    try {
                        val value = prefs.getFloat(key, -1f)
                        if (value < 0) null
                        else key to value
                    } catch (e: ClassCastException) {
                        // we've already run the conversion and can ignore
                        null
                    }
                }
                .forEach { (key, value) ->
                    remove(key)
                    putString(key, "null,$value")
                }
        }
    }

    fun <T> watchPref(context: Context, key: String, extractor: PreferenceExtractor<T>): LiveData<T?> {
        return object : LiveData<T?>() {

            private val listener = object : SharedPreferences.OnSharedPreferenceChangeListener {
                override fun onSharedPreferenceChanged(
                    prefs: SharedPreferences,
                    changedKey: String?
                ) {
                    if (changedKey != key) return
                    postValue(extractor.getValue(prefs, key))
                }
            }

            override fun onActive() {
                super.onActive()
                getPrefs(context) { prefs ->
                    postValue(extractor.getValue(prefs, key))
                    prefs.registerOnSharedPreferenceChangeListener(listener)
                }
            }

            override fun onInactive() {
                super.onInactive()
                getPrefs(context) { prefs ->
                    prefs.unregisterOnSharedPreferenceChangeListener(listener)
                }
            }
        }
    }
}
