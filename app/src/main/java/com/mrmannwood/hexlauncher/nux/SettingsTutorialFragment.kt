package com.mrmannwood.hexlauncher.nux

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.mrmannwood.launcher.R

class SettingsTutorialFragment : Fragment(R.layout.fragment_nux_setings_tutorial) {

    private lateinit var phantomFinger : ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        phantomFinger = view.findViewById(R.id.phantom_finger)

        val animationX = ObjectAnimator.ofFloat(phantomFinger, "scaleX", 0.5f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }
        val animationY = ObjectAnimator.ofFloat(phantomFinger, "scaleY", 0.5f).apply {
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.RESTART
        }

        val animatorSet = AnimatorSet().apply {
            playTogether(animationX, animationY)
            duration = 1200

        }
        animatorSet.start()
    }
}