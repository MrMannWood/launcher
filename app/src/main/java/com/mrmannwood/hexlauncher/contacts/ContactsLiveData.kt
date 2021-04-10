package com.mrmannwood.hexlauncher.contacts

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.MainThread
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.core.os.OperationCanceledException
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.coroutine.LiveDataWithCoroutineScope
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.util.concurrent.atomic.AtomicReference

class ContactsLiveData(private val context: Context) : LiveDataWithCoroutineScope<List<ContactData>>() {

    private val contentResolver = context.contentResolver
    private val permissionsLiveData = PermissionsLiveData(context, PreferenceKeys.Contacts.ALLOW_CONTACT_SEARCH, Manifest.permission.READ_CONTACTS)
    private var cancellationSignal: CancellationSignal? = null
    private val searchTerm = AtomicReference<String>()
    private val query = AtomicReference<() -> Unit>()

    private val permissionObserver = Observer<PermissionsLiveData.PermissionsResult> {
        if (it == PermissionsLiveData.PermissionsResult.PrefGrantedPermissionGranted) {
            query.set {
                scope?.launch {
                    searchTerm.getAndSet(null)?.let { term ->
                        queryContacts(term)?.let { contacts ->
                            postValue(contacts)
                        }
                    }
                }
            }
            query.get()?.invoke()
        } else {
            postValue(listOf())
        }
    }

    @MainThread
    fun setSearchTerm(term: String) {
        searchTerm.set(term)
        query.get()?.invoke()
    }

    override fun onActive() {
        super.onActive()
        permissionsLiveData.observeForever(permissionObserver)
    }

    override fun onInactive() {
        super.onInactive()
        permissionsLiveData.removeObserver(permissionObserver)
        cancellationSignal?.cancel()
        query.set(null)
    }

    private suspend fun queryContacts(term: String) : List<ContactData>? {
        return withContext(Dispatchers.Main) {
            cancellationSignal?.cancel()
            val cancellation = CancellationSignal()
            cancellationSignal = cancellation

            queryContacts(cancellation, term)
        }
    }

    private suspend fun queryContacts(cancellationSignal: CancellationSignal, term: String) : List<ContactData>? {
        return withContext(Dispatchers.IO) {
            if (term.isEmpty()) {
                listOf()
            } else {
                try {
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
                        while (it.moveToNext() && !cancellationSignal.isCanceled) {
                            val contactId = it.getLong(it.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                            val contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, contactId)
                            contacts.add(
                                    ContactData(
                                            id = contactId,
                                            name = it.getString(it.getColumnIndex(ContactsContract.Data.DISPLAY_NAME)),
                                            uri = contactUri,
                                            image = getImageForContact(contactUri),
                                    )
                            )
                        }
                        contacts
                    }
                } catch (e: OperationCanceledException) {
                    null
                }
            }
        }
    }

    private suspend fun getImageForContact(contactUri: Uri) : Drawable? =
            withContext(Dispatchers.IO) {
                contentResolver.query(
                        Uri.withAppendedPath(contactUri, ContactsContract.Contacts.Photo.CONTENT_DIRECTORY),
                        arrayOf(ContactsContract.Contacts.Photo.PHOTO),
                        null,
                        null,
                        null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getBlob(0)?.let {
                            BitmapDrawable(
                                    context.resources,
                                    BitmapFactory.decodeStream(ByteArrayInputStream(it))
                            )
                        }
                    } else {
                        null
                    }
                }
            }
}