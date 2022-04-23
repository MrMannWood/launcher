package com.mrmannwood.hexlauncher.home

import android.app.Activity
import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.PointF
import android.graphics.Rect
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.widget.ImageView
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
import androidx.lifecycle.LiveData
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
import com.mrmannwood.hexlauncher.measureScreen
import com.mrmannwood.hexlauncher.notifications.NotificationShadeUtil
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.settings.SettingsActivity
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHomeBinding
import timber.log.Timber
import java.lang.Math.sqrt
import kotlin.math.pow

class HomeFragment : WidgetHostFragment(), HandleBackPressed {

    private val wallpaperPickerContract = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val context = context ?: return@registerForActivityResult
        if (result.resultCode != Activity.RESULT_OK) return@registerForActivityResult
        if (result.data?.data == null) return@registerForActivityResult
        try {
            startActivity(
                WallpaperManager.getInstance(context)
                    .getCropAndSetWallpaperIntent(result.data!!.data!!).apply {
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
            )
        } catch (e: IllegalArgumentException) {
            Toast.makeText(context, R.string.error_cannot_change_wallpaper, Toast.LENGTH_LONG).show()
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
    private val preferenceSwipeSouthEastResultContract = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        onGestureActionUpdated(PreferenceKeys.Gestures.SwipeSouthEast.PACKAGE_NAME, result)
    }

    private var edgeExclusionZone: Int = 0
    private var screenWidth: Int = 0
    private lateinit var gestures: List<GestureConfiguration>

    private fun onGestureActionUpdated(preferenceKey: String, result: ActivityResult) {
        result.data.onAppListResult(
            onSuccess = { _, componentName ->
                val context = context ?: return@onAppListResult
                PreferencesRepository.getPrefs(
                    context,
                    OriginalThreadCallback.create {
                        it.edit {
                            putString(preferenceKey, componentName.flattenToString())
                        }
                    }
                )
            },
            onFailure = {
                val context = context ?: return@onAppListResult
                Toast.makeText(context, R.string.no_app_selected, Toast.LENGTH_LONG).show()
            }
        )
    }

    private val viewModel: HomeViewModel by activityViewModels()

    override val nameForInstrumentation = "HomeFragment"

    private var appList: List<AppInfo>? = null
    private var gestureViewHalfWidth: Int = 0
    private var gestureViewHalfHeight: Int = 0
    private var generatingGestureContextMenu: Boolean = false

    override fun makeDescription(isLoading: Boolean): HomeViewDescription {
        return HomeViewDescription.HomeDescription(isLoading = isLoading)
    }

    override fun onViewCreated(databinder: FragmentHomeBinding, savedInstanceState: Bundle?) {
        val context = databinder.root.context
        edgeExclusionZone = resources.getDimension(R.dimen.edge_exclusion_zone).toInt()
        screenWidth = measureScreen(requireActivity())
        gestures = listOf(
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeNorthWest.PACKAGE_NAME,
                databinder.northWest.root,
                viewModel.swipeNorthWestLiveData,
                DefaultAppType.PHONE,
                { databinder.hexItemNorthWest = it },
                preferenceSwipeNorthWestResultContract
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeNorth.PACKAGE_NAME,
                databinder.north.root,
                null,
                null,
                { databinder.hexItemNorth = it },
                null
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeNorthEast.PACKAGE_NAME,
                databinder.northEast.root,
                viewModel.swipeNorthEastLiveData,
                DefaultAppType.CAMERA,
                { databinder.hexItemNorthEast = it },
                preferenceSwipeNorthEastResultContract
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeWest.PACKAGE_NAME,
                databinder.west.root,
                viewModel.swipeWestLiveData,
                DefaultAppType.SMS,
                { databinder.hexItemWest = it },
                preferenceSwipeWestResultContract
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeEast.PACKAGE_NAME,
                databinder.east.root,
                viewModel.swipeEastLiveData,
                DefaultAppType.BROWSER,
                { databinder.hexItemEast = it },
                preferenceSwipeEastResultContract
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeSouthWest.PACKAGE_NAME,
                databinder.southWest.root,
                viewModel.swipeSouthWestLiveData,
                DefaultAppType.EMAIL,
                { databinder.hexItemSouthWest = it },
                preferenceSwipeSouthWestResultContract
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeSouth.PACKAGE_NAME,
                databinder.south.root,
                viewModel.swipeSouthLiveData,
                null,
                { databinder.hexItemSouth = it },
                null
            ),
            GestureConfiguration(
                PreferenceKeys.Gestures.SwipeSouthEast.PACKAGE_NAME,
                databinder.southEast.root,
                viewModel.swipeSouthEastLiveData,
                DefaultAppType.MAP,
                { databinder.hexItemSouthEast = it },
                preferenceSwipeSouthEastResultContract
            ),
        )

        databinder.appInfoAdapter = LauncherFragmentDatabindingAdapter

        measureGestureView(databinder)

        databinder.root.setOnTouchListener(makeOnTouchListener(databinder))

        databinder.root.setOnCreateContextMenuListener { menu, _, _ ->
            if (generatingGestureContextMenu) {
                generatingGestureContextMenu = false
                return@setOnCreateContextMenuListener
            }
            menu.add(R.string.menu_item_home_choose_wallpaper).setOnMenuItemClickListener {
                wallpaperPickerContract.launch(Intent(Intent.ACTION_PICK).apply { type = "image/*" })
                true
            }
            menu.add(R.string.menu_item_home_manage_widgets).setOnMenuItemClickListener {
                startActivity(Intent(activity, HomeArrangementActivity::class.java))
                true
            }
            menu.add(R.string.menu_item_home_settings).setOnMenuItemClickListener {
                activity?.let { startActivity(Intent(it, SettingsActivity::class.java)) }
                true
            }
        }

        gestures.forEach { config ->
            config.setHexItemFunc(makeHexItem(context, R.string.gesture_set_quick_access_item, R.drawable.ic_add))
            if (config.launcher != null) {
                setCreateGestureAction(config.view, config.launcher)
            }
        }
        databinder.hexItemNorth = makeHexItem(context, R.string.gesture_search_apps, R.drawable.ic_apps)
        databinder.north.root.setTag(
            R.id.gesture_icon_action,
            object : Runnable {
                override fun run() {
                    showLauncherFragment()
                }
            }
        )
        databinder.hexItemSouth = makeHexItem(context, R.string.gesture_search_apps, R.drawable.outline_notifications)
        databinder.south.root.setTag(
            R.id.gesture_icon_action,
            object : Runnable {
                override fun run() {
                    context?.let { NotificationShadeUtil.showNotificationShade(it) }
                }
            }
        )

        gestures.filter {
            it.key != PreferenceKeys.Gestures.SwipeNorth.PACKAGE_NAME &&
                it.key != PreferenceKeys.Gestures.SwipeSouth.PACKAGE_NAME
        }.forEach { config ->
            config.preferenceWatcher?.observe(viewLifecycleOwner) { componentName ->
                setHexItemContextMenu(config)
                when (componentName) {
                    null -> { /* ignore */ }
                    PreferenceKeys.Gestures.GESTURE_UNWANTED -> {
                        config.view.visibility = View.GONE
                    }
                    else -> {
                        ComponentName.unflattenFromString(componentName)?.let {
                            ensureAppInstalled(it) { config.componentName = it }
                            setAppInformationForGesture(config)
                        } ?: run {
                            context?.let { context ->
                                context.packageManager?.let { pacman ->
                                    pacman.getLaunchIntentForPackage(componentName)
                                        ?.resolveActivity(pacman)
                                        ?.let { resolvedName ->
                                            PreferencesRepository.getPrefs(context) {
                                                it.edit {
                                                    putString(config.key, resolvedName.flattenToString())
                                                }
                                            }
                                        }
                                }
                            }
                        }
                    }
                }
            }
        }

        viewModel.appListLiveData.observe(viewLifecycleOwner) {
            appList = it
            gestures.forEach { config ->
                if (config.componentName != null) {
                    setAppInformationForGesture(config)
                }
            }
            gestures.forEach { gesture -> initDefaultApp(gesture) }
            onLoadingComplete()
        }

        viewModel.gestureOpacityLiveData.observe(viewLifecycleOwner) { o ->
            val opacity = (o ?: 100).toFloat() / 100
            gestures.map { it.view }.forEach { view ->
                performViewAction(view) {
                    if (it is ImageView) {
                        it.alpha = opacity
                    }
                }
            }
        }
    }

    override fun handleBackPressed(): Boolean = true /* consume, this is a launcher*/

    private fun makeHexItem(
        context: Context,
        @StringRes label: Int,
        @DrawableRes icon: Int,
    ): HexItem = object : HexItem {
        override val label = resources.getString(label)
        override val icon: Provider<Drawable?> = Provider(
            {
                AdaptiveIconDrawable(
                    ColorDrawable(ContextCompat.getColor(context, R.color.colorOnPrimary)),
                    ContextCompat.getDrawable(context, icon)!!
                )
            },
            InlineExecutor
        )
        override val hidden: Boolean = false
        override val backgroundColor: Int = Color.TRANSPARENT
        override val backgroundHidden: Boolean = true
    }

    private fun setCreateGestureAction(v: View, activityResultLauncher: ActivityResultLauncher<Intent>) {
        v.setTag(
            R.id.gesture_icon_action,
            object : Runnable {
                override fun run() {
                    showSetAppForGesture(activityResultLauncher)
                }
            }
        )
    }

    private fun setAppInformationForGesture(config: GestureConfiguration) {
        appList?.firstOrNull { info -> info.componentName == config.componentName }?.let { appInfo ->
            config.setHexItemFunc(appInfo)
            config.view.visibility = View.VISIBLE
            config.appName = appInfo.label
            setOpenAppAction(config.view, appInfo)
        }
    }

    private fun setHexItemContextMenu(gesture: GestureConfiguration) {
        gesture.view.setOnCreateContextMenuListener { menu, _, _ ->
            gesture.appName?.let { menu.setHeaderTitle(it) }
            generatingGestureContextMenu = true // TODO this is a dirty hack
            gesture.launcher?.let { launcher ->
                menu.add(R.string.gesture_item_menu_change).setOnMenuItemClickListener {
                    showSetAppForGesture(launcher)
                    true
                }
            }
            menu.add(R.string.gesture_item_menu_disable).setOnMenuItemClickListener {
                val context = context ?: return@setOnMenuItemClickListener true
                PreferencesRepository.getPrefs(context) {
                    it.edit {
                        putString(gesture.key, PreferenceKeys.Gestures.GESTURE_UNWANTED)
                    }
                }
                true
            }
        }
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

    private fun showSetAppForGesture(activityResultLauncher: ActivityResultLauncher<Intent>) {
        activityResultLauncher.launch(
            Intent(activity, AppListActivity::class.java)
                .decorateForAppListLaunch(R.string.gesture_app_chooser_title)
        )
    }

    private fun measureGestureView(databinder: FragmentHomeBinding) {
        databinder.root.post(object : Runnable {
            override fun run() {
                if (databinder.gestureContainer.height > 0) {
                    gestureViewHalfWidth = databinder.gestureContainer.width / 2
                    gestureViewHalfHeight = databinder.gestureContainer.height / 2
                    databinder.gestureContainer.visibility = View.GONE
                } else {
                    databinder.root.post(this)
                }
            }
        })
    }

    private fun makeOnTouchListener(databinder: FragmentHomeBinding): View.OnTouchListener {
        val longPressTime = (ViewConfiguration.getLongPressTimeout() * 1.5).toLong()
        val doubleTapTime = ViewConfiguration.getDoubleTapTimeout()

        return object : View.OnTouchListener {

            private var showContextMenuRunnable: Runnable? = null
            private var showGestureDetailsContextMenuRunnable: Runnable? = null

            private lateinit var downPosition: PointF
            private var ignoreEvent: Boolean = false
            private var lastPosition: PointF = PointF()
            private var lastDown: Long = -1
            private var lastAction: Int = MotionEvent.ACTION_UP
            private var currentlyActive: Triple<View, Int, Int>? = null
            private var showingItemContextMenu = false

            override fun onTouch(view: View, me: MotionEvent): Boolean {
                if (ignoreEvent && me.action != MotionEvent.ACTION_DOWN) {
                    return false
                }

                when (me.action) {
                    MotionEvent.ACTION_DOWN -> {
                        ignoreEvent = me.rawX > screenWidth - edgeExclusionZone
                        if (ignoreEvent) return false
                        gestures.map { it.view }.forEach { makeGreyscale(it) }

                        showingItemContextMenu = false
                        downPosition = PointF(me.rawX, me.rawY)
                        databinder.gestureContainer.x = me.x - gestureViewHalfWidth
                        databinder.gestureContainer.y = me.y - gestureViewHalfHeight
                        databinder.gestureContainer.visibility = View.VISIBLE
                        if (now() - lastDown <= doubleTapTime) {
                            showContextMenuRunnable = makeShowContextMenuRunnable(
                                databinder.gestureContainer
                            ).also {
                                view.postDelayed(it, longPressTime)
                            }
                        }
                        lastDown = now()
                    }
                    MotionEvent.ACTION_UP -> {
                        if (!showingItemContextMenu) {
                            (currentlyActive?.first?.getTag(R.id.gesture_icon_action) as? Runnable)?.run()
                        }
                        gestures.map { it.view }.forEach { removeGreyscale(it) }
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
                            gestures.filter { isActive(me, it.view) }.forEach {
                                stoppedTouchingView()
                                startedTouchingView(it.view)
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
                lastAction = me.action
                lastPosition.x = me.rawX
                lastPosition.y = me.rawY
                return true
            }

            private fun now() = System.currentTimeMillis()

            private fun makeShowContextMenuRunnable(gestureContainer: View) = Runnable {
                stoppedTouchingView()
                view?.showContextMenu()
                gestureContainer.visibility = View.GONE
            }

            private fun makeShowGestureDetailsContextMenuRunnable(v: View) = Runnable {
                if (lastAction == MotionEvent.ACTION_UP) return@Runnable
                if (!getViewLocation(v).contains(lastPosition.x.toInt(), lastPosition.y.toInt())) return@Runnable
                v.showContextMenu(v.width.toFloat() / 2, v.height.toFloat() / 2)
                showingItemContextMenu = true
            }

            val xy = intArrayOf(0, 0)
            val rect = Rect()
            private fun getViewLocation(v: View): Rect {
                v.getLocationOnScreen(xy)
                rect.top = xy[1]
                rect.left = xy[0]
                rect.bottom = rect.top + v.height
                rect.right = rect.left + v.width
                return rect
            }

            private fun isActive(me: MotionEvent, v: View): Boolean {
                if (v.visibility != View.VISIBLE) return false
                val rect = getViewLocation(v)
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
                line2y2: Int
            ): Boolean {

                val s1_x = line1x2 - line1x1
                val s1_y = line1y2 - line1y1
                val s2_x = line2x2 - line2x1
                val s2_y = line2y2 - line2y1

                val s = (-s1_y * (line1x1 - line2x1) + s1_x * (line1y1 - line2y1)) / (-s2_x * s1_y + s1_x * s2_y)
                val t = (s2_x * (line1y1 - line2y1) - s2_y * (line1x1 - line2x1)) / (-s2_x * s1_y + s1_x * s2_y)

                return s >= 0 && s <= 1 && t >= 0 && t <= 1
            }

            private fun startedTouchingView(v: View) {
                currentlyActive = Triple(v, v.layoutParams.width, v.layoutParams.height)
                removeGreyscale(v)
                v.requestLayout()
                showGestureDetailsContextMenuRunnable = makeShowGestureDetailsContextMenuRunnable(v)
                v.postDelayed(showGestureDetailsContextMenuRunnable, longPressTime)
            }

            private fun stoppedTouchingView() {
                currentlyActive?.let { current ->
                    showGestureDetailsContextMenuRunnable?.let { current.first.removeCallbacks(it) }
                    makeGreyscale(current.first)
                }
                currentlyActive = null
            }
        }
    }

    private fun ensureAppInstalled(componentName: ComponentName, action: () -> Unit) {
        PackageManagerExecutor.execute {
            try {
                context?.packageManager?.getPackageInfo(componentName.packageName, 0) ?: return@execute
                action()
            } catch (_: Exception) { /* package isn't installed */ }
        }
    }

    private fun makeOpenAppRunnable(appInfo: AppInfo): Runnable = Runnable {
        try {
            startActivity(Intent().apply { component = appInfo.componentName }.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: Exception) {
            Timber.e(e, "Unable to open app: ${appInfo.componentName}")
        }
    }

    private fun performViewAction(v: View, action: (View) -> Unit) {
        if (v is ViewGroup) {
            for (i in 0 until v.childCount) {
                performViewAction(v.getChildAt(i), action)
            }
        } else {
            action(v)
        }
    }

    private fun removeGreyscale(v: View) {
        performViewAction(v) { view ->
            if (view is ImageView) {
                view.colorFilter = null
            }
        }
    }

    private fun makeGreyscale(v: View) {
        performViewAction(v) { view ->
            if (view is ImageView) {
                view.colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
            }
        }
    }

    private fun initDefaultApp(gesture: GestureConfiguration) {
        if (gesture.defaultApp == null) return
        getDefaultApp(gesture.defaultApp) { appInfo ->
            if (gesture.componentName != null) return@getDefaultApp
            gesture.componentName = appInfo.componentName
            gesture.appName = appInfo.label
            gesture.setHexItemFunc(appInfo)
            setOpenAppAction(gesture.view, appInfo)
        }
    }

    private fun getDefaultApp(type: DefaultAppType, action: (AppInfo) -> Unit) {
        val callback = OriginalThreadCallback(action)
        PackageManagerExecutor.execute {
            context?.packageManager?.let { pacman ->
                type.intent().resolveActivity(pacman)
                    ?.let { componentName ->
                        appList?.firstOrNull { it.componentName == componentName }
                            ?.let { callback.invoke(it) }
                    }
            }
        }
    }

    private class GestureConfiguration(
        val key: String,
        val view: View,
        val preferenceWatcher: LiveData<String?>?,
        val defaultApp: DefaultAppType?,
        val setHexItemFunc: (HexItem) -> Unit,
        val launcher: ActivityResultLauncher<Intent>?
    ) {
        var componentName: ComponentName? = null
        var appName: String? = null
    }

    private enum class DefaultAppType(val intent: () -> Intent) {
        CAMERA({ Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA) }),
        BROWSER({ Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/")) }),
        PHONE({ Intent(Intent.ACTION_DIAL) }),
        SMS({ Intent(Intent.ACTION_MAIN).also { it.addCategory(Intent.CATEGORY_APP_MESSAGING) } }),
        EMAIL({ Intent(Intent.ACTION_MAIN).also { it.addCategory(Intent.CATEGORY_APP_EMAIL) } }),
        MAP({ Intent(Intent.ACTION_MAIN).also { it.setPackage("com.google.android.apps.maps") } }),
    }
}
