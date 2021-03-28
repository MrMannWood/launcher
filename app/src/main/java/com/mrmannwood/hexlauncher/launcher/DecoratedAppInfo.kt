package com.mrmannwood.hexlauncher.launcher

sealed class DecoratedAppInfo {

    abstract val span: Int

    class AppInfoWrapper(val appInfo : AppInfo) : DecoratedAppInfo() {
        override val span: Int = 1
    }
}