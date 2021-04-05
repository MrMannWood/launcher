package com.mrmannwood.hexlauncher.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.mrmannwood.hexlauncher.contacts.ContactsLiveData
import com.mrmannwood.hexlauncher.settings.PreferencesLiveData

class LauncherViewModel(app: Application): AndroidViewModel(app) {

    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get(getApplication())
    val contacts = ContactsLiveData(app)

}