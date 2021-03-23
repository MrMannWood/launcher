package com.example.testapp.executor

import java.util.concurrent.Executor
import java.util.concurrent.Executors

object AppExecutors {

    val backgroundExecutor: Executor by lazy {
        Executors.newScheduledThreadPool(1)
    }

}