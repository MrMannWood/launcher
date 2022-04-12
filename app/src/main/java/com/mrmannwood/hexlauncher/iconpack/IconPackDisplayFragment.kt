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
        private const val KEY_SHOW_ALL_ICONS = "show_all_icons"

        fun newInstance(packageName: String, showAllIcons: Boolean): IconPackDisplayFragment {
            return IconPackDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(KEY_PACKAGE_NAME, packageName)
                    putBoolean(KEY_SHOW_ALL_ICONS, showAllIcons)
                }
            }
        }
    }

    private val viewModel: IconPackDisplayViewModel by viewModels {
        IconPackDisplayViewModelFactory(requireContext().applicationContext, packageName)
    }

    private var showAllIcons: Boolean = false
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
        showAllIcons = arguments?.getBoolean(KEY_SHOW_ALL_ICONS) ?: false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (packageName.isEmpty()) return

        iconAdapter = createResultAdapter(view.context)

        iconView = view.findViewById(R.id.icon_pack_display)
        iconView.layoutManager = GridLayoutManager(view.context, 4)
        iconView.adapter = iconAdapter

        if (!showAllIcons) {
            viewModel.installedApps.observe(viewLifecycleOwner) { apps ->
                installedApps = apps
                maybeShowIcons()
            }
        }
        viewModel.iconPackLiveData.observe(viewLifecycleOwner) { result ->
            result.onFailure { println("02_MARSHALL:: Failed to open icon pack") }
            result.onSuccess {
                iconInfo = it.associateBy { iconInfo -> iconInfo.component.packageName }
                maybeShowIcons()
            }
        }
    }

    private fun maybeShowIcons() {
        val apps = installedApps
        val icons = iconInfo
        val context = context
        if (context == null || icons == null) {
            println("02_MARSHALL::icons are null")
            return
        }

        if (showAllIcons) {
            println("02_MARSHALL::show all")
            iconAdapter.setData(IconPackHexItem::class, icons.values.map { makeHexItem(context, it) })
        } else {
            println("02_MARSHALL::show some")
            if (apps == null) {
                println("02_MARSHALL::apps are null")
                return
            }
            icons.keys.forEach {
                println("02_MARSHALL::$it")
            }
            iconAdapter.setData(
                IconPackHexItem::class,
                apps.mapNotNull { icons[it.packageName] }.map { makeHexItem(context, it) }.onEach {
                    println("02_MARSHALL::found")
                }
            )
        }
    }

    private fun makeHexItem(context: Context, iconInfo: IconPackIconInfo): IconPackHexItem {
        return IconPackHexItem(context, iconInfo)
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
                        .setTitle(hexItem.iconInfo.ownerAppName)
                        .setMessage(hexItem.label)
                        .show()
                }
            }
        )
    }

    private class IconPackHexItem(context: Context, val iconInfo: IconPackIconInfo): HexItem {
        override val label: String = iconInfo.component.toString()
        override val icon: Provider<Drawable?> = iconInfo.drawableProvider
        override val hidden: Boolean = false
        override val backgroundColor: Int = ContextCompat.getColor(context, R.color.colorOnSecondary)
        override val backgroundHidden: Boolean = false
    }
}