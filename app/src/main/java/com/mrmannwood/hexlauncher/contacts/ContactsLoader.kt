package com.mrmannwood.hexlauncher.contacts

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentResolverCompat
import timber.log.Timber

class ContactsLoader private constructor(private val contentResolver: ContentResolver) {

    companion object {
        private var instance: ContactsLoader? = null

        fun tryCreate(app: Application) : ContactsLoader? {
            var loader = instance
            return loader
                ?: if (app.checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                    null
                } else {
                    loader = ContactsLoader(app.contentResolver)
                    instance = loader
                    loader
                }

        }

        fun askForPermission(activity: Activity, requestCode: Int) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), requestCode)
            }
        }
    }

    fun loadContacts(search: String, result: (List<ContactData>) -> Unit) {
        ContentResolverCompat.query(
                contentResolver,
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                    ContactsContract.Data.CONTACT_ID,
                    ContactsContract.Data.DISPLAY_NAME_PRIMARY
                ),
                "${ContactsContract.Data.DISPLAY_NAME_PRIMARY} LIKE ?",
                arrayOf("%$search%"),
                null,
                null
        ).use {
            val names = ArrayList<ContactData>(it.count)
            Timber.d("Marshall: found ${it.count} contacts")
            while (it.moveToNext()) {
                names.add(
                    ContactData(
                        id = it.getInt(it.getColumnIndex(ContactsContract.Data.CONTACT_ID)),
                        name = it.getString(it.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                    )
                )
            }
            result(names)
        }
    }

}