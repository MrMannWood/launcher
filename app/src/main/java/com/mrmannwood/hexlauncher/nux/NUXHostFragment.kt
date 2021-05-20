package com.mrmannwood.hexlauncher.nux

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrmannwood.hexlauncher.HandleBackPressed
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
                        parentFragmentManager.beginTransaction()
                            .remove(this@NUXHostFragment)
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
                    { SwipeTutorialFragment() },
                    { SettingsTutorialFragment() },
                    { SetAsHomeFragment() }
                )

                override fun getItemCount(): Int = fragments.size

                override fun createFragment(position: Int): Fragment = fragments[position]()
            }
        }
        tabLayout = view.findViewById(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }

    override fun handleBackPressed(): Boolean = true /* consume */
}