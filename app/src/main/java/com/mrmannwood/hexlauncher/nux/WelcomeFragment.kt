package com.mrmannwood.hexlauncher.nux

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
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

        val nextButton = view.findViewById<View>(R.id.button_next)
        val animatorSet = AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(nextButton, "scaleX", 0.5f).apply {
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.REVERSE
                },
                ObjectAnimator.ofFloat(nextButton, "scaleY", 0.5f).apply {
                    repeatCount = ObjectAnimator.INFINITE
                    repeatMode = ObjectAnimator.REVERSE
                })
            duration = 1200

        }
        animatorSet.start()
    }

}
