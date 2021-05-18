package com.mrmannwood.hexlauncher.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor

class PermissionsLiveData(
        private val context: Context,
        sharedPrefsKey: String,
        val permission: String
)  : MediatorLiveData<PermissionsLiveData.PermissionsResult>() {

    private var prefGranted : Boolean? = null
    private var permissionGranted : Boolean? = null
    private val preferenceLiveData = PreferenceLiveData(sharedPrefsKey, BooleanExtractor)
    private val permissionsLiveData = object : LiveData<Boolean>() {
        override fun onActive() {
            super.onActive()
            postValue(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
        }
    }

    init {
        addSource(permissionsLiveData) { granted ->
            permissionGranted = granted
            maybePostValue()
        }
        addSource(preferenceLiveData) { granted ->
            prefGranted = granted == true
            maybePostValue()
        }
    }

    private fun maybePostValue() {
        val prefWasGranted = prefGranted ?: return
        val permissionWasGranted = permissionGranted ?: return
        postValue(
            when {
                prefWasGranted && permissionWasGranted -> PermissionsResult.Granted
                prefWasGranted && !permissionWasGranted  -> PermissionsResult.UserShouldGrantPermission
                !prefWasGranted && !permissionWasGranted  -> PermissionsResult.NotGranted
                !prefWasGranted && permissionWasGranted  -> {
                    PermissionsResult.UserShouldRevokePermission
                }
                else -> throw IllegalStateException("Illegal combination of pref and permission result: $prefWasGranted $permissionWasGranted")
            }
        )
    }

    sealed class PermissionsResult {
        object Granted : PermissionsResult()
        object NotGranted : PermissionsResult()
        object UserShouldRevokePermission : PermissionsResult()
        object UserShouldGrantPermission : PermissionsResult()
    }
}