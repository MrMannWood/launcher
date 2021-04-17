package com.mrmannwood.hexlauncher.home

import android.app.Application
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.mrmannwood.launcher.R

class HomeViewDatabindingAdapter(private val app: Application) {

    fun getProgressBarVisibility(description: HomeViewDescription) = if (description.isLoading()) View.VISIBLE else View.GONE

    fun getSlotsVisibility(description: HomeViewDescription) = if (description.isLoading()) View.INVISIBLE else View.VISIBLE

    fun getOddSlotBackgroundColor(description: HomeViewDescription) : Int {
        return if (description.showArrangementOptions()) {
            ResourcesCompat.getColor(app.resources, R.color.white_translucent, null)
        } else {
            ResourcesCompat.getColor(app.resources, android.R.color.transparent, null)
        }
    }

    fun getEvenSlotBackgroundColor(description: HomeViewDescription) : Int {
        return if (description.showArrangementOptions()) {
            ResourcesCompat.getColor(app.resources, R.color.black_translucent, null)
        } else {
            ResourcesCompat.getColor(app.resources, android.R.color.transparent, null)
        }
    }

    fun getContentDescriptionForSlot(slot: Int) : String {
        return app.getString(R.string.home_arrangement_content_description_slot, slot)
    }
}