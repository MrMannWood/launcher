package com.mrmannwood.hexlauncher.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor
import com.mrmannwood.launcher.BuildConfig

class PermissionsLiveData(
        private val context: Context,
        val sharedPrefsKey: String,
        val permission: String
)  : MediatorLiveData<PermissionsLiveData.PermissionsResult>() {

    private var prefGranted : Boolean? = null
    private var permissionGranted : Boolean? = null

    private val preferenceLiveData = PreferenceLiveData(sharedPrefsKey, BooleanExtractor)
    private val permissionsLiveData = object : LiveData<Boolean>() {
        override fun onActive() {
            super.onActive()
            if (BuildConfig.DEBUG) {
                postValue(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            } else {
                postValue(false)
            }
        }
    }

    override fun onActive() {
        super.onActive()
        addSource(preferenceLiveData) { granted ->
            prefGranted = granted == true
            maybePostValue()
        }
        addSource(permissionsLiveData) { granted ->
            permissionGranted = granted
            maybePostValue()
        }
    }

    override fun onInactive() {
        super.onInactive()
        removeSource(preferenceLiveData)
        removeSource(permissionsLiveData)
    }

    private fun maybePostValue() {
        val prefWasGranted = prefGranted
        val permissionWasGranted = permissionGranted
        if (prefWasGranted == null || permissionWasGranted == null) {
            return
        }
        postValue(
                when {
                    prefWasGranted && permissionWasGranted -> PermissionsResult.PrefGrantedPermissionGranted
                    prefWasGranted && !permissionWasGranted  -> PermissionsResult.PrefGrantedPermissionDenied
                    !prefWasGranted && !permissionWasGranted  -> PermissionsResult.PrefDeniedPermissionDenied
                    !prefWasGranted && permissionWasGranted  -> PermissionsResult.PrefDeniedPermissionGranted
                    else -> throw IllegalStateException("Illegal combination of pref and permission result: $prefWasGranted $permissionWasGranted")
                }
        )
    }

    sealed class PermissionsResult {
        object PrefGrantedPermissionGranted : PermissionsResult()
        object PrefGrantedPermissionDenied : PermissionsResult()
        object PrefDeniedPermissionDenied : PermissionsResult()
        object PrefDeniedPermissionGranted : PermissionsResult()
    }
}