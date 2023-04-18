package com.mrmannwood.hexlauncher.settings

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.executors.diskExecutor
import java.util.concurrent.CountDownLatch

class PreferencesRepository private constructor(
    val dao: PreferencesDao
) {

    companion object {
        @Volatile
        private var prefs: PreferencesRepository? = null

        fun getPrefsBlocking(context: Context): PreferencesRepository {
            var prefs = this.prefs
            if (prefs != null) return prefs

            val lock = CountDownLatch(1)
            getPrefs(context) { prefs = it; lock.countDown() }
            lock.await()
            return prefs!!
        }

        fun getPrefs(context: Context, callback: (PreferencesRepository) -> Unit) {
            val appContext = context.applicationContext
            diskExecutor.execute {
                val prefs = this.prefs
                if (prefs != null) {
                    callback(prefs)
                } else {
                    val prefs: PreferencesRepository = synchronized(this) {
                        var prefs = this.prefs
                        if (prefs == null) {
                            prefs = PreferencesRepository(
                                DB.getPrefsDatabase(appContext).preferencesDao()
                            )
                            this.prefs = prefs
                            convert(prefs.dao)
                            prefs
                        } else {
                            prefs
                        }
                    }
                    callback(prefs)
                }
            }
        }

        private fun convert(prefs: PreferencesDao) {
            arrayOf("home_widget_date_position", "home_widget_time_position")
                .mapNotNull { key ->
                    try {
                        val value = prefs.getPreference(key)
                        if (value is Preference.FloatPreference) {
                            key to value.value
                        } else {
                            null
                        }
                    } catch (e: ClassCastException) {
                        // we've already run the conversion and can ignore
                        null
                    }
                }
                .forEach { (key, value) ->
                    prefs.delete(key)
                    prefs.insert(Preference.StringPreference(key, "null,$value"))
                }
        }
    }

    fun <T> watchPref(key: String, extractor: PreferenceExtractor<T>): LiveData<T?> {
        return dao.watchPreference(key).map {
            it?.let { extractor.getValue(it) }
        }
    }
}
