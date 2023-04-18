package com.mrmannwood.hexlauncher

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.FileProvider
import com.mrmannwood.applist.AppListLiveData
import com.mrmannwood.applist.AppListManager
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.applist.writeAppsToFile
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.timber.FileLoggerTree
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.concurrent.CountDownLatch

class LauncherApplication : Application() {

    companion object {
        lateinit var APPLICATION: LauncherApplication
    }

    lateinit var appListManager: AppListManager
    lateinit var appListLiveData: AppListLiveData

    override fun onCreate() {
        super.onCreate()
        APPLICATION = this
        Timber.plant(FileLoggerTree.getAndInit(this@LauncherApplication))
        if (BuildConfig.DEBUG) {
            DebugBuildModeConfiguration.onApplicationCreate(this@LauncherApplication)
        } else {
            ReleaseBuildModeConfiguration.onApplicationCreate(this@LauncherApplication)
        }
        installUncaughtExceptionHandler()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        appListManager = AppListManager(this)

        DB.get(this)

        PreferencesRepository.getPrefs(context = this@LauncherApplication) { repo ->
            repo.watchPref(
                key = PreferenceKeys.Logging.ENABLE_DISK_LOGGING,
                extractor = PreferenceExtractor.BooleanExtractor
            ).observeForever { enable ->
                if (enable == true) {
                    FileLoggerTree.get().enableDiskFlush()
                } else {
                    FileLoggerTree.get().disableDiskFlush()
                }
            }
        }

        appListLiveData = AppListLiveData(appListManager, PackageManagerExecutor)
        appListLiveData.observeForever {
            AppListUpdater.updateAppList(applicationContext, it)
        }

        CoroutineScope(Dispatchers.IO).launch {
            File(filesDir, "rage_shake").deleteRecursively()
        }
    }

    private fun installUncaughtExceptionHandler() {
        val exceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Timber.e(throwable, "An uncaught exception occurred on thread ${thread.name}")
            } catch (ignored: Throwable) {
            }
            exceptionHandler?.uncaughtException(thread, throwable)
        }
    }

    fun rageShakeThing(activity: Activity) {
        diskExecutor.execute {
            val rageShakeDir = File(filesDir, "rage_shake")
            writeAppsToFile(activity, rageShakeDir)

            val latch = CountDownLatch(1)

            FileLoggerTree.get().copyLogsTo(File(filesDir, "rage_shake")) { latch.countDown() }

            runCatching { latch.await() }

            val uris = ArrayList(
                rageShakeDir.listFiles()?.map {
                    FileProvider.getUriForFile(
                        activity, "com.mrmannwood.hexlauncher.fileprovider", it
                    )
                } ?: emptyList()
            )

            val debugInfo = """
                OS Version: ${System.getProperty("os.version")} (${Build.VERSION.INCREMENTAL})
                OS API Level: ${Build.VERSION.SDK_INT}
                Device: ${Build.DEVICE}
                Model and Product: ${Build.MODEL} ${Build.PRODUCT}
                App Version: ${getString(R.string.app_version)}
            """.trimIndent()

            activity.startActivity(
                Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.dev_email)))
                    putExtra(Intent.EXTRA_SUBJECT, "Rage Shake Report")
                    putExtra(Intent.EXTRA_STREAM, uris)
                    putExtra(
                        Intent.EXTRA_TEXT,
                        getString(R.string.rage_shake_email_body, debugInfo)
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            )
        }
    }

    private interface BuildModeConfiguration {
        fun onApplicationCreate(application: Application)
    }

    private object DebugBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate(application: Application) {
            Timber.plant(Timber.DebugTree())

//            StrictMode.setThreadPolicy(StrictMode.ThreadPolicy.Builder().detectAll().build())
//            StrictMode.setVmPolicy(StrictMode.VmPolicy.Builder().detectAll().build())
        }
    }

    private object ReleaseBuildModeConfiguration : BuildModeConfiguration {
        override fun onApplicationCreate(application: Application) {}
    }
}
