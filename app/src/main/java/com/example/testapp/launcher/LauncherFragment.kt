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
import com.example.testapp.qwertyMistakes

class LauncherFragment : Fragment(), HandleBackPressed {

    private lateinit var searchView: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter<DecoratedAppInfo>

    private val viewModel : LauncherViewModel by activityViewModels()

    private var data : List<DecoratedAppInfo>? = null

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
//        recyclerView.addItemDecoration(createItemDecoration(layoutManager.spanCount))
        recyclerView.adapter = adapter

        viewModel.apps.observe(viewLifecycleOwner, Observer {
            it.getOrNull()?.let { apps ->
                val data = augmentAppsWithSpacingElements(apps)
                this.data = data
                adapter.setData(data)
            }
            it.exceptionOrNull()?.let {  error ->
                throw Exception("Oh No!", error)
            }
        })

        searchView.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(p0: Editable) {
                data?.let { data ->
                    val search = searchView.text.toString().toLowerCase()
                    if (search.isEmpty()) {
                        adapter.setData(data)
                    } else {
                        val sorted = mutableMapOf<AppInfo, Int>()
                        val length = search.length
                        data.filterIsInstance<DecoratedAppInfo.AppInfoWrapper>()
                            .map { it.appInfo}
                            .forEach {
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
                        adapter.setData(
                            augmentAppsWithSpacingElements(
                                sorted.filter { it.value != Int.MAX_VALUE }
                                    .toList()
                                    .sortedWith(compareBy({ it.second }, { it.first.label }))
                                    .map { it.first })
                        )
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        })

        forceShowKeyboard(searchView)
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun augmentAppsWithSpacingElements(data: List<AppInfo>) : List<DecoratedAppInfo> {
        val d : MutableList<DecoratedAppInfo> = data.map { DecoratedAppInfo.AppInfoWrapper(it) }.toMutableList()
        d.add(0, DecoratedAppInfo.FullSpan)
        return d
    }

    private fun createLayoutManager(context: Context) : GridLayoutManager {
        val layoutManager = object : GridLayoutManager(context, calculateNoOfColumns(), RecyclerView.VERTICAL, true) {
            override fun isLayoutRTL() : Boolean = true
        }

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                return data?.let { data ->
                    when (data[position]) {
                        is DecoratedAppInfo.FullSpan -> layoutManager.spanCount
                        else -> 1
                    }
                } ?: 1
            }
        }

        return layoutManager
    }

    private fun createItemDecoration(spanCount: Int) : RecyclerView.ItemDecoration {
        return object : RecyclerView.ItemDecoration() {
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                super.getItemOffsets(outRect, view, parent, state)
                val position = parent.getChildAdapterPosition(view)
                if (position < spanCount) {
                    outRect.set(0, 0, 0, searchView.height)
                } else {
                    outRect.set(0, 0, 0, 0)
                }
            }
        }
    }

    private fun createAdapter(context: Context): Adapter<DecoratedAppInfo> =
        Adapter(
            context = context,
            viewId = R.layout.list_app_item,
            bindFunc = { vdb, appInfo ->
                if (appInfo is DecoratedAppInfo.AppInfoWrapper) {
                    (vdb as ListAppItemBinding).appInfo = appInfo.appInfo
                    vdb.root.setOnClickListener {
                        context.packageManager.getLaunchIntentForPackage(appInfo.appInfo.packageName)?.let { intent ->
                            startActivity(intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                        } ?: Toast.makeText(context, "Unable to start app", Toast.LENGTH_LONG).show()
                    }
                } else {
                    (vdb as ListAppItemBinding).appInfo = null
                }
            }
        )

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
}