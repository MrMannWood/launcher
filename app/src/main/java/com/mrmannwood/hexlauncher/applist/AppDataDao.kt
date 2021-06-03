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
    fun insert(app: AppData)

    @Query("DELETE FROM app_data WHERE package_name NOT IN (:packageName)")
    fun deleteNotIncluded(packageName: List<String>)

    @Query("SELECT package_name, last_update_time FROM app_data")
    fun getLastUpdateTimeStamps() : List<AppData.Timestamp>

    @Query("UPDATE app_data SET last_update_time = 0 WHERE package_name IS NOT NULL")
    fun zeroAllLastUpdateTimeStamps()
}