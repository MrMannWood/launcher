package com.mrmannwood.hexlauncher.settings

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.preferences.PreferenceDef
import com.mrmannwood.preferences.PreferenceRepository
import com.mrmannwood.preferences.PreferenceType
import com.mrmannwood.preferences.PreferenceValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

object PreferencesRepository {

    private val mutex = Mutex()
    @Volatile private var prefs : SharedPreferences? = null

    suspend fun maybeConvertLegacyPrefs(context: Context) {
        withContext(Dispatchers.IO) {
            val prefs = getPrefs(context)
            if (!shouldConvertPrefs(prefs)) {
                return@withContext
            }

            PreferenceKeys.allPreferences.forEach { prefDef ->
                when (prefDef.type) {
                    is PreferenceType.STRING -> {
                        writePref(
                            prefDef as PreferenceDef<String>,
                            prefs.getString(prefDef.name, prefDef.defaultValue))
                    }
                    is PreferenceType.BOOLEAN -> {
                        writePref(
                            prefDef as PreferenceDef<Boolean>,
                            prefs.getBoolean(prefDef.name, prefDef.defaultValue as Boolean)
                        )
                    }
                    is PreferenceType.FLOAT -> {
                        writePref(
                            prefDef as PreferenceDef<Float>,
                            prefs.getFloat(prefDef.name, prefDef.defaultValue as Float)
                        )
                    }
                    is PreferenceType.INT -> {
                        writePref(
                            prefDef as PreferenceDef<Int>,
                            prefs.getInt(prefDef.name, prefDef.defaultValue as Int)
                        )
                    }
                    is PreferenceType.LONG -> {
                        writePref(
                            prefDef as PreferenceDef<Long>,
                            prefs.getLong(prefDef.name, prefDef.defaultValue as Long)
                        )
                    }
                }
            }
        }
    }

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

    fun <T> watchPref(context: Context, key: PreferenceDef<T>, extractor: PreferenceExtractor<T?>) : Flow<T?> {
        return watchPref(key).transform { emit(it.value) }
    }

    suspend fun <T> writePref(pref: PreferenceDef<T>, value: T?) {
        PreferenceRepository(DB.get().preferencesDao()).writePref(PreferenceValue(pref, value))
    }

    fun <T> watchPref(pref: PreferenceDef<T>): Flow<PreferenceValue<T>> {
        return PreferenceRepository(DB.get().preferencesDao()).watchPref(pref)
    }

    suspend fun <T> getPref(pref: PreferenceDef<T>): T? {
        return PreferenceRepository(DB.get().preferencesDao()).getPref(pref).value
    }

    private suspend fun shouldConvertPrefs(prefs: SharedPreferences): Boolean {
        val version = prefs.getString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME.name, "0.0.0")!!.split(".")
        val v = intArrayOf(1, 2, 19)
        version.forEachIndexed { index, part ->
            if (v[index] > part.toInt()) {
                return false
            }
        }
        return true
    }
}
