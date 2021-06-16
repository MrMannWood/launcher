package com.mrmannwood.hexlauncher.nux

import android.animation.AnimatorSet
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

        view.setOnTouchListener(object : View.OnTouchListener {

            val gestureDetector = GestureDetectorCompat(
                view.context,
                LauncherGestureDetectorListener(object : LauncherGestureDetectorListener.GestureListener {
                    override fun onSwipeUp() { }

                    override fun onSwipeDown() { }

                    override fun onSwipeRight() { }

                    override fun onSwipeLeft() { }

                    override fun onLongPress(x: Float, y: Float) {
                        requireView().showContextMenu(x, y)
                    }
                })
            )

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(me)
            }
        })

        view.setOnCreateContextMenuListener { menu, v, _ ->
            menu.add(R.string.nux_settings_tutorial_settings).setOnMenuItemClickListener {
                Toast.makeText(v.context, R.string.nux_settings_tutorial_user_clicked_settings, Toast.LENGTH_LONG).show()
                true
            }
        }
    }
}