package com.mrmannwood.hexlauncher

import android.content.Context
import android.graphics.Color
import androidx.core.content.contentValuesOf
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import timber.log.Timber

object DB {

    private var db : Database? = null

    fun get(context: Context): Database {
        var database = db
        if (database == null) {
            synchronized(DB::class) {
                database = db
                if (database == null) {
                    database = Room.databaseBuilder(
                        context.applicationContext,
                        Database::class.java,
                        "database"
                    )
                        .addMigrations(MIGRATION_9_10)
                        .fallbackToDestructiveMigration()
                        .build()
                    db = database
                }
            }
        }
        return database!!
    }
}

private val MIGRATION_9_10 = object : Migration(9, 10) {
    override fun migrate(db: SupportSQLiteDatabase) {
        Timber.d("Running room migration 9 -> 10")

        db.execSQL("""
            CREATE TABLE app_data_new (
                component_name TEXT PRIMARY KEY NOT NULL,
                label TEXT NOT NULL,
                last_update_time INTEGER NOT NULL,
                background_color INTEGER NOT NULL,
                hidden INTEGER NOT NULL DEFAULT 0,
                bgc_override INTEGER,
                background_hidden INTEGER NOT NULL DEFAULT 0,
                tags TEXT
            )
        """.trimIndent())

        LauncherApplication.APPLICATION.appListManager.queryAppList()
            .forEach { launcherItem ->
                db.insert(
                    "app_data_new",
                    OnConflictStrategy.REPLACE,
                    contentValuesOf(
                        "component_name" to launcherItem.componentName.flattenToString(),
                        "label" to launcherItem.label,
                        "last_update_time" to -1,
                        "background_color" to Color.TRANSPARENT,
                        "hidden" to false,
                        "bgc_override" to null,
                        "background_hidden" to false,
                        "tags" to ""
                    )
                )
                db.execSQL("""
                    UPDATE app_data_new
                    SET
                        last_update_time = (SELECT last_update_time FROM app_data WHERE app_data.package_name = '${launcherItem.packageName}'),
                        background_color = (SELECT background_color FROM app_data WHERE app_data.package_name = '${launcherItem.packageName}'),
                        hidden = (SELECT hidden FROM app_data_decoration WHERE app_data_decoration.package_name_dec = '${launcherItem.packageName}'),
                        bgc_override = (SELECT bgc_override FROM app_data_decoration WHERE app_data_decoration.package_name_dec = '${launcherItem.packageName}'),
                        background_hidden = (SELECT background_hidden FROM app_data_decoration WHERE app_data_decoration.package_name_dec = '${launcherItem.packageName}'),
                        tags = (SELECT tags FROM app_data_decoration WHERE app_data_decoration.package_name_dec = '${launcherItem.packageName}')
                    WHERE component_name = '${launcherItem.componentName.flattenToString()}'
                """.trimIndent())
            }
        db.execSQL("DROP TABLE app_data_decoration")
        db.execSQL("DROP TABLE app_data")
        db.execSQL("ALTER TABLE app_data_new RENAME TO app_data")
    }
}