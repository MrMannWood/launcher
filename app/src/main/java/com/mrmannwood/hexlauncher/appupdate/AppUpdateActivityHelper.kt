package com.mrmannwood.hexlauncher.appupdate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import timber.log.Timber

class AppUpdateActivityHelper(
    private val appUpdateRequestCode: Int,
    private val appUpdateInstallListener: AppUpdateService.InstallListener
) {

    private var appUpdateCallbacks: AppUpdateService.AppUpdateServiceCallbacks? = null

    fun onCreate(activity: AppCompatActivity) {
        val viewModel : AutoUpdateViewModel by activity.viewModels()
        Timber.d("Marshall - onCreate")

        viewModel.autoUpdatePrefLiveData.observe(activity) { allowCheck ->
            Timber.d("Marshall - allow auto-update check? $allowCheck")
            if (true == allowCheck) {
                activity.bindService(
                    Intent(activity, AppUpdateService::class.java),
                    object : ServiceConnection {
                        override fun onServiceConnected(name: ComponentName, service: IBinder) {
                            Timber.d("Marshall - Connected from auto update service")
                            val callbacks = (service as AppUpdateService.AppUpdateBinder).callbacks
                            callbacks.setRequestCode(appUpdateRequestCode)
                            callbacks.performUpdateCheck(activity, appUpdateInstallListener)
                            appUpdateCallbacks = callbacks
                        }
                        override fun onServiceDisconnected(name: ComponentName) {
                            Timber.d("Marshall - Disconnected from auto update service")
                        }
                    },
                    Context.BIND_AUTO_CREATE
                )
            }
        }
    }

    fun onResume(activity: AppCompatActivity) {
        appUpdateCallbacks?.performUpdateCheck(activity, appUpdateInstallListener)
    }

    fun onActivityResult(activity: AppCompatActivity, requestCode: Int, resultCode: Int, data: Intent?) {
        appUpdateCallbacks?.onActivityResult(activity, requestCode, resultCode, data, appUpdateInstallListener)
    }
}