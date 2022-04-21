package com.mrmannwood.hexlauncher

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.typeconverters.ManualRoomTypeConverters

@Database(version = 10, entities = [AppData::class])
@TypeConverters(ManualRoomTypeConverters::class)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
}
