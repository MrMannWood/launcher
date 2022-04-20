package com.mrmannwood.applist

import android.content.BroadcastReceiver
import android.content.Context
import androidx.lifecycle.LiveData
import java.util.concurrent.Executor

class AppListLiveData(
    context: Context,
    private val appListManager: AppListManager,
    private val executor: Executor
) : LiveData<List<LauncherItem>>() {

    private val context = context.applicationContext
    private var packagesChangedReceiver: BroadcastReceiver? = null
    private var managedEventReceiver: BroadcastReceiver? = null

    override fun onActive() {
        super.onActive()
        queryAppList()
        packagesChangedReceiver = appListManager.registerPackagesChangedReceiver { queryAppList() }
        managedEventReceiver = appListManager.registerManagedEventReceiver { queryAppList() }
    }

    override fun onInactive() {
        super.onInactive()
        packagesChangedReceiver?.let { context.unregisterReceiver(it) }
        managedEventReceiver?.let { context.unregisterReceiver(it) }
    }

    private fun queryAppList() {
        executor.execute {
            postValue(appListManager.queryAppList())
        }
    }

}