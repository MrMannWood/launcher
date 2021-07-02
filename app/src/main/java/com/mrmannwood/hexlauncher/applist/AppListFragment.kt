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
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.hexlauncher.launcher.LauncherViewModel
import com.mrmannwood.hexlauncher.qwertyMistakes
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding
import kotlinx.coroutines.*
import timber.log.Timber
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

    private var numColumnsInAppList: Int = 0

    private val viewModel : LauncherViewModel by activityViewModels()

    private var apps : List<AppInfo>? = null

    private fun getAppListHost() : Host<*> {
        return (requireActivity() as AppListHostActivity).getAppListHost()
    }

    override val nameForInstrumentation = "AppListFragment"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        numColumnsInAppList = calculateNoOfColumnsForAppList(resources)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultListAdapter = createResultAdapter()
        resultListView = view.findViewById<RecyclerView>(R.id.result_list).apply {
            layoutManager = createResultLayoutManager()
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
        viewModel.apps.observe(viewLifecycleOwner, { appList ->
            apps = appList
            performSearch()
        })
    }

    private fun createResultLayoutManager() : GridLayoutManager {
        return object : GridLayoutManager(
                requireContext(),
                numColumnsInAppList,
                RecyclerView.VERTICAL,
                true /* reverseLayout */
        ) {
            override fun isLayoutRTL() : Boolean = true
        }.apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return 1
                }
            }
        }
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
            searchApps(apps, search, numColumnsInAppList).map { it }
        )
    }
}


fun searchApps(apps: List<AppInfo>?, term: String, maxReturn: Int) : List<AppInfo> {
    return apps?.let { data ->
        if (term.isEmpty()) {
            Collections.emptyList()
        } else {
            val sorted = mutableMapOf<AppInfo, Int>()
            val length = term.length
            data.forEach {
                if (term.contains(' ')) {
                    if (length <= it.lowerLabel.length) {
                        sorted[it] = term.qwertyMistakes(it.lowerLabel.substring(0, length))
                    }
                } else {
                    var smallestVal = Int.MAX_VALUE
                    for (label in it.labelComponents) {
                        if (length <= label.length) {
                            val result = term.qwertyMistakes(label.substring(0, length))
                            if (result < smallestVal) {
                                smallestVal = result
                            }
                        }
                    }
                    sorted[it] = smallestVal
                }
            }
            sorted.filter { it.value != Int.MAX_VALUE }
                .toList()
                .sortedWith(compareBy({ it.second }, { it.first.label }))
                .map { it.first }
                .take(maxReturn)
        }
    } ?: Collections.emptyList()
}