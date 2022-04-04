package com.mrmannwood.hexlauncher.applist

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AppDataDao {
    @Query("SELECT * FROM app_data")
    fun getApps() : List<AppData>

    @Transaction
    @Query("SELECT * FROM app_data JOIN app_data_decoration ON app_data.package_name = app_data_decoration.package_name_dec WHERE package_name = :packageName")
    fun watchApp(packageName: String) : LiveData<DecoratedAppData>

    @Transaction
    @Query("SELECT * FROM app_data JOIN app_data_decoration ON app_data.package_name = app_data_decoration.package_name_dec ORDER BY app_data.label ASC")
    fun watchApps() : LiveData<List<DecoratedAppData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(app: AppData)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(appDataDecoration: AppDataDecoration)

    @Query("DELETE FROM app_data WHERE package_name NOT IN (:packageName)")
    fun deleteNotIncluded(packageName: List<String>)

    @Query("DELETE FROM app_data_decoration WHERE package_name_dec NOT IN (:packageName)")
    fun deleteNotIncludedDecoration(packageName: List<String>)

    @Query("SELECT package_name, last_update_time FROM app_data")
    fun getLastUpdateTimeStamps() : List<AppData.Timestamp>

    @Query("UPDATE app_data_decoration SET bgc_override = :backgroundColor WHERE package_name_dec = :packageName")
    fun setColorOverride(packageName: String, backgroundColor: Int)

    @Query("UPDATE app_data_decoration SET hidden = :hidden WHERE package_name_dec = :packageName")
    fun setHidden(packageName: String, hidden: Boolean)

    @Query("UPDATE app_data_decoration SET background_hidden = :backgroundHidden WHERE package_name_dec = :packageName")
    fun setBackgroundHidden(packageName: String, backgroundHidden: Boolean)

    @Query("UPDATE app_data_decoration SET tags = :tag WHERE package_name_dec = :packageName")
    fun setTags(packageName: String, tag: String)
}