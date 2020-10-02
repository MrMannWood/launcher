package com.example.testapp.launcher

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.HandleBackPressed
import com.example.testapp.R
import com.example.testapp.databinding.ListAppItemBinding
import com.example.testapp.databinding.ListItemSpacerBinding
import com.example.testapp.launcher.Toolbar.getStatusBarHeight
import com.example.testapp.qwertyMistakes

class LauncherFragment : Fragment(), HandleBackPressed {

    private lateinit var searchView: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter<DecoratedAppInfo>

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

        adapter = createAdapter(view.context)
        val layoutManager = createLayoutManager(view.context)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter

        searchView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                p0: View?, p1: Int, p2: Int, p3: Int, p4: Int, p5: Int, p6: Int, p7: Int, p8: Int
            ) {
                if (searchView.height > 0) {
                    searchView.removeOnLayoutChangeListener(this)
                    startObservingLiveData()
                }
            }
        })
        searchView.addTextChangedListener(createSearchTextListener())
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.apps.observe(viewLifecycleOwner, Observer {
            it.getOrNull()?.let { apps ->
                this.data = apps
                performSearch()
            }
            it.exceptionOrNull()?.let {  error ->
                throw Exception("Oh No!", error)
            }
        })
    }

    private fun augmentAppsWithSpacingElements(data: List<AppInfo>) : List<DecoratedAppInfo> {
        val d : MutableList<DecoratedAppInfo> = data.map { DecoratedAppInfo.AppInfoWrapper(it) }.toMutableList()
        d.add(0, DecoratedAppInfo.Space(searchView.height))
        d.add(DecoratedAppInfo.Space(requireContext().getStatusBarHeight()))
        return d
    }

    private fun createLayoutManager(context: Context) : GridLayoutManager {
        val layoutManager = object : GridLayoutManager(context, calculateNoOfColumns(), RecyclerView.VERTICAL, true) {
            override fun isLayoutRTL() : Boolean = true
        }

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return adapter.getItem(position)?.span?.let { if (it > 0) it else layoutManager.spanCount } ?: 1
            }
        }

        return layoutManager
    }

    private fun createAdapter(context: Context): Adapter<DecoratedAppInfo> =
        Adapter(
            context = context,
            viewFunc = { item: DecoratedAppInfo ->
                when(item) {
                    is DecoratedAppInfo.AppInfoWrapper -> R.layout.list_app_item
                    is DecoratedAppInfo.Space -> R.layout.list_item_spacer
                }
            },
            bindFunc = { vdb, appInfo ->
                when (appInfo) {
                    is DecoratedAppInfo.AppInfoWrapper -> {
                        (vdb as ListAppItemBinding).appInfo = appInfo.appInfo
                        vdb.root.setOnClickListener {
                            appInfo.startApplication(vdb.root.context)
                        }
                    }
                    is DecoratedAppInfo.Space -> {
                        (vdb as ListItemSpacerBinding).height = appInfo.height
                    }
                }
            }
        )

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

    private fun performSearch() {
        data?.let { data ->
            val search = searchView.text.toString().toLowerCase()
            val filtered =
                if (search.isEmpty()) {
                    data
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
            adapter.setData(augmentAppsWithSpacingElements(filtered))
        }
    }
}