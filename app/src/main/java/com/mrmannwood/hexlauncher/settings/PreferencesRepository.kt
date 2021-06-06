package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object PreferencesRepository {

    private val mutex = Mutex()
    @Volatile private var prefs : SharedPreferences? = null

    suspend fun getPrefs(context: Context) : SharedPreferences {
        val appContext = context.applicationContext
        return withContext(Dispatchers.IO) {
            if (prefs != null) {
                prefs!!
            } else {
                mutex.withLock {
                    var inPrefs = prefs
                    if (inPrefs == null) {
                        inPrefs = PreferenceManager.getDefaultSharedPreferences(appContext)
                        prefs = inPrefs
                    }
                    inPrefs!!
                }
            }
        }
    }

    @FlowPreview
    suspend fun <T> watchPref(context: Context, key: String, extractor: PreferenceExtractor<T>) : Flow<T?> {
        return channelFlow {
            val prefs = getPrefs(context)
            offer(extractor.getValue(prefs, key))
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, _ ->
                offer(extractor.getValue(prefs, key))
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }
}
