package com.mrmannwood.hexlauncher.contacts

import android.content.res.Resources
import com.mrmannwood.launcher.R

class ContactsDatabindingAdapter (
    private val resources: Resources
) {

    fun getContactPictureContentDescription(contact: ContactData) =
        resources.getString(R.string.contact_image_content_description, contact.name)
}