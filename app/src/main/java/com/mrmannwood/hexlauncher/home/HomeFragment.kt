package com.mrmannwood.hexlauncher.home

import android.app.Activity
import android.app.WallpaperManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.VisibleForTesting
import androidx.core.content.edit
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.activityViewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.gesture.LauncherGestureDetectorListener
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding
import timber.log.Timber

class HomeFragment : WidgetHostFragment(), HandleBackPressed {

    private val wallpaperPickerContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        if (result.data?.data == null) return@registerForActivityResult
        try {
            startActivity(WallpaperManager.getInstance(requireContext())
                .getCropAndSetWallpaperIntent(result.data!!.data!!).apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            )
        } catch (e: IllegalArgumentException) {
            Toast.makeText(requireContext(), R.string.error_cannot_change_wallpaper, Toast.LENGTH_LONG).show()
        }
    }

    private val viewModel: HomeViewModel by activityViewModels()

    private var swipeRightGesture : Gesture? = null
    private var swipeLeftGesture : Gesture? = null

    override val nameForInstrumentation = "HomeFragment"

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
            menu.add(R.string.menu_item_home_choose_wallpaper).setOnMenuItemClickListener {
                wallpaperPickerContract.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
                true
            }
            menu.add(R.string.menu_item_home_manage_widgets).setOnMenuItemClickListener {
                startActivity(Intent(activity, HomeArrangementActivity::class.java))
                true
            }
            menu.add(R.string.menu_item_home_settings).setOnMenuItemClickListener {
                startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                true
            }
        }

        viewModel.appListLiveData.observe(viewLifecycleOwner) { onLoadingComplete() }
        viewModel.swipeRightLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeRightGesture = Gesture(Gesture.Type.SWIPE_RIGHT, PreferenceKeys.Gestures.SwipeRight.PACKAGE_NAME, packageName)
        }
        viewModel.swipeLeftLiveData.observe(viewLifecycleOwner) { packageName ->
            swipeLeftGesture = Gesture(Gesture.Type.SWIPE_LEFT, PreferenceKeys.Gestures.SwipeLeft.PACKAGE_NAME, packageName)
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
                onGestureAttempted(swipeRightGesture)
            }

            override fun onSwipeLeft() {
                onGestureAttempted(swipeLeftGesture)
            }

            override fun onLongPress(x: Float, y: Float) {
                requireView().showContextMenu(x, y)
            }
        })
    )

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun showLauncherFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AppListFragment())
            .addToBackStack("AppListFragment")
            .commit()
    }

    private fun onGestureAttempted(gesture: Gesture?) {
        if (gesture == null || gesture.isUnwanted()) return

        var startedPackage = false
        gesture.packageName?.let { packageName ->
            requireActivity().packageManager.getLaunchIntentForPackage(packageName)?.let { intent ->
                try {
                    startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    startedPackage = true
                } catch (e: Exception) {
                    Timber.e(e, "Unable to open package: $packageName")
                }
            }
        }
        if (!startedPackage) {
            MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.gesture_not_bound_title)
                .setMessage(R.string.gesture_not_bound_message)
                .setPositiveButton(R.string.gesture_not_bound_button_positive) { _, _ ->
                    startActivity(Intent(requireActivity(), SettingsActivity::class.java))
                }
                .setNegativeButton(R.string.gesture_not_bound_button_negative) { _, _ ->
                    PreferencesRepository.getPrefs(requireContext()) { prefs -> prefs.edit {
                        putString(gesture.prefKey, PreferenceKeys.Gestures.GESTURE_UNWANTED)
                    } }
                }
                .show()
        }
    }

    private data class Gesture(val type: Type, val prefKey: String, val packageName: String?) {
        enum class Type {
            SWIPE_LEFT,
            SWIPE_RIGHT
        }

        fun isUnwanted() = packageName == PreferenceKeys.Gestures.GESTURE_UNWANTED
    }
}
