package com.mrmannwood.hexlauncher.iconpack

import com.mrmannwood.hexlauncher.Result
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mrmannwood.launcher.R

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
        viewModel.iconPackLiveData.observe(viewLifecycleOwner) { result ->
            result.onFailure { println("02_MARSHALL:: Failed to open icon pack") }
            result.onSuccess { println("02_MARSHALL:: Opened icon pack. Found ${it.size} drawables") }
        }
    }
}