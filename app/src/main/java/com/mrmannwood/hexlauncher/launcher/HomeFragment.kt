package com.mrmannwood.hexlauncher.launcher

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import kotlin.math.abs

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setOnTouchListener(object : View.OnTouchListener {

            val gestureDetector = createGestureDetector(view.context)

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(me)
            }
        })
        view.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add(R.string.menu_item_home_settings).setOnMenuItemClickListener {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                true
            }
        }
    }

    private fun createGestureDetector(context: Context) = GestureDetectorCompat(
        context,
        object : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                return true;
            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                if (abs(velocityX) > abs(velocityY)) {
                    // horizontal
                    if (velocityX < 0) {
                        onSwipeLeft()
                    } else {
                        onSwipeRight()
                    }
                    return true
                } else if (abs(velocityY) > abs(velocityX)) {
                    //vertical
                    if (velocityY < 0) {
                        onSwipeUp()
                    } else {
                        onSwipeDown()
                    }
                    return true
                }
                return super.onFling(e1, e2, velocityX, velocityY)
            }

            fun onSwipeUp() {
                showLauncherFragment()
            }

            fun onSwipeDown() {
                Toast.makeText(requireContext(), "Down", Toast.LENGTH_SHORT).show()
            }

            fun onSwipeLeft() {
                Toast.makeText(requireContext(), "Left", Toast.LENGTH_SHORT).show()
            }

            fun onSwipeRight() {
                Toast.makeText(requireContext(), "Right", Toast.LENGTH_SHORT).show()
            }

            override fun onLongPress(e: MotionEvent) {
                super.onLongPress(e)
                requireView().showContextMenu(e.x, e.y)
            }
        })

    private fun showLauncherFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AppListFragment())
            .addToBackStack("AppListFragment")
            .commit()
    }

    private fun launchAssistant() {
        startActivity(Intent(Intent.ACTION_VOICE_COMMAND).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }

    private fun launchPhone() {
        startActivity(Intent(Intent.ACTION_DIAL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}