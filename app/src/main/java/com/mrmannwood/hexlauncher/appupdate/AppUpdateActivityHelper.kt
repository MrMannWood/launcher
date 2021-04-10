package com.mrmannwood.hexlauncher.appupdate

import android.app.Activity
import android.content.Intent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.google.android.play.core.install.model.ActivityResult
import com.mrmannwood.hexlauncher.appupdate.AppUpdateService.MessageForService

class AppUpdateActivityHelper(
    private val activity: AppCompatActivity,
    private val appUpdateRequestCode: Int,
    private val appUpdateInstallListener: AppUpdateListener
) {

    interface AppUpdateListener {
        fun onUpdateNotProceeding()
        fun onUpdateFailed()
        fun onUpdateReadyForInstall(completeInstall: () -> Unit)
    }

    init {
        activity.lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                when (event) {
                    Lifecycle.Event.ON_CREATE -> onCreate()
//                    Lifecycle.Event.ON_RESUME -> onResume()
                    Lifecycle.Event.ON_DESTROY -> onDestroy()
                }
            }
        })
    }

    private val appUpdateListener = object : AppUpdateService.ActivityHandler.AppUpdateListener {
        override fun updateNotProceeding() {
            appUpdateInstallListener.onUpdateNotProceeding()
        }

        override fun updateFailed() {
            appUpdateInstallListener.onUpdateFailed()
        }

        override fun updateAvailable(prompt: (Activity, Int) -> Unit) {
            prompt(activity, appUpdateRequestCode)
        }

        override fun updateDownloaded() {
            appUpdateInstallListener.onUpdateReadyForInstall {
                AppUpdateService.SERVICE_HANDLER?.sendMessageToService(MessageForService.COMPLETE_UPDATE)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int) {
        if (appUpdateRequestCode != requestCode) return
        when (resultCode) {
            AppCompatActivity.RESULT_CANCELED -> {
                AppUpdateService.SERVICE_HANDLER?.sendMessageToService(MessageForService.USER_DENIED_UPDATE)
            }
            ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                AppUpdateService.SERVICE_HANDLER?.sendMessageToService(MessageForService.UPDATE_FAILED)
            }
            AppCompatActivity.RESULT_OK -> {
                AppUpdateService.SERVICE_HANDLER?.sendMessageToService(MessageForService.UPDATE_ALLOWED)
            }
        }
    }

    private fun onCreate() {
        val viewModel : AutoUpdateViewModel by activity.viewModels()
        viewModel.autoUpdatePrefLiveData.observe(
            activity,
            object : Observer<Boolean?> {
                override fun onChanged(allowCheck: Boolean?) {
                    viewModel.autoUpdatePrefLiveData.removeObserver(this)
                    if (true == allowCheck) {
                        AppUpdateService.ACTIVITY_HANDLER.attachListener(appUpdateListener)
                        activity.startForegroundService(
                            Intent(
                                activity,
                                AppUpdateService::class.java
                            )
                        )
                    }
                }
            }
        )
    }

    private fun onDestroy() {
        AppUpdateService.ACTIVITY_HANDLER.removeListener(appUpdateListener)
    }

    private fun onResume() {
        // TODO consider checking if download is complete
    }
}
