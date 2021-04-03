package com.mrmannwood.hexlauncher.contacts

import android.Manifest
import android.app.Application
import android.content.ContentUris
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.provider.ContactsContract
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import androidx.core.content.ContentResolverCompat
import androidx.core.os.CancellationSignal
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.executor.AppExecutors
import com.mrmannwood.hexlauncher.permissions.PermissionsLiveData
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import timber.log.Timber
import java.io.ByteArrayInputStream
import java.lang.Exception
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class ContactsLiveData(private val application: Application) : LiveData<Result<List<ContactData>>>() {

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
            postValue(Result.success(listOf()))
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
            val result : Result<List<ContactData>>? = try {
                searchTerm.getAndSet(null)?.let { term ->
                   if (term.isEmpty()) {
                        Result.success(listOf())
                    } else {
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
                            Result.success(contacts)
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Unable to load contacts")
                Result.failure(e)
            }
            if (!cancellation.isCanceled) {
                result?.let {
                    postValue(it)
                }
            }
        }
    }

    @WorkerThread
    fun getImageForContact(contactUri: Uri) : Drawable? =
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
                                application.resources,
                                BitmapFactory.decodeStream(ByteArrayInputStream(it))
                        )
                    }
                } else {
                    null
                }
            }
}