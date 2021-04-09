package com.mrmannwood.hexlauncher.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PreferencesLiveData private constructor(
        private val application: Application
) : LiveData<SharedPreferences>() {

    companion object {

        private lateinit var instance: PreferencesLiveData

        fun create(application: Application) : PreferencesLiveData{
            instance = PreferencesLiveData(application)
            return instance
        }

        fun get() = instance
    }

    @Volatile private var prefs : SharedPreferences? = null

    override fun onActive() {
        super.onActive()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                postValue(getSharedPreferences())
            }
        }
    }

    fun getSharedPreferences() : SharedPreferences {
        val preferences = prefs
        if (preferences != null) {
            return preferences
        }
        synchronized(this) {
            var preferences = prefs
            if (preferences != null) {
                return preferences
            }
            preferences = makeSharedPrefs()
            prefs = preferences
            return preferences
        }
    }

    private fun makeSharedPrefs() : SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(application)
    }
}