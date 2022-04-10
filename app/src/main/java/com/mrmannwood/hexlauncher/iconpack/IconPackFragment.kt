package com.mrmannwood.hexlauncher.iconpack

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.hexlauncher.view.HexagonalGridLayoutManager
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding

class IconPackFragment : Fragment(R.layout.fragment_icon_pack) {

    private val viewModel: IconPackViewModel by activityViewModels()

    private lateinit var iconPackAppView: RecyclerView
    private lateinit var iconPackAdapter: Adapter<AppInfo>

    private var leftHandedLayout : Boolean? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        iconPackAdapter = createResultAdapter()
        iconPackAppView = view.findViewById(R.id.icon_pack_app_list)
        iconPackAppView.adapter = iconPackAdapter

        viewModel.leftHandedLayout.observe(viewLifecycleOwner) { leftHanded ->
            leftHandedLayout = when {
                leftHanded == null -> false
                leftHandedLayout == leftHanded -> return@observe
                else -> leftHanded
            }
            iconPackAppView.layoutManager = createLayoutManager()
        }
        viewModel.iconPackAppsLiveData.observe(viewLifecycleOwner) { apps ->
            iconPackAdapter.setData(AppInfo::class, apps)
        }
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return HexagonalGridLayoutManager(
            if (leftHandedLayout == true) HexagonalGridLayoutManager.Corner.TOP_LEFT else HexagonalGridLayoutManager.Corner.TOP_RIGHT
        )
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
                    this.hexItem = appInfo
                    this.adapter = LauncherFragmentDatabindingAdapter
                }
                vdb.root.setOnClickListener {
                    //TODO
                }
            }
        )
    }
}