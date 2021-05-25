package com.mrmannwood.hexlauncher.launcher

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.applist.AppListFragment
import com.mrmannwood.hexlauncher.applist.searchApps
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentHexAppListBinding
import java.util.*

class HexAppListFragment : Fragment(), HandleBackPressed {

    private lateinit var searchView: KeyboardEditText
    private lateinit var databinder : FragmentHexAppListBinding
    private val viewModel : LauncherViewModel by activityViewModels()

    private val appBindings = mutableListOf<Pair<View, (AppInfo?) -> Unit>>()
    private var apps : List<AppInfo>? = null

    private fun getAppListHost() : AppListFragment.Host<*> {
        return (requireActivity() as AppListFragment.AppListHostActivity).getAppListHost()
    }

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
        getAppListHost().setOnEnd {
            hideKeyboard(requireActivity())
        }
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
    }

    override fun onStart() {
        super.onStart()
        forceShowKeyboard(searchView)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard(requireActivity())
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.apps.observe(viewLifecycleOwner, { result ->
            result.onSuccess { appList ->
                apps = appList
                performSearch()
            }
            result.onFailure {
                Toast.makeText(requireContext(), R.string.error_app_load, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            performSearch()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }

    private fun forceShowKeyboard(view: EditText) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
    }

    private fun hideKeyboard(activity: Activity) {
        val windowToken = activity.currentFocus?.windowToken ?: searchView.windowToken
        (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun performSearch() {
        val search = searchView.text.toString().trim().lowercase(Locale.ROOT)
        val filteredApps = searchApps(apps, search, 8)
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