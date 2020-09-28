package com.example.testapp.launcher

sealed class DecoratedAppInfo {

    class AppInfoWrapper(val appInfo : AppInfo) : DecoratedAppInfo()

    object FullSpan : DecoratedAppInfo()
}