package com.mrmannwood.applist

import android.content.BroadcastReceiver
import android.content.pm.LauncherApps
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor

class AppListLiveData(
    private val appListManager: AppListManager,
    private val executor: Executor
) : LiveData<List<LauncherItem>>() {

    private var packagesChangedReceiver: LauncherApps.Callback? = null
    private var managedEventReceiver: BroadcastReceiver? = null

    override fun onActive() {
        super.onActive()
        queryAppList()
        packagesChangedReceiver = appListManager.registerPackagesChangedReceiver { queryAppList() }
        managedEventReceiver = appListManager.registerManagedEventReceiver { queryAppList() }
    }

    override fun onInactive() {
        super.onInactive()
        packagesChangedReceiver?.let { appListManager.unregisterPackagesChangedReceiver(it) }
        managedEventReceiver?.let { appListManager.unregisterManagedEventReceiver(it) }
        packagesChangedReceiver = null
        managedEventReceiver = null
    }

    private fun queryAppList() {
        executor.execute {
            postValue(appListManager.queryAppList())
        }
    }
}
