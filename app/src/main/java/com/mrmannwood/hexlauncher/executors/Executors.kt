package com.mrmannwood.hexlauncher.executors

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

val mainThreadExecutor: Executor = object : Executor {

    private val handler = Handler(Looper.getMainLooper())

    override fun execute(task: Runnable) {
        handler.post(task)
    }

    fun executeDelayed(task: Runnable, delayMillis: Long) {
        handler.postDelayed(task, delayMillis)
    }
}

val cpuBoundTaskExecutor: Executor = Executors.newCachedThreadPool(object : ThreadFactory {
    private val count: AtomicInteger = AtomicInteger(0)
    override fun newThread(r: Runnable): Thread {
        return Thread(r).apply {
            name = "CpuBoundTaskThread-${count.getAndIncrement()}"
            priority = Thread.NORM_PRIORITY
        }
    }
})

val diskExecutor: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor { r ->
    Thread(r).apply {
        name = "DiskExecutionThread"
    }
}

class OriginalThreadCallback<T>(private val callback: (T) -> Unit) : Function<T> {

    companion object {
        private val threadHandler = ThreadLocal.withInitial {
            Looper.myLooper()?.let { looper ->
                Handler(looper)
            } ?: run {
                throw IllegalStateException("SameThreadCallback can only be used from a Looper thread")
            }
        }

        fun <T> create(callback: (T) -> Unit): (T) -> Unit {
            val c = OriginalThreadCallback(callback)
            return { c.invoke(it) }
        }
    }

    fun invoke(t: T) {
        threadHandler.get()!!.post { callback(t) }
    }
}

object InlineExecutor : Executor {
    override fun execute(command: Runnable) = command.run()
}

object PackageManagerExecutor : Executor {

    private val thread = HandlerThread("PackageManagerThread").apply { start() }
    private val handler = Handler(thread.looper)

    override fun execute(command: Runnable) {
        handler.post(command)
    }
}
