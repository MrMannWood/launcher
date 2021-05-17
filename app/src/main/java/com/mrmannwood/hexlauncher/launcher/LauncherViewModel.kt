package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.Result
import com.mrmannwood.hexlauncher.contacts.ContactsLiveData

class LauncherViewModel(app: Application): AndroidViewModel(app) {
    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get()
    val contacts = ContactsLiveData(app)
}