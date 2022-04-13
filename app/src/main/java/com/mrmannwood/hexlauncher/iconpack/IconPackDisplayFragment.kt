package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.Result
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mrmannwood.hexlauncher.launcher.*
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding

class IconPackDisplayFragment : Fragment(R.layout.fragment_icon_pack_display) {

    companion object {
        private const val KEY_PACKAGE_NAME = "package_name"

        fun newInstance(packageName: String): IconPackDisplayFragment {
            return IconPackDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PACKAGE_NAME, packageName)
                }
            }
        }
    }

    private val viewModel: IconPackDisplayViewModel by viewModels {
        IconPackDisplayViewModelFactory(requireContext().applicationContext, packageName)
    }

    private lateinit var packageName: String
    private lateinit var iconView: RecyclerView
    private lateinit var iconAdapter: Adapter<IconPackHexItem>

    private var installedApps: List<AppInfo>? = null
    private var iconInfo: Map<String, IconPackIconInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageName = arguments?.getString(KEY_PACKAGE_NAME) ?: ""
        if (packageName.isEmpty()) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (packageName.isEmpty()) return

        iconAdapter = createResultAdapter(view.context)

        iconView = view.findViewById(R.id.icon_pack_display)
        iconView.layoutManager = GridLayoutManager(view.context, 6)
        iconView.adapter = iconAdapter

        viewModel.installedApps.observe(viewLifecycleOwner) { apps ->
            installedApps = apps
            maybeShowIcons()
        }
        viewModel.iconPackLiveData.observe(viewLifecycleOwner) { result ->
            result.onFailure { println("02_MARSHALL:: Failed to open icon pack") }
            result.onSuccess {
                iconInfo = it.associateBy { iconInfo -> iconInfo.component.packageName ?: "" }
                maybeShowIcons()
            }
        }
    }

    private fun maybeShowIcons() {
        val apps = installedApps
        val icons = iconInfo
        val context = context
        if (context == null || icons == null || apps == null) return
        iconAdapter.setData(
            IconPackHexItem::class,
            apps.map { makeHexItem(context, it, icons[it.packageName]) }
        )
    }

    private fun makeHexItem(context: Context, appInfo: AppInfo, iconInfo: IconPackIconInfo?): IconPackHexItem {
        return IconPackHexItem(
            label = appInfo.label,
            icon = iconInfo?.drawableProvider ?: appInfo.icon,
            hidden = appInfo.hidden,
            backgroundColor = if (iconInfo != null)
                ContextCompat.getColor(context, R.color.colorOnSecondary)else appInfo.backgroundColor,
            backgroundHidden = if (iconInfo != null) false else appInfo.backgroundHidden
        )
    }

    private fun createResultAdapter(context: Context): Adapter<IconPackHexItem> {
        val idGenerator = Adapter.IdGenerator(listOf(IconPackHexItem::class to { it.label }))
        return Adapter(
            context = context,
            order = arrayOf(IconPackHexItem::class),
            idFunc = idGenerator::genId,
            viewFunc = { R.layout.list_app_item },
            bindFunc = { vdb, hexItem ->
                (vdb as ListAppItemBinding).apply {
                    this.hexItem = hexItem
                    this.adapter = LauncherFragmentDatabindingAdapter
                }
                vdb.root.setOnClickListener {
                    MaterialAlertDialogBuilder(vdb.root.context)
                        .setMessage(hexItem.label)
                        .show()
                }
            }
        )
    }

    private class IconPackHexItem(
        override val label: String,
        override val icon: Provider<Drawable?>,
        override val hidden: Boolean,
        override val backgroundColor: Int,
        override val backgroundHidden: Boolean
    ): HexItem
}