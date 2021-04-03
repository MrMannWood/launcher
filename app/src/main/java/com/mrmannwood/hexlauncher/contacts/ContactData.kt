package com.mrmannwood.hexlauncher.contacts

import android.graphics.drawable.Drawable
import android.net.Uri

data class ContactData(
    val id: Long,
    val name: String,
    val image: Drawable?,
    val uri: Uri
)