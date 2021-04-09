package com.mrmannwood.hexlauncher.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.view.GestureDetectorCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.gesture.LauncherGestureDetectorListener
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeArrangementBinding
import timber.log.Timber

class HomeFragment : Fragment(R.layout.fragment_home), HandleBackPressed {

    private val viewModel: HomeViewModel by activityViewModels()

    private lateinit var databinder : FragmentHomeArrangementBinding
    private lateinit var slots : List<FrameLayout>
    private var swipeRightPackage : String? = null
    private var swipeLeftPackage : String? = null

    private var dateWidget : View? = null
    private var timeWidget : View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        databinder = DataBindingUtil.inflate(inflater, R.layout.fragment_home_arrangement, container, false)
        databinder.adapter = HomeViewDatabindingAdapter(requireActivity().application)
        databinder.description = HomeViewDescription.HomeDescription(isLoading = true)
        return databinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        slots = listOf(
                databinder.slot0,
                databinder.slot1,
                databinder.slot2,
                databinder.slot3,
                databinder.slot4,
                databinder.slot5,
                databinder.slot6,
                databinder.slot7,
        )

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

        viewModel.appListLiveData.observe(viewLifecycleOwner) { _ ->
            databinder.description = HomeViewDescription.HomeDescription(isLoading = false)
        }
        viewModel.timeWidgetLiveData.observe(
                viewLifecycleOwner,
                WidgetLiveDataObserver(R.layout.widget_time)
        )
        viewModel.dateWidgetLiveData.observe(
                viewLifecycleOwner,
                WidgetLiveDataObserver(R.layout.widget_date)
        )
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
                if (!databinder.description!!.isLoading()) {
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

    private inner class WidgetLiveDataObserver(
            @LayoutRes private  val widgetLayout: Int
    ) : Observer<Int?> {

        private var widgetView : View? = null

        override fun onChanged(slot: Int?) {
            slot?.let {
                showInSlot(slot)
            } ?: run {
                hide()
            }
        }

        private fun showInSlot(slot: Int) {
            var view = widgetView
            if (view == null){
                view = LayoutInflater.from(requireContext()).inflate(widgetLayout, slots[slot], false)
                widgetView = view
            }
            slots[slot].addView(view)
        }

        private fun hide() {
            (widgetView?.parent as? ViewGroup)?.removeView(widgetView)
        }
    }
}