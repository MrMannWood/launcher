package com.mrmannwood.hexlauncher.applist

import androidx.annotation.WorkerThread
import com.mrmannwood.hexlauncher.DB
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException

@WorkerThread
fun writeAppsToFile(out: File) {
    if (!out.exists()) out.mkdirs()

    val apps = DB.get().appDataDao().getApps()
    try {
        FileWriter(File(out, "db_dump.txt")).use { writer ->
            apps.forEach { app ->
                writer.append("{package-name}:${app.label}:${app.foreground != null}\n")
            }
        }
    } catch (e: IOException) {
        Timber.e(e, "Error while copying database")
    }
}