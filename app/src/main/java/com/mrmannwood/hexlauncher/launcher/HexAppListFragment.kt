package com.mrmannwood.hexlauncher.launcher

import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.lifecycleScope
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.applist.searchApps
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHexAppListBinding
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.*

class HexAppListFragment : InstrumentedFragment(), HandleBackPressed {

    private lateinit var searchView: KeyboardEditText
    private lateinit var databinder : FragmentHexAppListBinding
    private val viewModel : LauncherViewModel by activityViewModels()

    private val appBindings = mutableListOf<Pair<View, (AppInfo?) -> Unit>>()
    private var apps : List<AppInfo>? = null
    private var enableCategorySearch : Boolean = true
    private var showKeyboardJob : Job? = null

    private fun getAppListHost() : AppListFragment.Host<*> {
        return (requireActivity() as AppListFragment.AppListHostActivity).getAppListHost()
    }

    override val nameForInstrumentation = "HexAppListFragment"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        databinder = DataBindingUtil.inflate(inflater, R.layout.fragment_hex_app_list, container, false)
        return databinder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        databinder.adapter = LauncherFragmentDatabindingAdapter

        searchView = view.findViewById(R.id.search)
        searchView.handleBackPressed = object : HandleBackPressed {
            override fun handleBackPressed(): Boolean {
                getAppListHost().end(null)
                return true
            }
        }
        searchView.addTextChangedListener(createSearchTextListener())
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                false
            } else {
                getAppListHost().onSearchButtonPressed(searchView.text.toString())
                true
            }
        }

        appBindings.add(Pair(databinder.app1, { app -> databinder.setApp1(app) }))
        appBindings.add(Pair(databinder.app2, { app -> databinder.setApp2(app) }))
        appBindings.add(Pair(databinder.app3, { app -> databinder.setApp3(app) }))
        appBindings.add(Pair(databinder.app4, { app -> databinder.setApp4(app) }))
        appBindings.add(Pair(databinder.app5, { app -> databinder.setApp5(app) }))
        appBindings.add(Pair(databinder.app6, { app -> databinder.setApp6(app) }))
        appBindings.add(Pair(databinder.app7, { app -> databinder.setApp7(app) }))
        appBindings.add(Pair(databinder.app8, { app -> databinder.setApp8(app) }))

        startObservingLiveData()

        viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_START)
            fun onStart() {
                showKeyboardJob = viewLifecycleOwner.lifecycleScope.launch {
                    forceShowKeyboard(searchView)
                }
            }
            @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
            fun onStop() {
                viewLifecycleOwner.lifecycleScope.launch {
                    showKeyboardJob?.cancelAndJoin()
                    hideKeyboard(requireActivity())
                }
            }
        })
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.enableCategorySearch.observe(viewLifecycleOwner, { enable ->
            enableCategorySearch = enable != false
        })
        viewModel.apps.observe(viewLifecycleOwner, { appList ->
            apps = appList
            performSearch()
        })
    }

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            performSearch()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }

    private suspend fun forceShowKeyboard(view: EditText) {
        withContext(Dispatchers.Main) {
            view.requestFocus()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val rootView = requireActivity().window.decorView
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

    private suspend fun hideKeyboard(activity: Activity) {
        withContext(Dispatchers.Main) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val controller = searchView.windowInsetsController!!
                controller.hide(WindowInsets.Type.ime())
            } else {
                val windowToken = activity.currentFocus?.windowToken ?: searchView.windowToken
                (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
                    windowToken,
                    0
                )
            }
        }
    }

    private fun performSearch() {
        val search = searchView.text.toString().trim().lowercase(Locale.ROOT)
        val filteredApps = searchApps(apps, search, enableCategorySearch, 8)
        appBindings.forEachIndexed { i, binding ->
            filteredApps.getOrNull(i)?.let { app ->
                binding.second.invoke(app)
                getAppListHost().onAppInfoBinding(binding.first, app)
                binding.first.setOnClickListener {
                    getAppListHost().onAppSelected(app)
                }
            } ?: run {
                binding.second.invoke(null)
            }
        }
    }
}