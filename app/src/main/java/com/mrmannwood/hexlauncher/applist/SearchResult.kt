package com.mrmannwood.hexlauncher.applist

import com.mrmannwood.hexlauncher.contacts.ContactData
import com.mrmannwood.hexlauncher.launcher.AppInfo

sealed class SearchResult {

    class App(val appInfo: AppInfo) : SearchResult()
    class Contact(val contact: ContactData) : SearchResult()

}