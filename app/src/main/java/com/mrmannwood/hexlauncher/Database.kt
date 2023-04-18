package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.settings.PreferenceEntity
import com.mrmannwood.hexlauncher.settings.PreferencesDao
import com.mrmannwood.hexlauncher.typeconverters.ManualRoomTypeConverters

@Database(version = 11, entities = [AppData::class])
@TypeConverters(ManualRoomTypeConverters::class)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao(): AppDataDao
}

@Database(version = 1, entities = [PreferenceEntity::class])
abstract class PrefsDatabase : RoomDatabase() {
    abstract fun preferencesDao(): PreferencesDao
}
