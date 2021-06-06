package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao

@Database(entities = [AppData::class], version = 2)
@TypeConverters(RoomTypeConverter::class)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
}