package com.mrmannwood.hexlauncher.permissions

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

object PermissionsHelper {

    fun checkHasPermission(context: Context, permission: String) : Boolean =
            ContextCompat.checkSelfPermission(
                    context,
                    permission
            ) == PackageManager.PERMISSION_GRANTED

}