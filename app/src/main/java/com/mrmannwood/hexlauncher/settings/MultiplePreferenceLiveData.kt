package com.mrmannwood.hexlauncher.settings

import androidx.lifecycle.MediatorLiveData

class MultiplePreferenceLiveData(
        private val preferences: Map<String, PreferenceLiveData.Extractor<*>>
) : MediatorLiveData<Map<String, Any?>>() {

    private val results = mutableMapOf<String, Any?>()

    override fun onActive() {
        super.onActive()
        preferences.forEach { (key, extractor) ->
            addSource(PreferenceLiveData(key, extractor)) { result ->
                results[key] = result
                postIfAllKeysPresent()
            }
        }
    }

    private fun postIfAllKeysPresent() {
        val result = mutableMapOf<String, Any?>()
        for (key in preferences.keys) {
            if(!result.containsKey(key)) {
                return
            }
            result[key] = result[key]
        }

        postValue(result.toMap())
    }
}