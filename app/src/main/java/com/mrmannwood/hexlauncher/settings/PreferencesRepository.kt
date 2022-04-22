package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import com.mrmannwood.hexlauncher.executors.diskExecutor

object PreferencesRepository {

    @Volatile private var prefs: SharedPreferences? = null

    fun getPrefs(context: Context, callback: (SharedPreferences) -> Unit) {
        val appContext = context.applicationContext
        if (prefs != null) {
            callback(prefs!!)
        } else {
            diskExecutor.execute {
                if (prefs != null) {
                    callback(prefs!!)
                } else {
                    synchronized(this@PreferencesRepository) {
                        if (prefs == null) {
                            prefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                        }
                    }
                    callback(prefs!!)
                }
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
