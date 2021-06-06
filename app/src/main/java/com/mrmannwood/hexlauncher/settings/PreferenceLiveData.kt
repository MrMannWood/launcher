package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect

class PreferenceLiveData<T>(
    context: Context,
    private val key: String,
    private val extractor: PreferenceExtractor<T>
) : LiveData<T>() {

    companion object {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    private val appContext = context.applicationContext

    private var job : Job? = null

    @MainThread
    override fun onActive() {
        super.onActive()
        job = scope.launch {
            PreferencesRepository.watchPref(appContext, key, extractor)
                .collect { postValue(it) }
        }
    }

    @MainThread
    override fun onInactive() {
        super.onInactive()
        job?.cancel()
    }

    override fun postValue(value: T?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.setValue(value)
        } else {
            super.postValue(value)
        }
    }
}