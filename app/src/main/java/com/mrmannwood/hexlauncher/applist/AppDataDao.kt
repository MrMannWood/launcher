package com.mrmannwood.hexlauncher.applist

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDataDao {
    @Query("SELECT * FROM app_data")
    fun getApps() : List<AppData>

    @Query("SELECT * FROM app_data ORDER BY label ASC")
    fun watchApps() : LiveData<List<AppData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(apps: List<AppData>)

    @Query("DELETE FROM app_data WHERE package_name IN (:packageName)")
    fun deleteAll(packageName: List<String>)
}