package com.mrmannwood.hexlauncher.nux

import android.app.Activity
import android.content.Context
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.WindowInsets
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mrmannwood.hexlauncher.executors.InlineExecutor
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.HexItem
import com.mrmannwood.hexlauncher.launcher.LauncherFragmentDatabindingAdapter
import com.mrmannwood.hexlauncher.launcher.Provider
import com.mrmannwood.hexlauncher.settings.PreferenceExtractor
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.hexlauncher.settings.PreferencesRepository.watchPref
import com.mrmannwood.hexlauncher.view.HexagonalGridLayoutManager
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SearchTutorialFragment : Fragment(R.layout.fragment_nux_search_tutorial) {

    private lateinit var leftHandedSwitch: SwitchMaterial
    private lateinit var message: TextView
    private lateinit var searchView: KeyboardEditText
    private lateinit var resultListView: RecyclerView
    private lateinit var resultListAdapter: Adapter<TutorialHexItem>
    private var showKeyboardJob: Job? = null
    private var leftHandedLayout: Boolean = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        message = view.findViewById(R.id.nux_search_message)
        resultListAdapter = createResultAdapter(view.context)
        resultListView = view.findViewById<RecyclerView>(R.id.result_list).apply {
            layoutManager = createLayoutManager()
            adapter = resultListAdapter
        }

        val exampleApps = listOf(
            TutorialHexItem(view.context.applicationContext, 1),
            TutorialHexItem(view.context.applicationContext, 2),
            TutorialHexItem(view.context.applicationContext, 3),
            TutorialHexItem(view.context.applicationContext, 4),
            TutorialHexItem(view.context.applicationContext, 5),
            TutorialHexItem(view.context.applicationContext, 6),
        )

        searchView = view.findViewById(R.id.search)
        searchView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
            override fun afterTextChanged(p0: Editable?) {
                if (searchView.text.toString().isEmpty()) {
                    message.setText(R.string.nux_search_message)
                    resultListAdapter.setData(TutorialHexItem::class, emptyList())
                } else {
                    message.setText(R.string.nux_search_message_2)
                    resultListAdapter.setData(TutorialHexItem::class, exampleApps)
                }
            }
        })

        viewLifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                showKeyboardJob = viewLifecycleOwner.lifecycleScope.launch {
                    activity?.let { forceShowKeyboard(it, searchView) }
                }
            }
            override fun onPause(owner: LifecycleOwner) {
                viewLifecycleOwner.lifecycleScope.launch {
                    showKeyboardJob?.cancelAndJoin()
                    activity?.let { hideKeyboard(it, searchView) }
                }
            }
        })

        leftHandedSwitch = view.findViewById(R.id.nux_search_left_switch)
        watchPref(view.context, PreferenceKeys.User.LEFT_HANDED, PreferenceExtractor.BooleanExtractor)
            .observe(viewLifecycleOwner) { leftHanded ->
                if (leftHanded == null) return@observe
                if (leftHandedLayout == leftHanded) return@observe
                leftHandedLayout = leftHanded
                resultListView.layoutManager = createLayoutManager()
                leftHandedSwitch.isChecked = leftHanded
            }

        leftHandedSwitch.setOnCheckedChangeListener { _, checked ->
            val context = context ?: return@setOnCheckedChangeListener
            PreferencesRepository.getPrefs(context) {
                it.edit {
                    putBoolean(PreferenceKeys.User.LEFT_HANDED, checked)
                }
            }
        }
    }

    class TutorialHexItem(context: Context, index: Int) : HexItem {
        val color = ContextCompat.getColor(
            context,
            if (index == 1 || index == 4 || index == 6) {
                R.color.colorOnPrimary
            } else {
                R.color.colorOnSecondary
            }
        )
        override val label: String = "Example App $index"
        override val icon: Provider<Drawable?> = Provider({ ColorDrawable(color) }, InlineExecutor)
        override val hidden: Boolean = false
        override val backgroundColor: Int = color
        override val backgroundHidden: Boolean = false
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return HexagonalGridLayoutManager(
            if (leftHandedLayout) HexagonalGridLayoutManager.Corner.BOTTOM_LEFT else HexagonalGridLayoutManager.Corner.BOTTOM_RIGHT
        )
    }

    private fun createResultAdapter(context: Context): Adapter<TutorialHexItem> {
        val idGenerator = Adapter.IdGenerator(listOf(TutorialHexItem::class to { it.label }))
        return Adapter(
            context = context,
            order = arrayOf(TutorialHexItem::class),
            idFunc = idGenerator::genId,
            viewFunc = { R.layout.list_app_item },
            bindFunc = { vdb, hexItem ->
                (vdb as ListAppItemBinding).apply {
                    this.hexItem = hexItem
                    this.adapter = LauncherFragmentDatabindingAdapter
                }
                vdb.root.setOnCreateContextMenuListener { menu, _, _ ->
                    menu.setHeaderTitle(hexItem.label)
                    menu.add(R.string.nux_search_app_customize).setOnMenuItemClickListener {
                        (parentFragment as NUXHostFragment).next()
                        true
                    }
                    menu.add(R.string.nux_search_app_details).setOnMenuItemClickListener {
                        (parentFragment as NUXHostFragment).next()
                        true
                    }
                }
            }
        )
    }

    private suspend fun forceShowKeyboard(activity: Activity, view: EditText) {
        withContext(Dispatchers.Main) {
            view.requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val rootView = activity.window.decorView
                while (lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    rootView.windowInsetsController!!.show(WindowInsets.Type.ime())
                    if (rootView.rootWindowInsets?.isVisible(WindowInsets.Type.ime()) == true) {
                        break
                    }
                    delay(20)
                }
            } else {
                val imm =
                    view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
            }
        }
    }

    private suspend fun hideKeyboard(activity: Activity, view: EditText) {
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = activity.window.decorView.windowInsetsController
                controller?.hide(WindowInsets.Type.ime())
            } else {
                val windowToken = activity.currentFocus?.windowToken ?: view.windowToken
                (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    windowToken,
                    0
                )
            }
        }
    }
}
