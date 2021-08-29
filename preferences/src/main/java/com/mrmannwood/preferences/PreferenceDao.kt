package com.mrmannwood.preferences

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy.REPLACE
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Insert(onConflict = REPLACE)
    suspend fun setPreference(preferenceData: PreferenceData)

    @Query("SELECT * FROM preference_data WHERE `key`=:key")
    fun watchPreference(key: String): Flow<PreferenceData?>

    @Query("SELECT * FROM preference_data WHERE `key`=:key")
    suspend fun getPreference(key: String): PreferenceData?
}