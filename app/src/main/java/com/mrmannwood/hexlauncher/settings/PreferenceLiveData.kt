package com.mrmannwood.hexlauncher.settings

import android.content.SharedPreferences
import android.os.Looper
import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import java.util.concurrent.atomic.AtomicBoolean

class PreferenceLiveData<T>(
    private val key: String,
    private val extractor: Extractor<T>
) : LiveData<T>() {

    private val isActive = AtomicBoolean(false)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { prefs, changedKey ->
        if (key == changedKey) {
            postValue(extractor.getValue(prefs, key))
        }
    }

    private var observer = Observer<SharedPreferences> { prefs ->
        if (isActive.get()) {
            sharedPreferences = prefs
            postValue(extractor.getValue(prefs, key))
            prefs.registerOnSharedPreferenceChangeListener(listener)
        }
    }

    private var sharedPreferences : SharedPreferences? = null

    @MainThread
    override fun onActive() {
        super.onActive()
        isActive.set(true)
        PreferencesLiveData.get().observeForever(observer)
    }

    @MainThread
    override fun onInactive() {
        super.onInactive()
        isActive.set(false)
        sharedPreferences?.unregisterOnSharedPreferenceChangeListener(listener)
        PreferencesLiveData.get().removeObserver(observer)
    }

    override fun postValue(value: T?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            super.setValue(value)
        } else {
            super.postValue(value)
        }
    }

    sealed class Extractor<T> {

        abstract fun getValue(sharedPreferences: SharedPreferences, key: String) : T?

        object StringExtractor : Extractor<String?>() {
            override fun getValue(sharedPreferences: SharedPreferences, key: String): String? {
                return sharedPreferences.getString(key, null)
            }
        }

        object BooleanExtractor : Extractor<Boolean?>() {
            override fun getValue(sharedPreferences: SharedPreferences, key: String): Boolean? {
                return if (sharedPreferences.contains(key)) {
                    sharedPreferences.getBoolean(key, false)
                } else {
                    null
                }
            }
        }

        object IntExtractor : Extractor<Int?>() {
            override fun getValue(sharedPreferences: SharedPreferences, key: String): Int? {
                return return if (sharedPreferences.contains(key)) {
                    sharedPreferences.getInt(key, 0)
                } else {
                    null
                }
            }
        }

        object FloatExtractor : Extractor<Float?>() {
            override fun getValue(sharedPreferences: SharedPreferences, key: String): Float? {
                return return if (sharedPreferences.contains(key)) {
                    sharedPreferences.getFloat(key, 0f)
                } else {
                    null
                }
            }
        }

        object LongExtractor : Extractor<Long?>() {
            override fun getValue(sharedPreferences: SharedPreferences, key: String): Long? {
                return return if (sharedPreferences.contains(key)) {
                    sharedPreferences.getLong(key, 0)
                } else {
                    null
                }
            }
        }
    }
}