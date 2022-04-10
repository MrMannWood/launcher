package com.mrmannwood.hexlauncher.applist

import android.content.ComponentName
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface AppDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(app: AppData): Long

    @Query("UPDATE app_data SET label = :label, last_update_time = :lastUpdateTime, background_color = :backgroundColor WHERE component_name = :componentName")
    fun update(label: String, lastUpdateTime: Long, backgroundColor: Int, componentName: ComponentName)

    @Query("SELECT * FROM app_data")
    fun getApps(): List<AppData>

    @Query("SELECT * FROM app_data JOIN app_data_decoration ON app_data.package_name = app_data_decoration.package_name_dec WHERE package_name IN (:packageNames)")
    fun getApps(packageNames: List<String>) : List<DecoratedAppData>

    @Transaction
    @Query("SELECT * FROM app_data WHERE component_name = :componentName")
    fun watchApp(componentName: ComponentName): LiveData<AppData>

    @Query("SELECT * FROM app_data")
    fun watchApps(): LiveData<List<AppData>>

    @Query("DELETE FROM app_data WHERE component_name NOT IN (:componentNames)")
    fun deleteNotIncluded(componentNames: List<ComponentName>)

    @Query("SELECT component_name, last_update_time FROM app_data")
    fun getLastUpdateTimeStamps(): List<AppData.Timestamp>

    @Query("UPDATE app_data SET bgc_override = :backgroundColor WHERE component_name = :componentName")
    fun setColorOverride(componentName: ComponentName, backgroundColor: Int)

    @Query("UPDATE app_data SET hidden = :hidden WHERE component_name = :componentName")
    fun setHidden(componentName: ComponentName, hidden: Boolean)

    @Query("UPDATE app_data SET background_hidden = :backgroundHidden WHERE component_name = :componentName")
    fun setBackgroundHidden(componentName: ComponentName, backgroundHidden: Boolean)

    @Query("UPDATE app_data SET tags = :tag WHERE component_name = :componentName")
    fun setTags(componentName: ComponentName, tag: String)
}
