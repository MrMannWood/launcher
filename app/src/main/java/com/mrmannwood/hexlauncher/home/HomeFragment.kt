package com.mrmannwood.hexlauncher.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.gesture.LauncherGestureDetectorListener
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding
import timber.log.Timber

class HomeFragment : WidgetHostFragment(), HandleBackPressed {

    private val viewModel: HomeViewModel by activityViewModels()

    private var swipeRightPackage : String? = null
    private var swipeLeftPackage : String? = null

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.HomeDescription(isLoading = isLoading)
    }

    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {

        databinder.root.setOnTouchListener(object : View.OnTouchListener {

            val gestureDetector = createGestureDetector(requireContext())

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                return gestureDetector.onTouchEvent(me)
            }
        })
        databinder.root.setOnCreateContextMenuListener { menu, _, _ ->
            menu.add(R.string.menu_item_home_settings).setOnMenuItemClickListener {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                true
            }
        }

        viewModel.appListLiveData.observe(viewLifecycleOwner) { _ -> onLoadingComplete() }
        viewModel.swipeRightLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeRightPackage = packageName
        }
        viewModel.swipeLeftLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeLeftPackage = packageName
        }
    }

    override fun handleBackPressed(): Boolean = true /* consume, this is a launcher*/

    private fun createGestureDetector(context: Context) = GestureDetectorCompat(
        context,
        LauncherGestureDetectorListener(object : LauncherGestureDetectorListener.GestureListener {
            override fun onSwipeUp() {
                if (!isLoading()) {
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