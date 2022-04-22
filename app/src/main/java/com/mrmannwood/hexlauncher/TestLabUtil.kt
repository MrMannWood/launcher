package com.mrmannwood.hexlauncher

import android.content.Context
import android.provider.Settings

object TestLabUtil {

    private var isTestLab: Boolean? = null

    fun isTestLab(context: Context): Boolean {
        var isTL = isTestLab
        if (isTL == null) {
            isTL = "true" == Settings.System.getString(context.contentResolver, "firebase.test.lab")
            isTestLab = isTL
        }
        return isTL
    }
}
