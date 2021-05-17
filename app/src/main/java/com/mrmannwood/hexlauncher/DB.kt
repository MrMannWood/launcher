package com.mrmannwood.hexlauncher

import androidx.room.Room

object DB {

    private lateinit var db : Database

    fun init(app: LauncherApplication) {
        db = Room.databaseBuilder(
            app.applicationContext,
            Database::class.java,
            "database"
        ).build()
    }

    fun get() = db
}