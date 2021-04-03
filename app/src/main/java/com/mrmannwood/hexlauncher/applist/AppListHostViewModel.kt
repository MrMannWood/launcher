package com.mrmannwood.hexlauncher.applist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mrmannwood.hexlauncher.contacts.ContactData
import com.mrmannwood.hexlauncher.launcher.AppInfo

class AppListHostViewModel : ViewModel() {
    val supportsAppMenu = MutableLiveData<Boolean>()
    val supportsContactSearch = MutableLiveData<Boolean>()
    val contactSelected = MutableLiveData<ContactData>()
    val appSelected = MutableLiveData<AppInfo>()
    val searchButtonSelected = MutableLiveData<String>()
    val endRequested = MutableLiveData<Any?>()

}

/*
    abstract class Host<T>(private val killFragment: (T?) -> Unit) {

        private lateinit var onEndFunc: () -> Unit

        fun setOnEnd(onEnd: () -> Unit) {
            this.onEndFunc = onEnd
        }

        fun end() = end(null)

        fun end(value: T?) {
            onEndFunc()
            killFragment(value)
        }

        open fun onAppInfoBinding(view: View, appInfo: AppInfo) { }
    }
 */