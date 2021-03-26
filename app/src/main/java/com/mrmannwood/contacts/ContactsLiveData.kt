package com.mrmannwood.contacts

import android.app.Application
import android.provider.ContactsContract
import androidx.annotation.MainThread
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.lifecycle.LiveData
import com.mrmannwood.executor.AppExecutors

class ContactsLiveData(application: Application) : LiveData<List<ContactData>>() {

    private val contentResolver = application.contentResolver
    private var isActive: Boolean = false
    private var isDirty: Boolean = false
    private var searchTerm: String? = null
    private var cancellationSignal: CancellationSignal? = null

    @MainThread fun setSearchTerm(searchTerm: String) {
        this.searchTerm = searchTerm
        this.isDirty = true
        if (isActive) {
            query(searchTerm)
        }
    }

    @MainThread override fun onActive() {
        super.onActive()
        isActive = true
        if (isDirty) {
            searchTerm?.let {
                query(it)
            }
        }
    }

    @MainThread override fun onInactive() {
        isActive = false
        super.onInactive()
    }

    @MainThread private fun query(searchTerm: String) {
        isDirty = false
        cancellationSignal?.cancel()
        val cancellation = CancellationSignal()
        cancellationSignal = cancellation

        AppExecutors.backgroundExecutor.execute {
            ContentResolverCompat.query(
                    contentResolver,
                    ContactsContract.Data.CONTENT_URI,
                    arrayOf(
                            ContactsContract.Data.CONTACT_ID,
                            ContactsContract.Data.DISPLAY_NAME_PRIMARY
                    ),
                    "${ContactsContract.Data.DISPLAY_NAME_PRIMARY} LIKE ?",
                    arrayOf("%$searchTerm%"),
                    null,
                    cancellationSignal
            ).use {
                val contacts = ArrayList<ContactData>(it.count)
                while (it.moveToNext() && !cancellation.isCanceled) {
                    contacts.add(
                            ContactData(
                                    id = it.getInt(it.getColumnIndex(ContactsContract.Data.CONTACT_ID)),
                                    name = it.getString(it.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                            )
                    )
                }

                if (!cancellation.isCanceled) {
                    postValue(contacts)
                }
            }
        }
    }
}