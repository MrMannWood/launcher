package com.mrmannwood.hexlauncher.appcustomize

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerDialog
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerViewModel
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.textentrydialog.TextEntryDialog
import com.mrmannwood.hexlauncher.textentrydialog.TextEntryDialogViewModel
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentAppCustomizationBinding
import com.mrmannwood.launcher.databinding.ListAppCustomizationTagBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

class AppCustomizationFragment : InstrumentedFragment() {

    companion object {
        private const val PACKAGE_NAME = "package_name"

        fun forPackage(packageName: String) : AppCustomizationFragment {
            return AppCustomizationFragment().apply {
                arguments = Bundle().apply {
                    putString(PACKAGE_NAME, packageName)
                }
            }
        }
    }

    override val nameForInstrumentation = "AppCustomizationFragment"

    private lateinit var binding: FragmentAppCustomizationBinding
    private lateinit var packageName: String
    private var appInfo: AppInfo? = null
    private lateinit var tagsAdapter: Adapter<SearchTerm>

    private lateinit var  viewModel : AppCustomizationViewModel
    private val colorPickerViewModel : ColorPickerViewModel by activityViewModels()
    private val textEntryDialogViewModel : TextEntryDialogViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        packageName = requireArguments().getString(PACKAGE_NAME, null)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_app_customization, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this, AppCustomizationViewModelFactory(requireContext(), packageName)).get(AppCustomizationViewModel::class.java)

        binding.resources = resources
        binding.adapter = CustomizationFragmentDatabindingAdapter

        viewModel.app.observe(viewLifecycleOwner) { app ->
            app?.let { info ->
                binding.appInfo = info
                appInfo = info
                tagsAdapter.setData(SearchTerm.Category::class, app.categories.map { SearchTerm.Category(it) })
                tagsAdapter.setData(SearchTerm.Tag::class, app.tags.map { SearchTerm.Tag(it) })
            } ?: run {
                parentFragmentManager.beginTransaction()
                    .remove(this@AppCustomizationFragment)
                    .commit()
            }
        }

        binding.iconBgcLayout.setOnClickListener {
            if (appInfo == null) {
                return@setOnClickListener
            }

            colorPickerViewModel.colorLiveData.value = appInfo!!.backgroundColor
            colorPickerViewModel.completionLiveData.value = false

            val colorObserver = Observer<Int> { color ->
                binding.iconBgcOverride = color
            }
            colorPickerViewModel.colorLiveData.observe(viewLifecycleOwner, colorObserver)
            colorPickerViewModel.completionLiveData.observe(viewLifecycleOwner, object : Observer<Boolean> {
                override fun onChanged(complete: Boolean) {
                    if (complete) {
                        colorPickerViewModel.colorLiveData.removeObserver(colorObserver)
                        colorPickerViewModel.completionLiveData.removeObserver(this)

                        binding.iconBgcOverride?.let { bgc ->
                            updateAppInfo { dao ->
                                dao.setColorOverride(appInfo!!.packageName, bgc)
                            }
                        }
                    }
                }
            })
            colorPickerViewModel.cancellationLiveData.observe(viewLifecycleOwner,
                { canceled ->
                    if (canceled) {
                        binding.iconBgcOverride = null
                    }
                })
            ColorPickerDialog().show(childFragmentManager, null)
        }

        binding.buttonHideApp.setOnClickListener {
            appInfo?.let { app ->
                updateAppInfo { dao ->
                    dao.setHidden(app.packageName, !app.hidden)
                }
            }
        }

        binding.iconBackgroundLayout.setOnClickListener {
            appInfo?.let { app ->
                updateAppInfo { dao ->
                    dao.setBackgroundHidden(app.packageName, !app.backgroundHidden)
                }
            }
        }

        textEntryDialogViewModel.completionLiveData.observe(viewLifecycleOwner) { text ->
            text?.split(" ")?.forEach { t ->
                t.trim().takeIf { it.isNotBlank() }?.lowercase()?.let { tag ->
                    if (tag.contains(",")) {
                        Toast.makeText(requireContext(), R.string.text_entry_cannot_contain_comma, Toast.LENGTH_LONG).show()
                    } else if (appInfo?.searchTerms?.contains(tag) != true) {
                        updateAppInfo { dao ->
                            dao.setTags(
                                appInfo!!.packageName,
                                ArrayList<String>().also {
                                    it.addAll(appInfo!!.tags)
                                    it.add(tag)
                                }.joinToString(",") { it }
                            )
                        }
                    }
                    textEntryDialogViewModel.completionLiveData.value = null
                }
            }
        }

        binding.buttonAddTag.setOnClickListener {
            TextEntryDialog().show(childFragmentManager, null)
        }

        binding.tags.layoutManager = LinearLayoutManager(requireContext())
        tagsAdapter = Adapter(
            context = requireContext(),
            order = arrayOf(SearchTerm.Category::class, SearchTerm.Tag::class),
            idFunc = { appInfo?.categories?.indexOf(it.term)?.toLong() ?: -1L },
            viewFunc = { R.layout.list_app_customization_tag },
            bindFunc = {  vdb, tag ->
                when(vdb) {
                    is ListAppCustomizationTagBinding -> {
                        vdb.tag = tag.term
                        vdb.canDelete = tag is SearchTerm.Tag
                        vdb.buttonTagDelete.setOnClickListener {
                            Timber.e("DELETE ${tag.term} FROM ${appInfo!!.packageName}")
                            updateAppInfo { dao ->
                                dao.setTags(
                                    appInfo!!.packageName,
                                    ArrayList<String>().also {
                                        it.addAll(appInfo!!.tags)
                                        it.remove(tag.term)
                                    }.joinToString(",") { it }
                                )
                            }
                        }
                    }
                }
            }
        )
        binding.tags.adapter = tagsAdapter
    }

    fun updateAppInfo(action: (dao: AppDataDao) -> Unit) {
        viewLifecycleOwner.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                action(DB.get().appDataDao())
            }
        }
    }

    sealed class SearchTerm(val term: String) {
        class Category(term: String): SearchTerm(term)
        class Tag(term: String): SearchTerm(term)
    }
}