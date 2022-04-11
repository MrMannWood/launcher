package com.mrmannwood.hexlauncher.iconpack

import android.content.Context
import android.graphics.drawable.Drawable
import com.mrmannwood.hexlauncher.Result
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.launcher.*
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding

class IconPackDisplayFragment : Fragment(R.layout.fragment_icon_pack_display) {

    companion object {
        private const val PACKAGE_NAME_KEY = "package_name"

        fun newInstance(packageName: String): IconPackDisplayFragment {
            return IconPackDisplayFragment().apply {
                arguments = Bundle().apply {
                    putString(PACKAGE_NAME_KEY, packageName)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageName = arguments?.getString(PACKAGE_NAME_KEY) ?: ""
        if (packageName.isEmpty()) {
            parentFragmentManager.popBackStack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (packageName.isEmpty()) return

        iconAdapter = createResultAdapter(view.context)

        iconView = view.findViewById(R.id.icon_pack_display)
        iconView.layoutManager = LinearLayoutManager(view.context)
        iconView.adapter = iconAdapter

        viewModel.iconPackLiveData.observe(viewLifecycleOwner) { result ->
            result.onFailure { println("02_MARSHALL:: Failed to open icon pack") }
            result.onSuccess {
                println("02_MARSHALL:: Got Icons")
                val context = getView()?.context ?: return@onSuccess
                println("02_MARSHALL:: Have context")
                iconAdapter.setData(IconPackHexItem::class, it.mapIndexed { idx, drawable -> makeHexItem(context, idx, drawable) })
            }
        }
    }

    private fun makeHexItem(context: Context, index: Int, drawable: Drawable): IconPackHexItem {
        return IconPackHexItem(context, index, drawable)
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
                    // TODO
                }
            }
        )
    }

    private class IconPackHexItem(
        context: Context, index: Int, drawable: Drawable
    ): HexItem {
        override val label: String = index.toString()
        override val icon: Provider<Drawable> = Provider(init = { drawable })
        override val hidden: Boolean = false
        override val backgroundColor: Int = ContextCompat.getColor(context, R.color.colorOnPrimary)
        override val backgroundHidden: Boolean = false
    }
}