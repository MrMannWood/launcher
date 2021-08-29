package com.mrmannwood.preferences

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preference_data")
class PreferenceData (
    @PrimaryKey @ColumnInfo(name = "key") val key: String,
    @ColumnInfo(name = "value", typeAffinity = ColumnInfo.BLOB) val value: ByteArray? = null
)
