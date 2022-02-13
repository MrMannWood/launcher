package com.mrmannwood.hexlauncher.applist

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.launcher.*
import com.mrmannwood.hexlauncher.qwertyMistakes
import com.mrmannwood.hexlauncher.view.HexagonalGridLayoutManager
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

    private fun getAppListHost() : Host<*> {
        return (requireActivity() as AppListHostActivity).getAppListHost()
    }

    override val nameForInstrumentation = "AppListFragment"

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultListAdapter = createResultAdapter()
        resultListView = view.findViewById<RecyclerView>(R.id.result_list).apply {
            layoutManager = HexagonalGridLayoutManager()
            adapter = resultListAdapter
        }

        searchView = view.findViewById(R.id.search)
        searchView.handleBackPressed = object : HandleBackPressed {
            override fun handleBackPressed(): Boolean {
                getAppListHost().end(null)
                return true
            }
        }
        searchView.addTextChangedListener(createSearchTextListener())
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                false
            } else {
                getAppListHost().onSearchButtonPressed(searchView.text.toString())
                true
            }
        }

        startObservingLiveData()

        viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                showKeyboardJob = viewLifecycleOwner.lifecycleScope.launch {
                    forceShowKeyboard(searchView)
                }
            }
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                viewLifecycleOwner.lifecycleScope.launch {
                    showKeyboardJob?.cancelAndJoin()
                    hideKeyboard(requireActivity())
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
        viewModel.enableCategorySearch.observe(viewLifecycleOwner, { enable ->
            enableCategorySearch = enable != false
        })
        viewModel.apps.observe(viewLifecycleOwner, { appList ->
            apps = appList
            performSearch()
        })
    }

    private fun createResultAdapter(): Adapter<AppInfo> {
        val idGenerator = Adapter.IdGenerator(listOf(AppInfo::class to { it.packageName }))
        return Adapter(
                context = requireContext(),
                order = arrayOf(AppInfo::class),
                idFunc = idGenerator::genId,
                viewFunc = { R.layout.list_app_item },
                bindFunc = { vdb, appInfo ->
                    (vdb as ListAppItemBinding).apply {
                        this.appInfo = appInfo
                        this.adapter = LauncherFragmentDatabindingAdapter
                    }
                    getAppListHost().onAppInfoBinding(vdb.root, appInfo)
                    vdb.root.setOnClickListener {
                        getAppListHost().onAppSelected(appInfo)
                    }
                }
        )
    }

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            performSearch()
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

    private fun performSearch() {
        val search = searchView.text.toString().trim().lowercase(Locale.ROOT)
        resultListAdapter.setData(
            AppInfo::class,
            searchApps(apps, search, enableCategorySearch, 8).map { it }
        )
    }
}

fun searchApps(apps: List<AppInfo>?, term: String, searchCategories: Boolean, maxReturn: Int) : List<AppInfo> {
    if (apps == null || term.isEmpty()) {
        return Collections.emptyList();
    }
    val matchingApps = mutableListOf<Triple<Int, SearchTermType, AppInfo>>()
    val length = term.length
    apps.forEach {
        if (term.contains(' ')) {
            if (length <= it.lowerLabel.length) {
                val result = term.qwertyMistakes(it.lowerLabel.substring(0, length))
                if (result != Int.MAX_VALUE) {
                    matchingApps.add(Triple(result, SearchTermType.FullName, it))
                }
            }
        } else {
            it.searchTerms.forEach { (label, type) ->
                if (length <= label.length && (searchCategories || type !=SearchTermType.Category)) {
                    val minAcceptable = if (type != SearchTermType.Label) 0 else Int.MAX_VALUE - 1
                    val result = term.qwertyMistakes(label.substring(0, length))
                    if (result <= minAcceptable) {
                        matchingApps.add(Triple(result, type, it))
                    }
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
        .take(maxReturn)
}