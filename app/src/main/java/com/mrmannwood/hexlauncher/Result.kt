package com.mrmannwood.hexlauncher

sealed class Result<T> {

    companion object {
        fun <T> success(result: T) : Result<T> = Success(result)
        fun <T> failure(error: Throwable) : Result<T> = Failure(error)
    }

    abstract fun <R> onSuccess(func: (T) -> R) : R?
    abstract fun <R> onFailure(func: (Throwable) -> R) : R?

    private class Success<T>(private val result: T) : Result<T>() {
        override fun <R> onSuccess(func: (T) -> R) : R {
            return func(result)
        }
        override fun <R> onFailure(func: (Throwable) -> R) : Nothing? = null
    }
    private class Failure<T>(private val error: Throwable) : Result<T>() {
        override fun <R> onSuccess(func: (T) -> R) : Nothing? = null
        override fun <R> onFailure(func: (Throwable) -> R) : R {
            return func(error)
        }
    }
}