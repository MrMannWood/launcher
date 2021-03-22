package com.example.testapp.launcher

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.lang.Exception

class LauncherViewModel(app: Application): AndroidViewModel(app) {

    val apps: LiveData<Result<List<AppInfo>>> = AppInfoLiveData.get(getApplication())

}