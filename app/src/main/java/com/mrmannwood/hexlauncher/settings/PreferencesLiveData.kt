package com.mrmannwood.hexlauncher.settings

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
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

    override fun onActive() {
        super.onActive()
        GlobalScope.launch {
            postValue(PreferencesRepository.getPrefs(application))
        }
    }
}