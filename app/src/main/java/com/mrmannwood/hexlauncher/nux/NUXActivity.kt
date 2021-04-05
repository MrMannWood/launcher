package com.mrmannwood.hexlauncher.nux

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrmannwood.hexlauncher.view.makeFullScreen
import com.mrmannwood.launcher.R

class NUXActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nux)
        supportActionBar?.hide()
        makeFullScreen()

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            adapter = object : FragmentStateAdapter(this@NUXActivity) {

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
        tabLayout = findViewById(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { _, _ -> }.attach()
    }

    override fun onBackPressed() {
        /* consume */
    }
}