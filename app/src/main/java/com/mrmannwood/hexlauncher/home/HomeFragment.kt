package com.mrmannwood.hexlauncher.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.gesture.LauncherGestureDetectorListener
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import timber.log.Timber

class HomeFragment : Fragment(R.layout.fragment_home), HandleBackPressed {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var appLoadProgressBar: View
    private lateinit var dateView: View
    private lateinit var timeView : View
    private var swipeRightPackage : String? = null
    private var swipeLeftPackage : String? = null

    private var appsAreLoaded : Boolean = false

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
        appLoadProgressBar = view.findViewById(R.id.app_load_progress_bar_container)
        dateView = view.findViewById(R.id.date)
        timeView = view.findViewById(R.id.time)

        viewModel.slot1LiveData.observe(viewLifecycleOwner) { slot1 ->
//            dateView.visibility = if (showDate == true) View.VISIBLE else View.INVISIBLE
        }
        viewModel.slot2LiveData.observe(viewLifecycleOwner) { slot2 ->
//            timeView.visibility = if (showTime == true) View.VISIBLE else View.INVISIBLE
        }
        viewModel.swipeRightLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeRightPackage = packageName
        }
        viewModel.swipeLeftLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeLeftPackage = packageName
        }
        viewModel.appListLiveData.observe(viewLifecycleOwner) { _ ->
            appLoadProgressBar.visibility = View.GONE
            appsAreLoaded = true
        }
    }

    override fun handleBackPressed(): Boolean = true /* consume, this is a launcher*/

    private fun createGestureDetector(context: Context) = GestureDetectorCompat(
        context,
        LauncherGestureDetectorListener(object : LauncherGestureDetectorListener.GestureListener {
            override fun onSwipeUp() {
                if (appsAreLoaded) {
                    showLauncherFragment()
                } else {
                    Toast.makeText(requireContext(), R.string.home_apps_still_loading, Toast.LENGTH_SHORT).show()
                }
            }

            override fun onSwipeDown() { }

            override fun onSwipeRight() {
                tryLaunchPackage(swipeRightPackage)
            }

            override fun onSwipeLeft() {
                tryLaunchPackage(swipeLeftPackage)
            }

            override fun onLongPress(x: Float, y: Float) {
                requireView().showContextMenu(x, y)
            }
        })
    )

    private fun showLauncherFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AppListFragment())
            .addToBackStack("AppListFragment")
            .commit()
    }

    private fun tryLaunchPackage(packageName: String?) {
        packageName?.let { packageName ->
            requireActivity().packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                try {
                    startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } catch (e: Exception) {
                    Timber.e(e, "Unable to open package: $packageName")
                    Toast.makeText(requireContext(), R.string.unable_to_start_app, Toast.LENGTH_SHORT).show()
                }
            } ?: {
                Toast.makeText(requireContext(), R.string.unable_to_start_app, Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(requireContext(), R.string.gesture_no_action_selected, Toast.LENGTH_SHORT).show()
        }
    }
}