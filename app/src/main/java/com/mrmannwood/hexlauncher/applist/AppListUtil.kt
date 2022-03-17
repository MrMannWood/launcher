package com.mrmannwood.hexlauncher.applist

import android.content.Context
import androidx.annotation.WorkerThread
import com.mrmannwood.hexlauncher.DB
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException

@WorkerThread
fun writeAppsToFile(context: Context, out: File) {
    if (!out.exists()) out.mkdirs()

    val apps = DB.get(context).appDataDao().getApps()
    try {
        FileWriter(File(out, "db_dump.txt")).use { writer ->
            apps.forEach { app ->
                writer.append("${app.label}:${app.packageName}:${app.lastUpdateTime}:${app.backgroundColor}")
            }
        }
    } catch (e: IOException) {
        Timber.e(e, "Error while copying database")
    }
}