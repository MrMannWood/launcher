package com.mrmannwood.hexlauncher.allapps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.applist.calculateNoOfColumnsForAppList
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemFullBinding

class AllAppsListFragment : Fragment() {

    private lateinit var resultListView: RecyclerView
    private lateinit var resultListAdapter: Adapter<AppInfo>

    private val viewModel : AllAppsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_all_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        resultListAdapter = createResultAdapter()
        resultListView = view.findViewById<RecyclerView>(R.id.app_list).apply {
            layoutManager = createResultLayoutManager()
            adapter = resultListAdapter
        }

        viewModel.apps.observe(viewLifecycleOwner, { appList ->
            resultListAdapter.setData(AppInfo::class, appList)
        })
    }

    private fun createResultLayoutManager() : GridLayoutManager {
        return object : GridLayoutManager(
            requireContext(),
            calculateNoOfColumnsForAppList(resources),
            RecyclerView.VERTICAL,
            true /* reverseLayout */
        ) {
            override fun isLayoutRTL() : Boolean = true
        }
    }

    private fun createResultAdapter(): Adapter<AppInfo> {
        val idGenerator = Adapter.IdGenerator(listOf(AppInfo::class to { it.packageName }))
        return Adapter(
            context = requireContext(),
            order = arrayOf(AppInfo::class),
            idFunc = idGenerator::genId,
            viewFunc = { R.layout.list_app_item_full },
            bindFunc = { vdb, result ->
                (vdb as ListAppItemFullBinding).apply {
                    appInfo = result
                    adapter = LauncherFragmentDatabindingAdapter
                }
            }
        )
    }
}