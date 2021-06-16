package com.mrmannwood.hexlauncher.nux

import android.animation.Animator
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.mrmannwood.hexlauncher.gesture.LauncherGestureDetectorListener
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

        view.setOnTouchListener(object : View.OnTouchListener {

            val gestureDetector = GestureDetectorCompat(
                view.context,
                LauncherGestureDetectorListener(object : LauncherGestureDetectorListener.GestureListener {
                    override fun onSwipeUp() {
                        Toast.makeText(view.context, R.string.nux_swipe_tutorial_user_swiped_up, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSwipeDown() { }

                    override fun onSwipeRight() { }

                    override fun onSwipeLeft() { }

                    override fun onLongPress(x: Float, y: Float) { }
                })
            )

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(me)
            }
        })
    }
}