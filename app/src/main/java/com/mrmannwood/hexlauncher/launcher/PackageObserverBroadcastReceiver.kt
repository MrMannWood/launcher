package com.mrmannwood.hexlauncher.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class PackageObserverBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val PACKAGES_CHANGED = "PACKAGES_CHANGED"
        private val ACCEPTED_ACTIONS = listOf("android.intent.action.PACKAGE_ADDED", "android.intent.action.PACKAGE_REMOVED")
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (!ACCEPTED_ACTIONS.contains(intent.action)) {
            return
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(Intent(PACKAGES_CHANGED))
    }
}