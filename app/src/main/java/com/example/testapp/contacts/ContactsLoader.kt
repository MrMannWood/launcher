package com.example.testapp.contacts

import android.app.Application
import android.provider.ContactsContract
import androidx.core.content.ContentResolverCompat

object ContactsLoader {

    fun loadContacts(app: Application, search: String, result: (List<String>) -> Unit) {
        ContentResolverCompat.query(
                app.contentResolver,
                ContactsContract.Data.CONTENT_URI,
                arrayOf(
                        ContactsContract.Data.DISPLAY_NAME
                ),
                "${ContactsContract.Data.DISPLAY_NAME} LIKE ?",
                arrayOf(search),
                null,
                null
        ).use {
            val names = ArrayList<String>(it.count)
            while (it.moveToNext()) {
                names.add(it.getString(it.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)))
            }
            result(names)
        }
    }

}