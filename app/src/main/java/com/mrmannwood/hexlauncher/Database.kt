package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.applist.AppDataDecoration
import com.mrmannwood.preferences.PreferenceDao
import com.mrmannwood.preferences.PreferenceData

@Database(
    version = 9,
    entities = [
        AppData::class,
        AppDataDecoration::class,
        PreferenceData::class
    ]
)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
    abstract fun preferencesDao() : PreferenceDao
}