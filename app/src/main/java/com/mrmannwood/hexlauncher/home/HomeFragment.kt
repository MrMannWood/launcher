package com.mrmannwood.hexlauncher.home

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListActivity
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.decorateForAppListLaunch
import com.mrmannwood.hexlauncher.applist.AppListActivity.Companion.onAppListResult
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.executors.InlineExecutor
import com.mrmannwood.hexlauncher.executors.OriginalThreadCallback
import com.mrmannwood.hexlauncher.executors.PackageManagerExecutor
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.HexItem
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.hexlauncher.launcher.Provider
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding
import timber.log.Timber
import java.lang.Math.*
import kotlin.math.pow

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

    private val preferenceSwipeNorthWestResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeNorthWest.PACKAGE_NAME, result)
    }
    private val preferenceSwipeNorthEastResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeNorthEast.PACKAGE_NAME, result)
    }
    private val preferenceSwipeWestResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeWest.PACKAGE_NAME, result)
    }
    private val preferenceSwipeEastResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeEast.PACKAGE_NAME, result)
    }
    private val preferenceSwipeSouthWestResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeSouthWest.PACKAGE_NAME, result)
    }
    private val preferenceSwipeSouthResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeSouth.PACKAGE_NAME, result)
    }
    private val preferenceSwipeSouthEastResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeSouthEast.PACKAGE_NAME, result)
    }

    private fun onGestureActionUpdated(preferenceKey: String, result: ActivityResult) {
        result.data.onAppListResult(
            onSuccess = { _, packageName ->
                PreferencesRepository.getPrefs(requireContext(), OriginalThreadCallback.create {
                    it.edit {
                        putString(preferenceKey, packageName)
                    }
                })
            },
            onFailure = {
                Toast.makeText(requireContext(), R.string.no_app_selected, Toast.LENGTH_LONG).show()
            }
        )
    }

    private val viewModel: HomeViewModel by activityViewModels()

    override val nameForInstrumentation = "HomeFragment"

    private lateinit var gestureViewNorthWest: View
    private lateinit var gestureViewNorth: View
    private lateinit var gestureViewNorthEast: View
    private lateinit var gestureViewWest: View
    private lateinit var gestureViewEast: View
    private lateinit var gestureViewSouthWest: View
    private lateinit var gestureViewSouth: View
    private lateinit var gestureViewSouthEast: View

    private var showContextMenuRunnable: Runnable? = null

    private var appList: List<AppInfo>? = null
    private val gesturePackages = mutableListOf<String?>()
    private var gestureViewHalfWidth: Int = 0
    private var gestureViewHalfHeight: Int = 0
    private var gestureLocations: List<Pair<View, PointF>>? = null

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.HomeDescription(isLoading = isLoading)
    }

    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {
        gestureViewNorthWest = databinder.northWest.root
        gestureViewNorth = databinder.north.root
        gestureViewNorthEast = databinder.northEast.root
        gestureViewWest = databinder.west.root
        gestureViewEast = databinder.east.root
        gestureViewSouthWest = databinder.southWest.root
        gestureViewSouth = databinder.south.root
        gestureViewSouthEast = databinder.southEast.root

        databinder.appInfoAdapter = LauncherFragmentDatabindingAdapter

        measureGestureView(databinder)

        databinder.root.setOnTouchListener(makeOnTouchListener(databinder))

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

        databinder.hexItemNorth = makeHexItem(R.string.gesture_search_apps, R.drawable.ic_apps)
        databinder.north.root.setTag(R.id.gesture_icon_action, object : Runnable {
            override fun run() {
                showLauncherFragment()
            }
        })

        databinder.hexItemNorthWest = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemNorthEast = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemWest = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemEast = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemSouthWest = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemSouth = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        databinder.hexItemSouthEast = makeHexItem(R.string.gesture_set_quick_access_item, R.drawable.ic_add)
        setCreateGestureAction(databinder.northWest.root, preferenceSwipeNorthWestResultContract)
        setCreateGestureAction(databinder.northEast.root, preferenceSwipeNorthEastResultContract)
        setCreateGestureAction(databinder.west.root, preferenceSwipeWestResultContract)
        setCreateGestureAction(databinder.east.root, preferenceSwipeEastResultContract)
        setCreateGestureAction(databinder.southWest.root, preferenceSwipeSouthWestResultContract)
        setCreateGestureAction(databinder.south.root, preferenceSwipeSouthResultContract)
        setCreateGestureAction(databinder.southEast.root, preferenceSwipeSouthEastResultContract)

        listOf(
            viewModel.swipeNorthWestLiveData to { info: AppInfo ->
                databinder.hexItemNorthWest = info
                setOpenAppAction(databinder.northWest.root, info)
            },
            viewModel.swipeNorthEastLiveData to { info: AppInfo ->
                databinder.hexItemNorthEast = info
                setOpenAppAction(databinder.northEast.root, info)
            },
            viewModel.swipeWestLiveData to { info: AppInfo ->
                databinder.hexItemWest = info
                setOpenAppAction(databinder.west.root, info)
            },
            viewModel.swipeEastLiveData to { info: AppInfo ->
                databinder.hexItemEast = info
                setOpenAppAction(databinder.east.root, info)
            },
            viewModel.swipeSouthWestLiveData to { info: AppInfo ->
                databinder.hexItemSouthWest = info
                setOpenAppAction(databinder.southWest.root, info)
            },
            viewModel.swipeSouthLiveData to { info: AppInfo ->
                databinder.hexItemSouth = info
                setOpenAppAction(databinder.south.root, info)
            },
            viewModel.swipeSouthEastLiveData to { info: AppInfo ->
                databinder.hexItemSouthEast = info
                setOpenAppAction(databinder.southEast.root, info)
            },
        ).forEachIndexed { idx, (liveData, setFunction) -> liveData.observe(viewLifecycleOwner) { packageName ->
            if (packageName == null) return@observe
            ensureAppInstalled(packageName) {
                appList?.firstOrNull { info -> info.packageName == packageName }?.let { appInfo ->
                    setFunction(appInfo)
                }
                gesturePackages.add(idx, packageName)
            }
        } }

        viewModel.appListLiveData.observe(viewLifecycleOwner) {
            appList = it
            gesturePackages.forEachIndexed { idx, packageName ->
                if (packageName != null) {
                    appList?.firstOrNull { info -> info.packageName == packageName }?.let { appInfo ->
                        when(idx) {
                            0 -> {
                                databinder.hexItemNorthWest = appInfo
                                setOpenAppAction(databinder.northWest.root, appInfo)
                            }
                            1 -> {
                                databinder.hexItemNorthEast = appInfo
                                setOpenAppAction(databinder.northEast.root, appInfo)
                            }
                            2 -> {
                                databinder.hexItemWest = appInfo
                                setOpenAppAction(databinder.west.root, appInfo)
                            }
                            3 -> {
                                databinder.hexItemEast = appInfo
                                setOpenAppAction(databinder.east.root, appInfo)
                            }
                            4 -> {
                                databinder.hexItemSouthWest = appInfo
                                setOpenAppAction(databinder.southWest.root, appInfo)
                            }
                            5 -> {
                                databinder.hexItemSouth = appInfo
                                setOpenAppAction(databinder.south.root, appInfo)
                            }
                            6 -> {
                                databinder.hexItemSouthEast = appInfo
                                setOpenAppAction(databinder.southEast.root, appInfo)
                            }
                            else -> throw IllegalArgumentException("Illegal index while reading gestures $idx")
                        }
                    }
                }
            }
            onLoadingComplete()
        }
    }

    override fun handleBackPressed(): Boolean = true /* consume, this is a launcher*/

    private fun makeHexItem(
        @StringRes label: Int,
        @DrawableRes icon: Int,
    ): HexItem = object: HexItem {
        override val label = resources.getString(label)
        override val icon: Provider<Drawable> = Provider(
            { ContextCompat.getDrawable(requireContext(), icon)!! }, InlineExecutor)
        override val hidden: Boolean = false
        override val backgroundColor: Int = Color.TRANSPARENT
        override val backgroundHidden: Boolean = true
    }

    private fun setCreateGestureAction(v: View, activityResultLauncher: ActivityResultLauncher<Intent>) {
        v.setTag(R.id.gesture_icon_action, object : Runnable {
            override fun run() {
                activityResultLauncher.launch(
                    Intent(activity, AppListActivity::class.java)
                        .decorateForAppListLaunch(R.string.gesture_app_chooser_title))
            }
        })
    }

    private fun setOpenAppAction(v: View, appInfo: AppInfo) {
        v.setTag(R.id.gesture_icon_action, makeOpenAppRunnable(appInfo))
    }

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    fun showLauncherFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.container, AppListFragment())
            .addToBackStack("AppListFragment")
            .commit()
    }

    private fun measureGestureView(databinder: FragmentHomeBinding) {
        databinder.root.post(object: Runnable {
            override fun run() {
                if (databinder.gestureContainer.height > 0) {
                    gestureViewHalfWidth = databinder.gestureContainer.width / 2
                    gestureViewHalfHeight = databinder.gestureContainer.height / 2

                    gestureLocations = listOf(
                        gestureViewNorthWest to PointF(gestureViewNorthWest.x, gestureViewNorthWest.y),
                        gestureViewNorth to PointF(gestureViewNorth.x, gestureViewNorth.y),
                        gestureViewNorthEast to PointF(gestureViewNorthEast.x, gestureViewNorthEast.y),
                        gestureViewWest to PointF(gestureViewWest.x, gestureViewWest.y),
                        gestureViewEast to PointF(gestureViewEast.x, gestureViewEast.y),
                        gestureViewSouthWest to PointF(gestureViewSouthWest.x, gestureViewSouthWest.y),
                        gestureViewSouth to PointF(gestureViewSouth.x, gestureViewSouth.y),
                        gestureViewSouthEast to PointF(gestureViewSouthEast.x, gestureViewSouthEast.y),
                    )

                    databinder.gestureContainer.visibility = View.GONE

                } else {
                    databinder.root.post(this)
                }
            }
        })
    }

    private fun makeOnTouchListener(databinder: FragmentHomeBinding): View.OnTouchListener {
        val longPressTime = ViewConfiguration.getLongPressTimeout().toLong()

        return object : View.OnTouchListener {

            private lateinit var downPosition: PointF
            private var currentlyActive: Triple<View, Int, Int>? = null

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                when (me.action) {
                    MotionEvent.ACTION_DOWN -> {
                        downPosition = PointF(me.rawX, me.rawY)
                        databinder.gestureContainer.x = me.x - gestureViewHalfWidth
                        databinder.gestureContainer.y = me.y - gestureViewHalfHeight
                        databinder.gestureContainer.visibility = View.VISIBLE
                        showContextMenuRunnable = makeShowContextMenuRunnable(databinder.gestureContainer, me.x, me.y).also {
                            view?.postDelayed(it, longPressTime)
                        }
                    }
                    MotionEvent.ACTION_UP -> {
                        (currentlyActive?.first?.getTag(R.id.gesture_icon_action) as? Runnable)?.run()
                        stoppedTouchingView()
                        showContextMenuRunnable?.let { view?.removeCallbacks(it) }
                        databinder.gestureContainer.visibility = View.GONE
                    }
                    MotionEvent.ACTION_MOVE -> {
                        currentlyActive?.let { current ->
                            if (!isActive(me, current.first)) {
                                stoppedTouchingView()
                            }
                        }
                        if (currentlyActive == null) {
                            gestureLocations?.filter { isActive(me, it.first) }?.forEach {
                                stoppedTouchingView()
                                currentlyActive = Triple(it.first, it.first.layoutParams.width, it.first.layoutParams.height)
                                it.first.layoutParams.width = (it.first.layoutParams.width * 1.5).toInt()
                                it.first.layoutParams.height = (it.first.layoutParams.height * 1.5).toInt()
                                it.first.requestLayout()
                            }
                        }
                    }
                }
                downPosition.let { dp ->
                    showContextMenuRunnable?.let { r ->
                        if (20 < sqrt((me.rawX - dp.x).toDouble().pow(2) + (me.rawY - dp.y).toDouble().pow(2))) {
                            view?.removeCallbacks(r)
                        }
                    }
                }
                return true
            }

            private fun makeShowContextMenuRunnable(gestureContainer: View, x: Float, y: Float) = Runnable {
                stoppedTouchingView()
                view?.showContextMenu(x, y)
                gestureContainer.visibility = View.GONE
            }

            val xy = intArrayOf(0,0)
            val rect = Rect()
            private fun isActive(me: MotionEvent, v: View): Boolean {
                v.getLocationOnScreen(xy)
                rect.top = xy[1]
                rect.left = xy[0]
                rect.bottom = rect.top + v.height
                rect.right = rect.left + v.width
                return rect.contains(me.rawX.toInt(), me.rawY.toInt()) ||
                        doLinesIntersect(me.rawX, me.rawY, downPosition.x, downPosition.y, rect.left, rect.top, rect.left, rect.top + rect.height()) ||
                        doLinesIntersect(me.rawX, me.rawY, downPosition.x, downPosition.y, rect.left, rect.top, rect.left + rect.width(), rect.top) ||
                        doLinesIntersect(me.rawX, me.rawY, downPosition.x, downPosition.y, rect.left + rect.width(), rect.top, rect.left + rect.width(), rect.top + rect.height()) ||
                        doLinesIntersect(me.rawX, me.rawY, downPosition.x, downPosition.y, rect.left, rect.top + rect.height(), rect.left + rect.width(), rect.top + rect.height())
            }

            private fun doLinesIntersect(
                line1x1: Float,
                line1y1: Float,
                line1x2: Float,
                line1y2: Float,

                line2x1: Int,
                line2y1: Int,
                line2x2: Int,
                line2y2: Int): Boolean {

                val s1_x = line1x2 - line1x1
                val s1_y = line1y2 - line1y1
                val s2_x = line2x2 - line2x1
                val s2_y = line2y2 - line2y1

                val s = (-s1_y * (line1x1 - line2x1) + s1_x * (line1y1 - line2y1)) / (-s2_x * s1_y + s1_x * s2_y)
                val t = ( s2_x * (line1y1 - line2y1) - s2_y * (line1x1 - line2x1)) / (-s2_x * s1_y + s1_x * s2_y)

                return s >= 0 && s <= 1 && t >= 0 && t <= 1
            }

            private fun stoppedTouchingView() {
                currentlyActive?.let { current ->
                    current.first.layoutParams.width = current.second
                    current.first.layoutParams.height = current.third
                    current.first.requestLayout()
                    currentlyActive = null
                }
            }
        }
    }

    private fun ensureAppInstalled(packageName: String, action: () -> Unit) {
        PackageManagerExecutor.execute {
            try {
                context?.packageManager?.getPackageInfo(packageName, 0) ?: return@execute
                action()
            } catch (_ : Exception) { /* package isn't installed */ }
        }
    }

    private fun makeOpenAppRunnable(appInfo: AppInfo): Runnable = Runnable {
        requireActivity().packageManager.getLaunchIntentForPackage(appInfo.packageName)?.let { intent ->
            try {
                startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            } catch (e: Exception) {
                Timber.e(e, "Unable to open package: ${appInfo.packageName}")
            }
        }
    }
}
