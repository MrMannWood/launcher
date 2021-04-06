package com.mrmannwood.hexlauncher.coroutine

import androidx.annotation.CallSuper
import androidx.lifecycle.LiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

open class LiveDataWithCoroutineScope<T> : LiveData<T>() {

    var scope : CoroutineScope? = null

    @CallSuper
    override fun onActive() {
        super.onActive()
        scope = MainScope()
    }

    @CallSuper
    override fun onInactive() {
        super.onInactive()
        scope!!.cancel()
    }

}