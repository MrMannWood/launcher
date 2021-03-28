package com.mrmannwood.hexlauncher.contacts

import android.Manifest
import android.app.Application
import android.content.ContentResolver
import android.provider.ContactsContract
import androidx.core.content.ContentResolverCompat
import com.mrmannwood.hexlauncher.permissions.PermissionsHelper
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.Preferences
import timber.log.Timber

class ContactsLoader private constructor(private val contentResolver: ContentResolver) {

    companion object {
        private var instance: ContactsLoader? = null

        const val CONTACTS_PERMISSION = Manifest.permission.READ_CONTACTS

        private fun canAccessContacts(app: Application) : Boolean {
            return Preferences.getPrefs(app).getBoolean(PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH)
                    && PermissionsHelper.checkHasPermission(app, CONTACTS_PERMISSION)
        }

        fun tryCreate(app: Application) : ContactsLoader? {
            var loader = instance
            return loader ?: if (!canAccessContacts(app)) {
                null
            } else {
                loader = ContactsLoader(app.contentResolver)
                instance = loader
                loader
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