package com.mrmannwood.hexlauncher.screenshots

import androidx.core.content.edit
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.mrmannwood.hexlauncher.launcher.LauncherActivity
import com.mrmannwood.hexlauncher.settings.PreferenceKeys
import com.mrmannwood.hexlauncher.settings.PreferencesRepository
import com.mrmannwood.launcher.BuildConfig
import com.mrmannwood.launcher.R
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import tools.fastlane.screengrab.Screengrab
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy


@RunWith(AndroidJUnit4::class)
@LargeTest
class TakeScreenshotsTest {

    @get:Rule val launcherRule = ActivityScenarioRule(LauncherActivity::class.java)

    companion object {
        @BeforeClass
        fun setupClass() {
            Screengrab.setDefaultScreenshotStrategy(UiAutomatorScreenshotStrategy())
        }
    }

    @Before
    fun setup() {
        PreferencesRepository.getPrefs(getApplicationContext()) {
            it.edit { clear() }
            it.edit { putString(PreferenceKeys.Version.LAST_RUN_VERSION_NAME, BuildConfig.VERSION_NAME) }
        }
    }

    @Test
    fun searchRightHanded() {
        performSearch("red") {
            Thread.sleep(1_000)
            Screengrab.screenshot(it.length.toString())
        }
    }

    @Test
    fun searchLeftHanded() {
        PreferencesRepository.getPrefs(getApplicationContext()) {
            it.edit { putBoolean(PreferenceKeys.User.LEFT_HANDED, true) }
        }
        Thread.sleep(1_000)
        performSearch("red") {
            if (it.length == 3) {
                Thread.sleep(1_000)
                Screengrab.screenshot("4")
            }
        }
    }

    @Test
    fun settings() {
        onView(isRoot()).perform(longClick())
        Thread.sleep(1_000)
        onView(withText(R.string.menu_item_home_settings)).perform(click())
        Thread.sleep(1_000)
        Screengrab.screenshot("4")
    }

    @Test
    fun customize() {
        onView(isRoot()).perform(swipeUp())
        Thread.sleep(1_000)
        onView(withId(R.id.search)).perform(click(), replaceText("reddit"))
        Thread.sleep(1_000)
        onView(withId(R.id.result_list)).perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(0, longClick()))
        Thread.sleep(1_000)
        onView(withText(R.string.menu_item_app_customize)).perform(click())
        Thread.sleep(1_000)
        Screengrab.screenshot("5")
    }

    private fun performSearch(term: String, onLetterEntered: (String) -> Unit) {
        onView(isRoot()).perform(swipeUp())
        Thread.sleep(1_000)

        val sb = StringBuilder()
        term.forEach { char ->
            sb.append(char)
            onView(withId(R.id.search)).perform(click(), replaceText(sb.toString()))
            onLetterEntered(sb.toString())
        }
    }
}