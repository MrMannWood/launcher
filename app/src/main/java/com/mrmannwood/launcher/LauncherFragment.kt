package com.mrmannwood.launcher

import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.HandleBackPressed
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding
import com.mrmannwood.qwertyMistakes
import com.mrmannwood.view.KeyboardEditText
import timber.log.Timber
import java.util.*


class LauncherFragment : Fragment(), HandleBackPressed {

    private lateinit var searchView: KeyboardEditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter<DecoratedAppInfo>

    private var numColumns: Int = 0

    private val viewModel : LauncherViewModel by activityViewModels()

    private var data : List<AppInfo>? = null

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_launcher, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        searchView = view.findViewById(R.id.search)
        recyclerView = view.findViewById(R.id.list)

        numColumns = calculateNoOfColumns()

        adapter = createAdapter(view.context)
        val layoutManager = createLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        searchView.handleBackPressed = object : HandleBackPressed {
            override fun handleBackPressed(): Boolean {
                performActionAndEnd { /* no-op */ }
                return true
            }
        }
        searchView.addTextChangedListener(createSearchTextListener())
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                false
            } else {
                performActionAndEnd {
                    startActivity(Intent(Intent.ACTION_WEB_SEARCH).apply {
                        putExtra(SearchManager.QUERY, searchView.text.toString())
                    })
                }
                true
            }
        }

        startObservingLiveData()
    }

    override fun onStart() {
        super.onStart()
        forceShowKeyboard(searchView)
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.apps.observe(viewLifecycleOwner, {
            it.getOrNull()?.let { apps ->
                this.data = apps
                performSearch()
            }
            it.exceptionOrNull()?.let { error ->
                Timber.e(error)
                throw Exception("Oh No!", error)
            }
        })
    }

    private fun createLayoutManager(context: Context) : GridLayoutManager {
        return object : GridLayoutManager(context, numColumns, RecyclerView.VERTICAL, true) {
            override fun isLayoutRTL() : Boolean = true
        }
    }

    private fun createAdapter(context: Context): Adapter<DecoratedAppInfo> =
        Adapter(
                context = context,
                viewFunc = { R.layout.list_app_item },
                bindFunc = { vdb, appInfo ->
                    when (appInfo) {
                        is DecoratedAppInfo.AppInfoWrapper -> {
                            (vdb as ListAppItemBinding).appInfo = appInfo.appInfo
                            vdb.adapter = LauncherFragmentDatabindingAdapter
                            vdb.root.setOnClickListener {
                                performActionAndEnd {
                                    appInfo.startApplication(vdb.root.context)
                                }
                            }
                            vdb.root.setOnLongClickListener {
                                performActionAndEnd {
                                    startActivity(IconDetailsActivity.buildIntent(requireActivity(), appInfo.appInfo))
                                }
                                true
                            }
                        }
                    }
                }
        )

    private fun performActionAndEnd(action: () -> Unit) {
        action()
        hideKeyboard(requireActivity())
        parentFragmentManager.popBackStack()
    }

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            performSearch()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }

    private fun calculateNoOfColumns(): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / 75 + 0.5).toInt()
    }

    private fun forceShowKeyboard(view: EditText) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private fun hideKeyboard(activity: Activity) {
        val windowToken = activity.currentFocus?.windowToken ?: searchView.windowToken
        (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun performSearch() {
        data?.let { data ->
            val search = searchView.text.toString().trim().toLowerCase()
            val filtered =
                    if (search.isEmpty()) {
                        Collections.emptyList()
                    } else {
                        val sorted = mutableMapOf<AppInfo, Int>()
                        val length = search.length
                        data.forEach {
                            if (search.contains(' ')) {
                                if (length <= it.lowerLabel.length) {
                                    sorted[it] = search.qwertyMistakes(it.lowerLabel.substring(0, length))
                                }
                            } else {
                                var smallestVal = Int.MAX_VALUE
                                for (label in it.labelComponents) {
                                    if (length <= label.length) {
                                        val result = search.qwertyMistakes(label.substring(0, length))
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
                    }
            adapter.setData(
                    filtered.map { DecoratedAppInfo.AppInfoWrapper(it) }
                            .take(numColumns)
                            .toList()
            )
        }
    }
}