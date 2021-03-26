package com.mrmannwood.wallpaper

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.mrmannwood.launcher.R
import java.lang.ref.WeakReference

class WallpaperActivity : AppCompatActivity() {

    companion object {
        private const val IMAGE_REQUEST = 1
    }

    private val progressBar : ProgressBar by lazy { findViewById<ProgressBar>(R.id.progress_bar) }
    private val button : Button by lazy { findViewById<Button>(R.id.button) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wallpaper)

        button.setOnClickListener { view ->
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK || data == null) {
            Toast.makeText(this, "No Wallpaper Selected", Toast.LENGTH_LONG).show()
        } else {
            progressBar.visibility = View.VISIBLE
            button.visibility = View.GONE
            FileUriWorker(WeakReference(this)).execute(data.data)
        }
    }

    class FileUriWorker(private val activityRef: WeakReference<WallpaperActivity>) : AsyncTask<Uri, String?, Bitmap?>() {

        override fun doInBackground(vararg uri: Uri): Bitmap? {
            return try {
                activityRef.get()?.contentResolver?.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Images.Media.DATA),
                    MediaStore.Images.Media._ID + "=?",
                    arrayOf(DocumentsContract.getDocumentId(uri[0]).split(":")[1]),
                    null
                )?.use { cursor ->
                    if (!cursor.moveToFirst()) {
                        null
                    } else {
                        BitmapFactory.decodeFile(
                            cursor.getString(
                                cursor.getColumnIndex(MediaStore.Images.Media.DATA)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }

        override fun onPostExecute(result: Bitmap?) {
            super.onPostExecute(result)
            if (result == null) {
                return
            }
            activityRef.get()?.let { activity ->
                WallpaperManager.getInstance(activity).setBitmap(result)
                activity.button.visibility = View.VISIBLE
                activity.progressBar.visibility = View.GONE
                Toast.makeText(activity, "Wallpaper has been set", Toast.LENGTH_LONG).show()
            }
        }
    }
}