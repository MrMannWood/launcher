package com.mrmannwood.hexlauncher.applist

import com.mrmannwood.hexlauncher.DB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException

suspend fun writeAppsToFile(out: File) {
    withContext(Dispatchers.IO) {
        if (!out.exists()) out.mkdirs()

        val apps = DB.get().appDataDao().getApps()
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
}