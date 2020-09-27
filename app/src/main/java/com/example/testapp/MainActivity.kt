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
        recyclerView.layoutManager =
            object : GridLayoutManager(this, calculateNoOfColumns(), RecyclerView.VERTICAL, true) {
                override fun isLayoutRTL() : Boolean = true
            }
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

                        adapter.setData(
                            sorted.filter { it.value != Int.MAX_VALUE }
                                .toList()
                                .sortedWith(compareBy({ it.second }, { it.first.label }))
                                .map { it.first }
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