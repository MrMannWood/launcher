package com.mrmannwood.hexlauncher.contacts

import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.mrmannwood.launcher.R

class ContactsDatabindingAdapter (
    private val resources: Resources
) {

    private val blackDrawable by lazy {
        ColorDrawable(ResourcesCompat.getColor(resources, R.color.black, null))
    }

    fun getContactPictureContentDescription(contact: ContactData) =
        resources.getString(R.string.contact_image_content_description, contact.name)

    fun getContactImageSource(contact: ContactData) = contact.image ?: blackDrawable

    fun getContactLetterVisibility(contact: ContactData) = if (contact.image == null) View.VISIBLE else View.GONE

    fun getContactLetter(contact: ContactData) =
            contact.name
                    .split(' ')
                    .map { it[0] }
                    .joinToString(
                            separator = "",
                            limit = 4,
                            truncated = ""
                    ) { it.toString() }
}