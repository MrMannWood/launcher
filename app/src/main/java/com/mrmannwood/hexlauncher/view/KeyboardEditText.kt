package com.mrmannwood.hexlauncher.view

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import androidx.annotation.UiThread
import androidx.appcompat.R
import androidx.appcompat.widget.AppCompatEditText
import com.mrmannwood.hexlauncher.HandleBackPressed

@UiThread
class KeyboardEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = R.attr.editTextStyle
) : AppCompatEditText(context, attrs, defStyleAttr) {

    var handleBackPressed: HandleBackPressed? = null

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        if (event.keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
            handleBackPressed?.handleBackPressed()
        }
        return super.onKeyPreIme(keyCode, event)
    }
}
