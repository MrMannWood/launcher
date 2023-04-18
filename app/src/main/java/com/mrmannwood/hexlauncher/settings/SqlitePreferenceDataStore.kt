package com.mrmannwood.hexlauncher.settings

import androidx.preference.PreferenceDataStore

class SqlitePreferenceDataStore(
    private val preferencesDao: PreferencesDao
) : PreferenceDataStore() {

    override fun putString(key: String, value: String?) {
        if (value == null) {
            preferencesDao.delete(key)
        } else {
            preferencesDao.insert(Preference.StringPreference(key, value))
        }
    }

    override fun putInt(key: String, value: Int) {
        preferencesDao.insert(Preference.IntPreference(key, value))
    }

    override fun putLong(key: String, value: Long) {
        preferencesDao.insert(Preference.LongPreference(key, value))
    }

    override fun putFloat(key: String, value: Float) {
        preferencesDao.insert(Preference.FloatPreference(key, value))
    }

    override fun putBoolean(key: String, value: Boolean) {
        preferencesDao.insert(Preference.BooleanPreference(key, value))
    }

    override fun getString(key: String, default: String?): String? {
        val result = preferencesDao.getPreference(key) ?: return default
        if (result !is Preference.StringPreference) throw ClassCastException("$key does not represent a String")
        return result.value
    }

    override fun getInt(key: String, default: Int): Int {
        val result = preferencesDao.getPreference(key) ?: return default
        if (result !is Preference.IntPreference) throw ClassCastException("$key does not represent a String")
        return result.value
    }

    override fun getLong(key: String, default: Long): Long {
        val result = preferencesDao.getPreference(key) ?: return default
        if (result !is Preference.LongPreference) throw ClassCastException("$key does not represent a String")
        return result.value
    }

    override fun getFloat(key: String, default: Float): Float {
        val result = preferencesDao.getPreference(key) ?: return default
        if (result !is Preference.FloatPreference) throw ClassCastException("$key does not represent a String")
        return result.value
    }

    override fun getBoolean(key: String, default: Boolean): Boolean {
        val result = preferencesDao.getPreference(key) ?: return default
        if (result !is Preference.BooleanPreference) throw ClassCastException("$key does not represent a String")
        return result.value
    }
}
