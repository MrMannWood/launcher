package com.mrmannwood.hexlauncher.applist

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.contacts.ContactData
import com.mrmannwood.hexlauncher.launcher.*
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.ListAppItemBinding
import com.mrmannwood.hexlauncher.qwertyMistakes
import com.mrmannwood.hexlauncher.view.KeyboardEditText
import com.mrmannwood.launcher.databinding.ListItemContactBinding
import timber.log.Timber
import java.util.*

class AppListFragment : Fragment(), HandleBackPressed {

    private lateinit var searchView: KeyboardEditText
    private lateinit var appListView: RecyclerView
    private lateinit var appListAdapter: Adapter<AppInfo>

    private lateinit var contactsListView: RecyclerView
    private lateinit var contactsAdapter: Adapter<ContactData>

    private var numColumnsInAppList: Int = 0

    private val viewModel : LauncherViewModel by activityViewModels()
    private val hostViewModel : AppListHostViewModel by activityViewModels()

    private var apps : List<AppInfo>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        numColumnsInAppList = calculateNoOfColumnsForAppList()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_app_list, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        appListAdapter = createAppListAdapter()
        appListView = view.findViewById<RecyclerView>(R.id.app_list).apply {
            layoutManager = createAppListLayoutManager()
            adapter = appListAdapter
        }

        searchView = view.findViewById(R.id.search)
        searchView.handleBackPressed = object : HandleBackPressed {
            override fun handleBackPressed(): Boolean {
                hostViewModel.endRequested.value = true
                return true
            }
        }
        searchView.addTextChangedListener(createSearchTextListener())
        searchView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId != EditorInfo.IME_ACTION_SEARCH) {
                false
            } else {
                hostViewModel.searchButtonSelected.value = searchView.text.toString()
                true
            }
        }

        contactsAdapter = createContactsAdapter()
        contactsListView = view.findViewById<RecyclerView>(R.id.contact_list).apply {
            layoutManager = createContactsLayoutManager()
            adapter = contactsAdapter
        }

        startObservingLiveData()
    }

    override fun onStart() {
        super.onStart()
        forceShowKeyboard(searchView)
    }

    override fun handleBackPressed(): Boolean {
        if (TextUtils.isEmpty(searchView.text)) {
            return false
        }
        searchView.setText("")
        return true
    }

    private fun startObservingLiveData() {
        viewModel.apps.observe(viewLifecycleOwner, {
            it.getOrNull()?.let { apps ->
                this.apps = apps
                performSearch()
            }
            it.exceptionOrNull()?.let { error ->
                Timber.e(error)
                Toast.makeText(requireContext(), R.string.error_app_load, Toast.LENGTH_LONG).show()
            }
        })
        hostViewModel.supportsContactSearch.observe(viewLifecycleOwner) { supportsContactSearch ->
            if (supportsContactSearch) {
                viewModel.contacts.observe(viewLifecycleOwner, contactsObserver)
            } else {
                viewModel.contacts.removeObserver(contactsObserver)
            }
        }
        hostViewModel.supportsAppMenu
    }

    private fun createAppListLayoutManager() : GridLayoutManager {
        return object : GridLayoutManager(
                requireContext(),
                numColumnsInAppList,
                RecyclerView.VERTICAL,
                true /* reverseLayout */
        ) {
            override fun isLayoutRTL() : Boolean = true
        }
    }

    private fun createAppListAdapter(): Adapter<AppInfo> =
            Adapter(
                    context = requireContext(),
                    viewFunc = { R.layout.list_app_item },
                    bindFunc = { vdb, appData ->
                        (vdb as ListAppItemBinding).apply {
                            appInfo = appData
                            adapter = LauncherFragmentDatabindingAdapter
                        }
                        if (true == hostViewModel.supportsAppMenu.value) {
                            vdb.root.setOnCreateContextMenuListener { menu, _, _ ->
                                menu.add(R.string.menu_item_uninstall_app_title).setOnMenuItemClickListener {
                                    startActivity(Intent(Intent.ACTION_DELETE).apply {
                                        data = Uri.parse("package:${appData.packageName}")
                                    })
                                    true
                                }
                            }
                        }
                        vdb.root.setOnClickListener {
                            hostViewModel.appSelected.value = appData
                        }
                    }
            )

    private fun createContactsLayoutManager() : LinearLayoutManager =
            LinearLayoutManager(requireContext()).apply {
                reverseLayout = true
            }

    private fun createContactsAdapter(): Adapter<ContactData> =
            Adapter(
                    context = requireContext(),
                    viewFunc = { R.layout.list_item_contact },
                    bindFunc = { vdb, contactData ->
                        (vdb as ListItemContactBinding).apply {
                            contact = contactData
                        }
                        vdb.root.setOnClickListener {
                            hostViewModel.contactSelected.value = contactData
                        }
                    }
            )

    private fun createSearchTextListener() : TextWatcher = object : TextWatcher {

        override fun afterTextChanged(p0: Editable) {
            performSearch()
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) = Unit
    }

    private fun calculateNoOfColumnsForAppList(): Int {
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        return (screenWidthDp / 75 + 0.5).toInt()
    }

    private fun forceShowKeyboard(view: EditText) {
        view.requestFocus()
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private fun hideKeyboard(activity: Activity) {
        val windowToken = activity.currentFocus?.windowToken ?: searchView.windowToken
        (activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(windowToken, 0)
    }

    private fun performSearch() {
        val search = searchView.text.toString().trim().toLowerCase(Locale.ROOT)
        viewModel.contacts.setSearchTerm(search)
        apps?.let { data ->
            val filteredApps =
                    if (search.isEmpty()) {
                        Collections.emptyList()
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
                        sorted.filter { it.value != Int.MAX_VALUE }
                                .toList()
                                .sortedWith(compareBy({ it.second }, { it.first.label }))
                                .map { it.first }
                                .take(numColumnsInAppList)
                    }
            appListAdapter.setData(filteredApps)
        }
    }

    private val contactsObserver = Observer<Result<List<ContactData>>> {
        it.getOrNull()?.let { contacts ->
            contactsAdapter.setData(contacts.take(2))
        }
        it.exceptionOrNull()?.let {
            contactsAdapter.setData(listOf())
            Toast.makeText(requireContext(), R.string.error_contact_load, Toast.LENGTH_LONG).show()
        }
    }
}