package com.mrmannwood.hexlauncher.appcustomize

import android.content.ComponentName
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import com.mrmannwood.hexlauncher.DB
import com.mrmannwood.hexlauncher.applist.AppDataDao
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerDialog
import com.mrmannwood.hexlauncher.colorpicker.ColorPickerViewModel
import com.mrmannwood.hexlauncher.executors.cpuBoundTaskExecutor
import com.mrmannwood.hexlauncher.executors.diskExecutor
import com.mrmannwood.hexlauncher.fragment.InstrumentedFragment
import com.mrmannwood.hexlauncher.icon.IconAdapter
import com.mrmannwood.hexlauncher.launcher.Adapter
import com.mrmannwood.hexlauncher.launcher.AppInfo
import com.mrmannwood.hexlauncher.launcher.Provider
import com.mrmannwood.hexlauncher.textentrydialog.TextEntryDialog
import com.mrmannwood.hexlauncher.textentrydialog.TextEntryDialogViewModel
import com.mrmannwood.launcher.R
import com.mrmannwood.launcher.databinding.FragmentAppCustomizationBinding
import com.mrmannwood.launcher.databinding.ListAppCustomizationTagBinding

class AppCustomizationFragment : InstrumentedFragment() {

    companion object {
        private const val COMPONENT_NAME = "component_name"

        fun forComponent(componentName: ComponentName): AppCustomizationFragment {
            return AppCustomizationFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(COMPONENT_NAME, componentName)
                }
            }
        }
    }

    override val nameForInstrumentation = "AppCustomizationFragment"

    private lateinit var binding: FragmentAppCustomizationBinding
    private lateinit var componentName: ComponentName
    private var appInfo: AppInfo? = null
    private lateinit var tagsAdapter: Adapter<SearchTerm>

    private val viewModel: AppCustomizationViewModel by viewModels {
        AppCustomizationViewModelFactory(
            requireContext(),
            componentName
        )
    }
    private val colorPickerViewModel: ColorPickerViewModel by activityViewModels()
    private val textEntryDialogViewModel: TextEntryDialogViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        componentName = requireArguments().getParcelable(COMPONENT_NAME)!!
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_app_customization, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.resources = resources
        binding.adapter = CustomizationFragmentDatabindingAdapter

        viewModel.app.observe(viewLifecycleOwner) { app ->
            app?.let { info ->
                binding.appInfo = info
                appInfo = info
                tagsAdapter.setData(
                    SearchTerm.Category::class,
                    app.categories.map { SearchTerm.Category(it) })
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
            colorPickerViewModel.completionLiveData.observe(
                viewLifecycleOwner,
                object : Observer<Boolean> {
                    override fun onChanged(complete: Boolean) {
                        if (complete) {
                            colorPickerViewModel.colorLiveData.removeObserver(colorObserver)
                            colorPickerViewModel.completionLiveData.removeObserver(this)

                            binding.iconBgcOverride?.let { bgc ->
                                updateAppInfo { dao ->
                                    dao.setColorOverride(appInfo!!.componentName, bgc)
                                }
                            }
                        }
                    }
                }
            )
            colorPickerViewModel.cancellationLiveData.observe(viewLifecycleOwner) { canceled ->
                if (canceled) {
                    binding.iconBgcOverride = null
                }
            }
            ColorPickerDialog().show(childFragmentManager, null)

            colorPickerViewModel.colorSuggestionLiveData.value = null
            extractColorsForPicker(appInfo!!.icon) { iconAdapter, icon ->
                listOf(iconAdapter.getBackgroundColor(icon))
            }
            extractColorsForPicker(appInfo!!.icon) { iconAdapter, icon ->
                iconAdapter.getPalette(
                    icon = icon,
                    onPalette = { palette: Palette ->
                        addColorsToColorPickerSuggestions(
                            listOfNotNull(
                                palette.dominantSwatch,
                                palette.darkVibrantSwatch,
                                palette.lightVibrantSwatch,
                                palette.vibrantSwatch,
                                palette.darkMutedSwatch,
                                palette.lightMutedSwatch,
                                palette.mutedSwatch,
                            )
                                .filter { it.population > 25 }
                                .map { it.rgb }
                                .distinct()
                        )
                    }
                )
                emptyList()
            }
        }

        binding.buttonHideApp.setOnClickListener {
            appInfo?.let { app ->
                updateAppInfo { dao ->
                    dao.setHidden(app.componentName, !app.hidden)
                }
            }
        }

        binding.iconBackgroundLayout.setOnClickListener {
            appInfo?.let { app ->
                updateAppInfo { dao ->
                    dao.setBackgroundHidden(app.componentName, !app.backgroundHidden)
                }
            }
        }

        textEntryDialogViewModel.completionLiveData.observe(viewLifecycleOwner) { text ->
            text?.split(" ")?.forEach { t ->
                t.trim().takeIf { it.isNotBlank() }?.lowercase()?.let { tag ->
                    if (tag.contains(",")) {
                        context?.let { context ->
                            Toast.makeText(
                                context,
                                R.string.text_entry_cannot_contain_comma,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    } else if (appInfo?.searchTerms?.contains(tag) != true) {
                        updateAppInfo { dao ->
                            dao.setTags(
                                appInfo!!.componentName,
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

        binding.tags.layoutManager = LinearLayoutManager(view.context)
        tagsAdapter = Adapter(
            context = view.context,
            order = arrayOf(SearchTerm.Category::class, SearchTerm.Tag::class),
            idFunc = { appInfo?.categories?.indexOf(it.term)?.toLong() ?: -1L },
            viewFunc = { R.layout.list_app_customization_tag },
            bindFunc = { vdb, tag ->
                when (vdb) {
                    is ListAppCustomizationTagBinding -> {
                        vdb.resources = resources
                        vdb.adapter = CustomizationFragmentDatabindingAdapter
                        vdb.tag = tag.term
                        vdb.canDelete = tag is SearchTerm.Tag
                        vdb.buttonTagDelete.setOnClickListener {
                            updateAppInfo { dao ->
                                dao.setTags(
                                    appInfo!!.componentName,
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

    private fun updateAppInfo(action: (dao: AppDataDao) -> Unit) {
        diskExecutor.execute {
            context?.let { context ->
                action(DB.get(context).appDataDao())
            }
        }
    }

    private fun extractColorsForPicker(
        drawableProvider: Provider<Drawable>,
        action: (iconAdapter: IconAdapter, drawable: Drawable) -> List<Int>
    ) {
        drawableProvider.get { drawable ->
            cpuBoundTaskExecutor.execute {
                addColorsToColorPickerSuggestions(action(IconAdapter.INSTANCE, drawable))
            }
        }
    }

    private fun addColorsToColorPickerSuggestions(colors: List<Int>) {
        if (colors.isEmpty()) {
            return
        }
        colorPickerViewModel.colorSuggestionLiveData.postValue(
            (colorPickerViewModel.colorSuggestionLiveData.value?.toMutableList()
                ?: ArrayList()).also {
                it.addAll(colors)
            }
        )
    }

    sealed class SearchTerm(val term: String) {
        class Category(term: String) : SearchTerm(term)
        class Tag(term: String) : SearchTerm(term)
    }
}
