package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.applist.AppDataDecoration

@Database(version = 8, entities = [AppData::class, AppDataDecoration::class])
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
}