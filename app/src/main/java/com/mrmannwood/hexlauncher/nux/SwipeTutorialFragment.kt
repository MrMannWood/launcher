package com.mrmannwood.hexlauncher.nux

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.mrmannwood.launcher.R

class SwipeTutorialFragment : Fragment(R.layout.fragment_nux_swipe_tutorial) {

    private lateinit var phantomFinger : ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        phantomFinger = view.findViewById(R.id.phantom_finger)
        ObjectAnimator.ofFloat(phantomFinger, "translationY", -300f).apply {
            duration = 1000
            val startValue = phantomFinger.y
            addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator?) { }

                override fun onAnimationEnd(animation: Animator) {
                    phantomFinger.y = startValue
                    animation.start()
                }

                override fun onAnimationCancel(animation: Animator?) { }

                override fun onAnimationRepeat(animation: Animator?) { }
            })
            start()
        }
    }
}