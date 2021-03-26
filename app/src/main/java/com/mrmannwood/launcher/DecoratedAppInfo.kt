package com.mrmannwood.launcher

import android.content.Context
import android.content.Intent
import android.widget.Toast

sealed class DecoratedAppInfo {

    abstract val span: Int

    class AppInfoWrapper(val appInfo : AppInfo) : DecoratedAppInfo() {
        override val span: Int = 1

        fun startApplication(context: Context) {
            context.packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let { intent ->
                context.startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } ?: Toast.makeText(context, "Unable to start app", Toast.LENGTH_LONG).show()
        }
    }
}