package com.example.testapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.testapp.databinding.ListAppItemBinding

class MainActivity : AppCompatActivity() {

    private lateinit var searchView: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: Adapter<AppInfo>

    private val viewModel: MainActivityViewModel by viewModels()

    private var data: List<AppInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()


        searchView = findViewById(R.id.search)
        recyclerView = findViewById(R.id.list)

        adapter = createAdapter()
        recyclerView.layoutManager = GridLayoutManager(this, calculateNoOfColumns(), RecyclerView.VERTICAL, true)
        recyclerView.adapter = adapter

        viewModel.apps.observe(this, Observer {
            it.result()?.let { apps ->
                data = apps
                adapter.setData(apps)
            }
            it.error()?.let {  error ->
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
                        val sorted = mutableListOf<Pair<Int, AppInfo>>()
                        val length = search.length
                        data.forEach {
                            if (length <= it.lowerLabel.length) {
                                val label = it.lowerLabel.substring(0, length)
                                val lDist = search.qwertyMistakes(label)
                                sorted.add(lDist to it)
                            }
                        }

                        adapter.setData(
                            sorted.sortedWith(compareBy({ it.first }, { it.second.label }))
                                .filter { it.first != Integer.MAX_VALUE }
                                .map { it.second }
                                .toList()
                        )
                    }
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
        })
    }

    private fun createAdapter(): Adapter<AppInfo> =
        Adapter(
            context = this,
            viewId = R.layout.list_app_item,
            bindFunc = { vdb, appInfo ->
                (vdb as ListAppItemBinding).appInfo = appInfo
                vdb.root.setOnClickListener {
                    startActivity(packageManager.getLaunchIntentForPackage(appInfo.packageName))
                }
            }
        )

    private fun calculateNoOfColumns(): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / 75 + 0.5).toInt()
    }
}