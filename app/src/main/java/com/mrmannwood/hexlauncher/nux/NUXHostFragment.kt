package com.mrmannwood.hexlauncher.nux

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrmannwood.hexlauncher.HandleBackPressed
import com.mrmannwood.hexlauncher.home.HomeFragment
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R

class NUXHostFragment : Fragment(R.layout.fragment_nux_host), HandleBackPressed {

    interface NuxCompleted {
        fun onNuxCompleted()
    }

    interface AcceptsNuxCompleted {
        fun acceptNuxCompleted(nuxCompleted: NuxCompleted)
    }

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            if (fragment is AcceptsNuxCompleted) {
                fragment.acceptNuxCompleted(object : NuxCompleted {
                    override fun onNuxCompleted() {
                        PreferencesRepository.getPrefs(requireContext()) { repo ->
                            repo.dao.putString(
                                PreferenceKeys.Version.LAST_RUN_VERSION_NAME,
                                BuildConfig.VERSION_NAME
                            )
                        }
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.container, HomeFragment())
                            .commit()
                    }
                })
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewPager = view.findViewById<ViewPager2>(R.id.pager).apply {
            adapter = object : FragmentStateAdapter(this@NUXHostFragment) {

                private val fragments = listOf(
                    { WelcomeFragment() },
                    { TouchTutorialFragment() },
                    { SearchTutorialFragment() },
                    { SetAsHomeFragment() }
                )

                override fun getItemCount(): Int = fragments.size

                override fun createFragment(position: Int): Fragment = fragments[position]()
            }
        }
        viewPager.isUserInputEnabled = false
        tabLayout = view.findViewById(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, _ -> tab.view.isClickable = false }.attach()
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (position == 2) {
                    tabLayout.visibility = View.GONE
                } else {
                    tabLayout.visibility = View.VISIBLE
                }
                super.onPageSelected(position)
            }
        })
    }

    override fun handleBackPressed(): Boolean = true /* consume */

    fun next() {
        viewPager.currentItem = viewPager.currentItem + 1
    }
}
