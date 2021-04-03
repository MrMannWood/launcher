package com.mrmannwood.hexlauncher.contacts

import android.Manifest
import android.app.Application
import android.provider.ContactsContract
import androidx.annotation.MainThread
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.executor.AppExecutors
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ContactsLiveData(application: Application) : LiveData<List<ContactData>>() {

    private val contentResolver = application.contentResolver
    private val permissionsLiveData = PermissionsLiveData(application, PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, Manifest.permission.READ_CONTACTS)
    private val isActive = AtomicBoolean(false)
    private val searchTerm = AtomicReference<String?>()
    private val query = AtomicReference<() -> Unit> { }
    private var cancellationSignal: CancellationSignal? = null

    private val permissionObserver = Observer<PermissionsLiveData.PermissionsResult> {
        if (it == PermissionsLiveData.PermissionsResult.PrefGrantedPermissionGranted) {
            query.set { performQuery() }
            query.get().invoke()
        } else {
            postValue(listOf())
        }
    }

    @MainThread fun setSearchTerm(term: String) {
        searchTerm.set(term)
        query.get().invoke()
    }

    @MainThread override fun onActive() {
        super.onActive()
        isActive.set(true)
        permissionsLiveData.observeForever(permissionObserver)
    }

    @MainThread override fun onInactive() {
        isActive.set(false)
        super.onInactive()
        cancellationSignal?.cancel()
        cancellationSignal = null
        permissionsLiveData.removeObserver(permissionObserver)
    }

    @MainThread private fun performQuery() {
        if (!isActive.get()) {
            return
        }

        cancellationSignal?.cancel()
        val cancellation = CancellationSignal()
        cancellationSignal = cancellation

        AppExecutors.backgroundExecutor.execute {
            val term = searchTerm.getAndSet(null)
            if (term != null) {
                ContentResolverCompat.query(
                        contentResolver,
                        ContactsContract.Data.CONTENT_URI,
                        arrayOf(
                                ContactsContract.Data.CONTACT_ID,
                                ContactsContract.Data.DISPLAY_NAME_PRIMARY
                        ),
                        "${ContactsContract.Data.DISPLAY_NAME_PRIMARY} LIKE ?",
                        arrayOf("%${term}%"),
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
}