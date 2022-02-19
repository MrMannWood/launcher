package com.mrmannwood.hexlauncher.allapps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.appcustomize.AppCustomizationFragment
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemFullBinding

class AllAppsListFragment : Fragment() {

    private lateinit var resultListView: RecyclerView
    private lateinit var resultListAdapter: Adapter<AppInfo>

    private val viewModel : AllAppsViewModel by activityViewModels()

    private var leftHandedLayout: Boolean = false

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

        viewModel.apps.observe(viewLifecycleOwner) { appList ->
            resultListAdapter.setData(AppInfo::class, appList)
        }
        viewModel.leftHandedLayout.observe(viewLifecycleOwner) { leftHanded ->
            if (leftHanded == null) return@observe
            if (leftHandedLayout == leftHanded) return@observe
            leftHandedLayout = leftHanded
            resultListAdapter.notifyDataSetChanged()
        }
    }

    private fun createResultLayoutManager() : RecyclerView.LayoutManager {
        return LinearLayoutManager(requireContext()).apply {
            reverseLayout = true
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
                    leftHanded = leftHandedLayout
                }
                vdb.root.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.setHeaderTitle(result.label)
                    menu.add(R.string.menu_item_app_details).setOnMenuItemClickListener {
                        startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = Uri.parse("package:${result.packageName}")
                        })
                        true
                    }
                    menu.add(R.string.menu_item_app_customize).setOnMenuItemClickListener {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.settings_root, AppCustomizationFragment.forPackage(result.packageName))
                            .addToBackStack("AppCustomizationFragment")
                            .commit()
                        true
                    }
                    menu.add(R.string.menu_item_uninstall_app_title).setOnMenuItemClickListener {
                        startActivity(Intent(Intent.ACTION_DELETE).apply {
                            data = Uri.parse("package:${result.packageName}")
                        })
                        true
                    }
                }
            }
        )
    }
}