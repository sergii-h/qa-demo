package com.example.demo.integration

import android.content.Context
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.locale.AppLocale
import com.example.demo.ui.TestTags
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class LanguageSelectionIntegrationTests : IntegrationTestBase() {

    private val appContext = RuntimeEnvironment.getApplication()

    @Before
    fun setUpLocale() {
        appContext.getSharedPreferences("demo_locale", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        AppLocale.setLanguage(appContext, AppLocale.ENGLISH)
    }

    @Test
    fun shouldRenderLanguageSwitcherWithEnAndEsOptions() {
        // Given
        mockServer.enqueueGetTasks()
        launchApp()

        // Then
        assertIsDisplayed(TestTags.LANGUAGE_SWITCHER)

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        }

        // Then
        assertIsDisplayed(TestTags.LANGUAGE_OPTION_EN)
        assertIsDisplayed(TestTags.LANGUAGE_OPTION_ES)
    }

    @Test
    fun shouldChangeCurrentLanguageWhenUserSelectsAnotherLanguageOption() {
        // Given
        mockServer.enqueueGetTasksForLanguageSwitch()
        launchApp()

        switchLanguage(LanguageOption.ES)
        assertTextEquals(TestTags.PAGE_TITLE, "Tareas")

        // When
        mockServer.enqueueGetTasks()
        switchLanguage(LanguageOption.EN)

        // Then
        assertTextEquals(TestTags.PAGE_TITLE, "Tasks")
    }
}
