package com.mrmannwood.hexlauncher.nux

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.mrmannwood.hexlauncher.launcher.LauncherActivity
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.view.makeFullScreen
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R
import java.lang.IllegalArgumentException

class NUXActivity : AppCompatActivity() {

    private val viewModel by viewModels<NUXViewModel>()

    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.preferencesLiveData.value?.let { prefs ->
            if (checkFirstRun(prefs)) {
                startLauncher()
                return
            }
        } ?: run {
            viewModel.preferencesLiveData.observe(this) { prefs ->
                if (checkFirstRun(prefs)) {
                    startLauncher()
                }
            }
        }
        setContentView(R.layout.activity_nux)
        supportActionBar?.hide()
        makeFullScreen()

        viewPager = findViewById<ViewPager2>(R.id.pager).apply {
            adapter = object : FragmentStateAdapter(this@NUXActivity) {
                override fun getItemCount(): Int = 3

                override fun createFragment(position: Int): Fragment =
                    when(position) {
                        0 -> WelcomeFragment()
                        1 -> SwipeTutorialFragment()
                        2 -> SetAsHomeFragment()
                        else -> {
                            throw IllegalArgumentException("Too many pages: $position")
                        }
                    }
            }
        }
        tabLayout = findViewById(R.id.tab_layout)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->

        }.attach()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startLauncher(finish = false)
    }

    private fun checkFirstRun(prefs: SharedPreferences) : Boolean {
        val lastRunVersion = prefs.getString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, null)
        return if (BuildConfig.VERSION_NAME == lastRunVersion) {
            true
        } else {
            prefs.edit {
                putString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, BuildConfig.VERSION_NAME)
            }
            false
        }
    }

    fun startLauncher(finish: Boolean = true) {
        startActivity(Intent(this, LauncherActivity::class.java))
        if (finish) {
            finish()
        }
    }
}