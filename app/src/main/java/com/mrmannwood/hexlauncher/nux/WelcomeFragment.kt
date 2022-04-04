package com.mrmannwood.hexlauncher.nux

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.mrmannwood.launcher.R

class WelcomeFragment : Fragment(R.layout.fragment_nux_welcome) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setOnClickListener {
            (parentFragment as NUXHostFragment).next()
        }
    }

}
