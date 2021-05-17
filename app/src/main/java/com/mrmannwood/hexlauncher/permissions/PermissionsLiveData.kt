package com.mrmannwood.hexlauncher.permissions

import android.annotation.TargetApi
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData.Extractor.BooleanExtractor
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import com.mrmannwood.launcher.BuildConfig

class PermissionsLiveData(
        private val context: Context,
        sharedPrefsKey: String,
        val permission: String
)  : MediatorLiveData<PermissionsLiveData.PermissionsResult>() {

    private val preferenceLiveData = PreferenceLiveData(sharedPrefsKey, BooleanExtractor)

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            addSource(PermissionsCheckingLiveData(context, permission, preferenceLiveData)) {
                postValue(it)
            }
        } else {
            addSource(NoPermissionsLiveData(preferenceLiveData)) {
                postValue(it)
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private class PermissionsCheckingLiveData(
        context: Context,
        val permission: String,
        preferenceLiveData: LiveData<Boolean?>
    ) : MediatorLiveData<PermissionsResult>() {

        private val permissionsLiveData = object : LiveData<Boolean>() {
            override fun onActive() {
                super.onActive()
                postValue(ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED)
            }
        }

        private var prefGranted : Boolean? = null
        private var permissionGranted : Boolean? = null

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
    }

    private class NoPermissionsLiveData(preferenceLiveData: LiveData<Boolean?>) : MediatorLiveData<PermissionsResult>() {
        init {
            addSource(preferenceLiveData) { granted ->
                postValue(
                    if(granted == true)
                        PermissionsResult.Granted
                    else
                        PermissionsResult.NotGranted
                )
            }
        }
    }

    sealed class PermissionsResult {
        object Granted : PermissionsResult()
        object NotGranted : PermissionsResult()
        object UserShouldRevokePermission : PermissionsResult()
        object UserShouldGrantPermission : PermissionsResult()
    }
}