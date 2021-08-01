package com.mrmannwood.hexlauncher.applist

import androidx.room.Embedded
import androidx.room.Relation

data class DecoratedAppData(
    @Embedded val appData: AppData,
    @Relation(
        parentColumn = "package_name",
        entityColumn = "package_name_dec"
    ) val decoration: AppDataDecoration
)