package com.mrmannwood.hexlauncher.appupdate

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.tasks.Task
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferenceLiveData
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData
import com.mrmannwood.launcher.R
import timber.log.Timber
import java.lang.IllegalStateException
import java.lang.ref.WeakReference
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicBoolean

class AppUpdateService : Service() {

    companion object {
        val ACTIVITY_HANDLER = ActivityHandler()
        var SERVICE_HANDLER : ServiceHandler? = null
    }

    object MessageForService {
        const val USER_DENIED_UPDATE = -1
        const val START = 1
        const val UPDATE_ALLOWED = 2
        const val COMPLETE_UPDATE = 3
        const val UPDATE_FAILED = 4
    }

    object MessageForActivity {
        const val UPDATE_NOT_PROCEEDING = -2
        const val UPDATE_AVAILABLE = 1
        const val UPDATE_DOWNLOADED = 2
        const val UPDATE_FAILED = 3
    }

    class ActivityHandler : Handler(Looper.getMainLooper()) {

        interface AppUpdateListener {
            fun updateNotProceeding()
            fun updateAvailable(prompt: (Activity, Int) -> Unit)
            fun updateDownloaded()
            fun updateFailed()
        }

        private val listeners : MutableList<WeakReference<AppUpdateListener>> = CopyOnWriteArrayList()

        fun attachListener(listener: AppUpdateListener) {
            listeners.add(WeakReference(listener))
        }

        fun removeListener(listener: AppUpdateListener) {
            listeners.removeIf { it.get() == listener }
        }

        override fun handleMessage(msg: Message) {
            listeners.removeIf { it.get() == null }
            when(msg.arg1) {
                MessageForActivity.UPDATE_NOT_PROCEEDING -> call { updateNotProceeding() }
                MessageForActivity.UPDATE_AVAILABLE ->  call {
                    updateAvailable { activity, requestCode ->
                        val (updateManager, appUpdateInfo) = (msg.obj as Pair<AppUpdateManager, AppUpdateInfo>)
                        updateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            activity,
                            AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE)
                                .setAllowAssetPackDeletion(true)
                                .build(),
                            requestCode
                        )
                    }
                }
                MessageForActivity.UPDATE_DOWNLOADED ->  call { updateDownloaded() }
                MessageForActivity.UPDATE_FAILED ->  call { updateFailed() }
            }
        }

        private fun call(func: AppUpdateListener.() -> Unit) {
            listeners.forEach { ref -> ref.get()?.let { listener -> func(listener) } }
        }
    }

    inner class ServiceHandler(looper: Looper) : Handler(looper) {

        private var canAutoUpdate : Boolean? = null

        fun sendMessageToService(message: Int) {
            sendMessage(Message.obtain().apply {
                arg1 = message
            })
        }

        override fun handleMessage(msg: Message) {
            if (!checkCanAutoUpdate()) {
                stopSelf()
                return
            }

            when (msg.arg1) {
                MessageForService.START -> start()
                MessageForService.UPDATE_ALLOWED -> updateAllowed()
                MessageForService.COMPLETE_UPDATE -> completeUpdate()
                MessageForService.USER_DENIED_UPDATE -> userDeniedUpdate()
                MessageForService.UPDATE_FAILED -> updateFailed()
                else -> {
                    stopSelf()
                    throw IllegalStateException("Unknown ask for service: ${msg.arg1}")
                }
            }
        }

        private fun checkCanAutoUpdate() : Boolean{
            var isAllowed = canAutoUpdate
            if (isAllowed == null) {
                isAllowed = PreferencesLiveData.get().getSharedPreferences().getBoolean(PreferenceKeys.AutoUpdate.ALLOW_AUTO_UPDATE, false)
                canAutoUpdate = isAllowed
            }
            return isAllowed
        }

        private fun updateAllowed() {
            updateManager.registerListener {
                InstallStateUpdatedListener { state ->
                    if(!handleInstallStatusUpdate(state.installStatus())) {
                        stopSelf()
                    }
                }
            }
        }

        private fun userDeniedUpdate() {
            //TODO write to shared prefs that user doesn't want to update
            stopSelf()
        }

        private fun completeUpdate() {
            updateManager.completeUpdate()
            stopSelf()
        }

        private fun updateFailed() {
            sendMessageToActivity(MessageForActivity.UPDATE_NOT_PROCEEDING)
            stopSelf()
        }

        private fun start() {
            updateManager = AppUpdateManagerFactory.create(this@AppUpdateService)
            val updateTask = updateManager.appUpdateInfo

            val wasSuccessCalled = AtomicBoolean(false)
            updateTask.addOnSuccessListener { info ->
                wasSuccessCalled.set(true)
                if(!handleUpdateAvailabilityUpdate(info)) {
                    if (!handleInstallStatusUpdate(info.installStatus())) {
                        stopSelf()
                    }
                }
            }
            updateTask.addOnFailureListener { e -> Timber.e(e, "Failed to get update") }
            updateTask.addOnCompleteListener {
                if (!wasSuccessCalled.get()) {
                    stopSelf()
                }
            }
        }

        private fun handleUpdateAvailabilityUpdate(info: AppUpdateInfo) : Boolean {
            return when(info.updateAvailability()) {
                UpdateAvailability.UPDATE_NOT_AVAILABLE -> {
                    stopSelf()
                    true
                }
                UpdateAvailability.UPDATE_AVAILABLE -> {
                    sendMessageToActivity(MessageForActivity.UPDATE_AVAILABLE) {
                        obj = Pair(updateManager, info)
                    }
                    true
                }
                else -> false
            }
        }

        private fun handleInstallStatusUpdate(installStatus: Int) : Boolean {
            return when (installStatus) {
                InstallStatus.DOWNLOADING -> { true }
                InstallStatus.DOWNLOADED -> {
                    sendMessageToActivity(MessageForActivity.UPDATE_DOWNLOADED)
                    true
                }
                InstallStatus.FAILED -> {
                    sendMessageToActivity(MessageForActivity.UPDATE_FAILED)
                    stopSelf()
                    true
                }
                InstallStatus.CANCELED -> { stopSelf(); true }
                InstallStatus.INSTALLING -> { stopSelf(); true }
                InstallStatus.INSTALLED -> { stopSelf(); true }
                else -> false
            }
        }

        private fun sendMessageToActivity(message: Int, apply: Message.() -> Unit = {}) {
            ACTIVITY_HANDLER.sendMessage(Message.obtain().apply {
                arg1 = message
                apply(this)
            })
        }
    }

    private lateinit var updateManager : AppUpdateManager

    override fun onCreate() {
        super.onCreate()
        startForeground()

        HandlerThread("AppUpdateThread", Process.THREAD_PRIORITY_BACKGROUND).apply {
            start()
            ServiceHandler(looper).apply {
                sendMessageToService(MessageForService.START)
                SERVICE_HANDLER = this
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        SERVICE_HANDLER?.looper?.quitSafely()
        SERVICE_HANDLER = null
    }

    private fun startForeground() {
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(
                NotificationChannel(
                    "app_update",
                    getString(R.string.app_update_notification_channel_name),
                    NotificationManager.IMPORTANCE_LOW
                )
            )

        startForeground(
            1,
            Notification.Builder(this, "app_update")
                .setContentTitle(getString(R.string.app_update_notification_title))
                .build()
        )
    }
}