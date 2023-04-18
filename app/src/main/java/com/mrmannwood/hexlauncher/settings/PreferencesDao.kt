package com.mrmannwood.hexlauncher.settings

import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.room.*
import com.mrmannwood.hexlauncher.executors.diskExecutor

@Dao
abstract class PreferencesDao {

    private val globalWatcher: MutableLiveData<Preference<*>?> = MutableLiveData()
    private val watchers: MutableMap<String, MutableLiveData<Preference<*>?>> = mutableMapOf()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract fun insert(preference: PreferenceEntity)

    @Query("DELETE FROM preferences WHERE key = :key")
    protected abstract fun deleteEntity(key: String)

    @Query("DELETE FROM preferences")
    abstract fun deleteAll()

    @Query("SELECT * FROM preferences WHERE key = :key")
    abstract fun getPreferenceEntity(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences")
    abstract fun getAllPreferences(): List<PreferenceEntity>

    fun delete(key: String) {
        deleteEntity(key)
        callWatchers(key, null)
    }

    fun insert(preference: Preference<*>) {
        insert(PreferenceEntity.fromPreference(preference))
        callWatchers(preference.key, preference)
    }

    fun getPreference(key: String): Preference<*>? = getPreferenceEntity(key)?.toPreference()

    fun watchPreference(key: String): LiveData<Preference<*>?> {
        val liveData = synchronized(watchers) {
            watchers.getOrPut(key) { MutableLiveData<Preference<*>?>() }
        }
        if (liveData.value == null) {
            diskExecutor.execute {
                val a = getPreference(key)
                liveData.postValue(a)
            }
        }
        return liveData
    }

    fun getString(key: String, default: String?): String? {
        val value = getPreference(key) ?: return default
        if (value !is Preference.StringPreference) throw ClassCastException("$key does not represent a String")
        return value.value
    }

    fun getBoolean(key: String, default: Boolean): Boolean {
        val value = getPreference(key) ?: return default
        if (value !is Preference.BooleanPreference) throw ClassCastException("$key does not represent a Boolean")
        return value.value
    }

    fun getInt(key: String, default: Int): Int {
        val value = getPreference(key) ?: return default
        if (value !is Preference.IntPreference) throw ClassCastException("$key does not represent a Int")
        return value.value
    }

    fun getFloat(key: String, default: Float): Float {
        val value = getPreference(key) ?: return default
        if (value !is Preference.FloatPreference) throw ClassCastException("$key does not represent a Float")
        return value.value
    }

    fun getLong(key: String, default: Long): Long {
        val value = getPreference(key) ?: return default
        if (value !is Preference.LongPreference) throw ClassCastException("$key does not represent a Long")
        return value.value
    }

    fun putString(key: String, value: String) {
        insert(Preference.StringPreference(key, value))
    }

    fun putInt(key: String, value: Int) {
        insert(Preference.IntPreference(key, value))
    }

    fun putBoolean(key: String, value: Boolean) {
        insert(Preference.BooleanPreference(key, value))
    }

    @Transaction
    open fun performInTransaction(operations: List<() -> Unit>) {
        operations.forEach { it.invoke() }
    }

    fun registerGlobalWatcher(watcher: Observer<Preference<*>?>) {
        globalWatcher.observeForever(watcher)
    }

    fun unregisterGlobalWatcher(watcher: Observer<Preference<*>?>) {
        globalWatcher.removeObserver(watcher)
    }

    private fun callWatchers(key: String, preference: Preference<*>?) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            watchers[key]?.value = preference
            globalWatcher.value = preference
        } else {
            watchers[key]?.postValue(preference)
            globalWatcher.postValue(preference)
        }
    }
}
