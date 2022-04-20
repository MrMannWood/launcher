package com.mrmannwood.hexlauncher

import android.graphics.Color
import androidx.core.content.contentValuesOf
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mrmannwood.hexlauncher.applist.AppData
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.applist.AppListUpdater
import com.mrmannwood.hexlauncher.typeconverters.ManualRoomTypeConverters

@Database(version = 10, entities = [AppData::class]) // TODO gonna need a conversion
@TypeConverters(ManualRoomTypeConverters::class)
abstract class Database : RoomDatabase() {
    abstract fun appDataDao() : AppDataDao
}

val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE app_data_new (
                component_name TEXT PRIMARY KEY,
                label TEXT NOT NULL,
                last_update_time INTEGER NOT NULL,
                background_color INTEGER NOT NULL,
                hidden INTEGER DEFAULT 0,
                bgc_override INTEGER,
                background_hidden INTEGER DEFAULT 0,
                tags TEXT
            )
        """.trimIndent())

        LauncherApplication.APPLICATION.appListManager.queryAppList()
            .forEach { launcherItem ->
                db.insert(
                    "app_data_new",
                    OnConflictStrategy.REPLACE,
                    contentValuesOf(
                        "component_name" to launcherItem.componentName,
                        "label" to launcherItem.label,
                        "last_update_time" to -1,
                        "background_color" to Color.TRANSPARENT,
                        "hidden" to false,
                        "bgc_override" to null,
                        "background_hidden" to false,
                        "tags" to null
                    )
                )
                db.execSQL("""
                    UPDATE app_data_new
                    SET
                        last_update_time = (SELECT last_update_time FROM app_data WHERE package_name = ${launcherItem.packageName}),
                        background_color = (SELECT background_color FROM app_data WHERE package_name = ${launcherItem.packageName}),
                        hidden = (SELECT hidden FROM app_data_decoration WHERE package_name = ${launcherItem.packageName})
                        bgc_override = (SELECT bgc_override FROM app_data_decoration WHERE package_name = ${launcherItem.packageName})
                        background_hidden = (SELECT background_hidden FROM app_data_decoration WHERE package_name = ${launcherItem.packageName})
                        tags = (SELECT tags FROM app_data_decoration WHERE package_name = ${launcherItem.packageName})
                    WHERE component_name = ${launcherItem.componentName}
                """.trimIndent())
            }
        db.execSQL("DROP TABLE app_data_decoration")
        db.execSQL("DROP TABLE app_data")
        db.execSQL("ALTER TABLE app_data_new RENAME TO app_data")
    }
}