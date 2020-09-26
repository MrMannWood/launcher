package com.example.testapp

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import java.lang.Exception

class MainActivityViewModel(app: Application): AndroidViewModel(app) {

    val apps: LiveData<LoadResult<List<AppInfo>, Exception>> by lazy {
        ApplicationPackageLiveData(getApplication<Application>().packageManager)
    }

}