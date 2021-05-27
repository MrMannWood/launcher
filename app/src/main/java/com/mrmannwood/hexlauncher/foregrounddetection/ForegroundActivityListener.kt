package com.mrmannwood.hexlauncher.foregrounddetection

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.annotation.MainThread

@MainThread
object ForegroundActivityListener {

    private val foregroundUpdateListeners = mutableListOf<(Boolean) -> Unit>()
    private val createdActivities = mutableListOf<Activity>()
    private val startedActivities = mutableListOf<Activity>()
    private val resumedActivities = mutableListOf<Activity>()

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(lifecycleCallbacks)
    }

    fun registerForegroundUpdateListener(listener: (isInForeground: Boolean) -> Unit) {
        foregroundUpdateListeners.add(listener)
    }

    fun unregisterForegroundUpdateListener(listener: (isInForeground: Boolean) -> Unit) {
        foregroundUpdateListeners.remove(listener)
    }

    private val lifecycleCallbacks = object :  Application.ActivityLifecycleCallbacks {

        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            createdActivities.add(activity)
        }

        override fun onActivityStarted(activity: Activity) {
            startedActivities.add(activity)
            if (startedActivities.size == 1) {
                foregroundUpdateListeners.forEach { it.invoke(true) }
            }
        }

        override fun onActivityResumed(activity: Activity) {
            resumedActivities.add(activity)
        }

        override fun onActivityPaused(activity: Activity) {
            resumedActivities.remove(activity)
        }

        override fun onActivityStopped(activity: Activity) {
            startedActivities.remove(activity)
            if (startedActivities.isEmpty()) {
                foregroundUpdateListeners.forEach { it.invoke(false) }
            }
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) { }

        override fun onActivityDestroyed(activity: Activity) {
        createdActivities.remove(activity)
    }
    }

    fun forCurrentForegroundActivity(
        noForegroundActivity: () -> Unit = {},
        activityExists: (Activity) -> Unit
    ) {
        when {
            resumedActivities.isNotEmpty() -> {
                activityExists(resumedActivities[0])
            }
            startedActivities.isNotEmpty() -> {
                activityExists(startedActivities[0])
            }
            else -> {
                noForegroundActivity()
            }
        }
    }
}