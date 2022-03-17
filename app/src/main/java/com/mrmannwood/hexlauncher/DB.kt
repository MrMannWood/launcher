package com.mrmannwood.hexlauncher

import android.content.Context
import androidx.room.Room

object DB {

    private var db : Database? = null

    fun get(context: Context): Database {
        var database = db
        if (database == null) {
            synchronized(DB::class) {
                database = db
                if (database == null) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        Database::class.java,
                        "database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    db = database
                }
            }
        }
        return database!!
    }
}