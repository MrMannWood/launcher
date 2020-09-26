package com.example.testapp

sealed class LoadResult<Result, Error> {
    abstract fun result(): Result?
    abstract fun error(): Error?

    class Result<R, E>(private val result: R): LoadResult<R, E>() {
        override fun result(): R = result
        override fun error(): E? = null
    }

    class Error<R, E>(private val error: E): LoadResult<R, E>() {
        override fun result(): R? = null
        override fun error(): E = error
    }
}