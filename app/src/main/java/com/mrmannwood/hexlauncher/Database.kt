package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao

@Database(entities = [AppData::class], version = 2)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
}