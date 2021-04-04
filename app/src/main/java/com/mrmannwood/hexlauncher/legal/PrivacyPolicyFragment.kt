package com.mrmannwood.hexlauncher.legal

import android.os.Bundle
import android.text.Html
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.mrmannwood.launcher.R

class PrivacyPolicyFragment : Fragment(R.layout.fragment_privacy_policy) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<TextView>(R.id.privacy_policy).apply {
            text = Html.fromHtml(getString(R.string.privacy_policy), Html.FROM_HTML_MODE_LEGACY)
        }
    }
}