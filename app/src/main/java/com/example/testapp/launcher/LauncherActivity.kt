package com.example.testapp.launcher

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.testapp.HandleBackPressed
import com.example.testapp.R
import com.example.testapp.contacts.ContactsLoader

class LauncherActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_CONTACTS = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launcher)
        supportActionBar?.hide()

        if (supportFragmentManager.findFragmentById(R.id.container) == null) {
            supportFragmentManager.beginTransaction()
                .add(R.id.container, HomeFragment())
                .commit()
        }

        // makeStatusBarTranslucent()

        if(ContactsLoader.tryCreate(application) == null) {
            ContactsLoader.askForPermission(this, REQUEST_CODE_CONTACTS)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO
                } else {
                    //TODO
                }
            }
        }
    }

    override fun onBackPressed() {
        val handled = supportFragmentManager.findFragmentById(R.id.container)?.let { fragment ->
            if (fragment is HandleBackPressed) {
                fragment.handleBackPressed()
            } else {
                false
            }
        } ?: false
        if (!handled) {
            super.onBackPressed()
        }
    }

    private fun makeStatusBarTranslucent() {
        window.let { window ->
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.statusBarColor = ContextCompat.getColor(this, R.color.black_translucent)
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
    }
}