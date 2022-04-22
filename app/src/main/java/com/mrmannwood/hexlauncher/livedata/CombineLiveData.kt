package com.mrmannwood.hexlauncher.livedata

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java.util.concurrent.Executor

fun <T, K, S> LiveData<T>.combineWith(
    liveData: LiveData<K>,
    executor: Executor,
    combine: (T?, K?) -> S?
): LiveData<S> = CombineLiveData(this, liveData, executor, combine)

class CombineLiveData<T, K, S>(
    source1: LiveData<T>,
    source2: LiveData<K>,
    executor: Executor,
    combine: (T?, K?) -> S?,
) : MediatorLiveData<S>() {

    private var data1: T? = null
    private var data2: K? = null

    init {
        addSource(source1) { data ->
            data1 = data
            executor.execute {
                combine(data1, data2)?.let { postValue(it) }
            }
        }
        addSource(source2) { data ->
            data2 = data
            executor.execute {
                combine(data1, data2)?.let { postValue(it) }
            }
        }
    }
}
