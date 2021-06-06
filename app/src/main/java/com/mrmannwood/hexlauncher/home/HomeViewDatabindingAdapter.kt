package com.mrmannwood.hexlauncher.home

import android.view.View

object HomeViewDatabindingAdapter {
    fun getProgressBarVisibility(description: HomeViewDescription) = if (description.isLoading()) View.VISIBLE else View.GONE
}