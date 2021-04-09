package com.mrmannwood.hexlauncher.home

interface HomeViewDescription {

    fun isLoading() : Boolean

    fun showArrangementOptions() : Boolean

    class HomeDescription(private val isLoading : Boolean) : HomeViewDescription {
        override fun isLoading(): Boolean = isLoading
        override fun showArrangementOptions(): Boolean = false
    }

    class ArrangementDescription(private val isLoading : Boolean) : HomeViewDescription {
        override fun isLoading(): Boolean = isLoading
        override fun showArrangementOptions(): Boolean = true
    }
}