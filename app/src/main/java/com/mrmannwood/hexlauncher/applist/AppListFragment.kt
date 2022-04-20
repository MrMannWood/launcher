package com.mrmannwood.hexlauncher.applist

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.launcher.*
import com.mrmannwood.hexlauncher.levenshtein
import com.mrmannwood.hexlauncher.view.HexagonalGridLayoutManager
import com.mrmannwood.hexlauncher.view.HexagonalGridLayoutManager.Corner
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding
import kotlinx.coroutines.*
import java.util.*

class AppListFragment : InstrumentedFragment(), HandleBackPressed {

    abstract class Host<T>(private val killFragment: (T?) -> Unit) {

        fun end() = end(null)

        fun end(value: T?) {
            killFragment(value)
        }

        abstract fun onAppSelected(appInfo: AppInfo)
        open fun onSearchButtonPressed(searchTerm: String) { }
        open fun onAppInfoBinding(view: View, appInfo: AppInfo) { }
    }

    // TODO this is really hacky, and should be replaced with ViewModel
    interface AppListHostActivity {
        fun getAppListHost() : Host<*>
    }

    private lateinit var searchView: KeyboardEditText
    private lateinit var resultListView: RecyclerView
    private lateinit var resultListAdapter: Adapter<AppInfo>
    private var showKeyboardJob : Job? = null

    private val viewModel : LauncherViewModel by activityViewModels()

    private var apps : List<AppInfo>? = null
    private var enableCategorySearch : Boolean = true
    private var enableAllAppsSearch : Boolean = false
    private var leftHandedLayout : Boolean = false

    private fun getAppListHost() : Host<*>? {
        return (activity as? AppListHostActivity)?.getAppListHost()
    }

    override val nameForInstrumentation = "AppListFragment"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultListAdapter = createResultAdapter(view.context)
        resultListView = view.findViewById<RecyclerView>(R.id.result_list).apply {
            layoutManager = createLayoutManager()
            adapter = resultListAdapter
        }

        searchView = view.findViewById(R.id.search)
        searchView.handleBackPressed = object : HandleBackPressed {
            override fun handleBackPressed(): Boolean {
                getAppListHost()?.end(null)
                return true
            }
        }
        searchView.addTextChangedListener(createSearchTextListener())
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                false
            } else {
                getAppListHost()?.onSearchButtonPressed(searchView.text.toString())
                true
            }
        }

        startObservingLiveData()

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                showKeyboardJob = viewLifecycleOwner.lifecycleScope.launch {
                    forceShowKeyboard(searchView)
                }
            }
            override fun onStop(owner: LifecycleOwner) {
                viewLifecycleOwner.lifecycleScope.launch {
                    showKeyboardJob?.cancelAndJoin()
                    activity?.let { hideKeyboard(it) }
                }
            }
        })
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.enableCategorySearch.observe(viewLifecycleOwner) { enable ->
            enableCategorySearch = enable != false
        }
        viewModel.enableAllAppsSearch.observe(viewLifecycleOwner) { enable ->
            enableAllAppsSearch = enable == true
        }
        viewModel.leftHandedLayout.observe(viewLifecycleOwner) { leftHanded ->
            if (leftHanded == null) return@observe
            if (leftHandedLayout == leftHanded) return@observe
            leftHandedLayout = leftHanded
            resultListView.layoutManager = createLayoutManager()
        }
        viewModel.apps.observe(viewLifecycleOwner) { appList ->
            apps = appList
            performSearch()
        }
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return HexagonalGridLayoutManager(
            if (leftHandedLayout) Corner.BOTTOM_LEFT else Corner.BOTTOM_RIGHT
        )
    }

    private fun createResultAdapter(context: Context): Adapter<AppInfo> {
        val idGenerator = Adapter.IdGenerator(listOf(AppInfo::class to { it.packageName }))
        return Adapter(
                context = context,
                order = arrayOf(AppInfo::class),
                idFunc = idGenerator::genId,
                viewFunc = { R.layout.list_app_item },
                bindFunc = { vdb, appInfo ->
                    (vdb as ListAppItemBinding).apply {
                        this.hexItem = appInfo
                        this.adapter = LauncherFragmentDatabindingAdapter
                    }
                    getAppListHost()?.onAppInfoBinding(vdb.root, appInfo)
                    vdb.root.setOnClickListener {
                        getAppListHost()?.onAppSelected(appInfo)
                    }
                }
        )
    }

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            if (enableAllAppsSearch && searchView.text.toString() == "...") {
                showAllApps()
            } else {
                performSearch()
            }
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }

    private suspend fun forceShowKeyboard(view: EditText) {
        withContext(Dispatchers.Main) {
            view.requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val rootView = requireActivity().window.decorView
                while (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    rootView.windowInsetsController!!.show(WindowInsets.Type.ime())
                    if (rootView.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true) {
                        break
                    }
                    delay(20)
                }
            } else {
                val imm =
                    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }
    }

    private suspend fun hideKeyboard(activity: Activity) {
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = searchView.windowInsetsController!!
                controller.hide(WindowInsets.Type.ime())
            } else {
                val windowToken = activity.currentFocus?.windowToken ?: searchView.windowToken
                (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    windowToken,
                    0
                )
            }
        }
    }

    private fun showAllApps() {
        val activity = activity ?: return

        if (resultListView.layoutManager !is GridLayoutManager) {
            val displayMetrics: DisplayMetrics = resources.displayMetrics
            val numColumns = (displayMetrics.widthPixels / (resources.getDimension(R.dimen.hex_view_height) + 24)).toInt()

            resultListView.layoutManager = object : GridLayoutManager(
                activity,
                numColumns,
                RecyclerView.VERTICAL,
                true /* reverseLayout */
            ) {
                override fun isLayoutRTL() : Boolean = !leftHandedLayout
            }.apply {
                spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                    override fun getSpanSize(position: Int) = 1
                }
            }
        }

        resultListAdapter.setData(AppInfo::class, apps ?: emptyList())

        viewLifecycleOwner.lifecycleScope.launch {
            showKeyboardJob?.cancelAndJoin()
            hideKeyboard(activity)
        }
    }

    private fun performSearch() {
        if (resultListView.layoutManager !is HexagonalGridLayoutManager) {
            resultListView.layoutManager = createLayoutManager()
        }

        val search = searchView.text.toString().trim().lowercase(Locale.ROOT)
        resultListAdapter.setData(
            AppInfo::class,
            searchApps(apps, search).map { it }
        )
    }

    private fun searchApps(apps: List<AppInfo>?, term: String) : List<AppInfo> {
        if (apps == null || term.isEmpty()) {
            return Collections.emptyList();
        }
        val matchingApps = mutableListOf<Triple<Int, SearchTermType, AppInfo>>()
        val length = term.length
        apps.forEach { app ->
            app.searchTerms.forEach { (label, type) ->
                if (length <= label.length && (enableCategorySearch || type != SearchTermType.Category)) {
                    val minAcceptable = if (type != SearchTermType.Label) 0 else (length / 3)
                    val result = term.levenshtein(label.substring(0, length))
                    if (result <= minAcceptable) {
                        matchingApps.add(Triple(result, type, app))
                    }
                }
            }
        }

        return matchingApps.sortedWith(compareBy(
            { it.first },
            { it.second },
            { it.third.label }
        ))
            .map { it.third }
            .distinct()
            .take(8)
    }
}
