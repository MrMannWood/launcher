package com.mrmannwood.hexlauncher.appupdate

import android.app.Activity
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import timber.log.Timber
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicBoolean

class AppUpdateService : Service() {

    interface InstallListener {
        fun onAppInstalled(completeInstall: () -> Unit)
        fun onInstallCancelled(retryInstall: () -> Unit)
        fun onInstallFailed(retryInstall: () -> Unit)
    }

    interface AppUpdateServiceCallbacks {
        fun setRequestCode(requestCode: Int)
        fun performUpdateCheck(activity: Activity, installListener: InstallListener)
        fun onActivityResult(activity: Activity, requestCode: Int, resultCode: Int, data: Intent?, installListener: InstallListener)
    }

    class AppUpdateBinder(val callbacks: AppUpdateServiceCallbacks) : Binder()

    private val isUpdateRequested = AtomicBoolean(false)

    override fun onBind(intent: Intent?): IBinder {
        startService(Intent(this, AppUpdateService::class.java))
        return AppUpdateBinder(object : AppUpdateServiceCallbacks {

            private val updateManager = AppUpdateManagerFactory.create(this@AppUpdateService)

            private var appUpdateRequestCode : Int = -1

            override fun setRequestCode(requestCode: Int) {
                appUpdateRequestCode = requestCode
            }

            override fun performUpdateCheck(activity: Activity, installListener: InstallListener) {
                if (isUpdateRequested.compareAndSet(false, true)) {
                    startUpdateRequest(WeakReference(activity), WeakReference(installListener))
                }
            }

            override fun onActivityResult(
                activity: Activity,
                requestCode: Int,
                resultCode: Int,
                data: Intent?,
                installListener: InstallListener
            ) {
                if (appUpdateRequestCode != requestCode) {
                    return
                }
                onUpdateRequestResult(activity, installListener, resultCode)
            }

            private fun startUpdateRequest(
                activityRef: WeakReference<Activity>,
                installListenerRef: WeakReference<InstallListener>
            ) {
                val wasSuccessListenerCalled = AtomicBoolean(false)
                updateManager.appUpdateInfo.addOnCompleteListener { _ ->
                    if (!wasSuccessListenerCalled.get()) {
                        stop()
                    }
                }
                updateManager.appUpdateInfo.addOnFailureListener { exception ->
                    Timber.e(exception, "Update to get update")
                }
                updateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
                    wasSuccessListenerCalled.set(true)
                    when {
                        appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE -> {
                            activityRef.get()?.let { activity ->
                                updateManager.startUpdateFlowForResult(
                                    appUpdateInfo,
                                    activity,
                                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                                        .setAllowAssetPackDeletion(true)
                                        .build(),
                                    appUpdateRequestCode
                                )
                            } ?: run {
                                stop()
                            }
                        }
                        appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED -> {
                            installListenerRef.get()?.let { listener ->
                                listener.onAppInstalled { updateManager.completeUpdate() }
                            }
                            stop()
                        }
                        else -> {
                            stop()
                        }
                    }
                }
            }

            private fun onUpdateRequestResult(
                activity: Activity,
                installListener: InstallListener,
                resultCode: Int
            ) {
                when (resultCode) {
                    AppCompatActivity.RESULT_OK -> {
                        val listenerRef = WeakReference(installListener)
                        updateManager.registerListener {
                            InstallStateUpdatedListener { state ->
                                if (state.installStatus() == InstallStatus.DOWNLOADED) {
                                    listenerRef.get()?.let { listener ->
                                        listener.onAppInstalled { updateManager.completeUpdate() }
                                    } ?: run {
                                        updateManager.completeUpdate()
                                    }
                                }
                                if (state.installStatus() != InstallStatus.DOWNLOADING) {
                                    stopSelf()
                                }
                            }

                        }
                    }
                    AppCompatActivity.RESULT_CANCELED -> {
                        installListener.onInstallCancelled {
                            startUpdateRequest(WeakReference(activity), WeakReference(installListener))
                        }
                        stop()
                    }
                    ActivityResult.RESULT_IN_APP_UPDATE_FAILED -> {
                        installListener.onInstallFailed {
                            startUpdateRequest(WeakReference(activity), WeakReference(installListener))
                        }
                        stop()
                    }
                }
            }

            private fun stop() {
                isUpdateRequested.set(false)
                stopSelf()
            }
        })
    }
}