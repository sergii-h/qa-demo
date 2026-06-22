package com.example.demo.integration

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
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
class LanguageIntegrationTest : IntegrationTestBase() {

    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUpLocale() {
        context.getSharedPreferences("demo_locale", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        AppLocale.setLanguage(context, AppLocale.ENGLISH)
    }

    @Test
    fun shouldRenderLanguageSwitcherWhenListShown() {
        // Given
        mockServer.enqueueGetTasks()

        // When
        launchApp()

        // Then
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_EN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_ES).assertIsDisplayed()
    }

    @Test
    fun shouldShowSpanishListTitleWhenEsSelected() {
        // Given
        mockServer.enqueueGetTasksForLanguageSwitch()
        launchApp()

        // When
        switchLanguage(LanguageOption.ES)

        // Then
        composeTestRule.onNodeWithTag(TestTags.PAGE_TITLE).assertTextEquals("Tareas")
    }

    @Test
    fun shouldChangeLanguageWhenUserSelectsAnotherLanguageOption() {
        // Given
        mockServer.enqueueGetTasksForLanguageSwitch()
        launchApp()
        switchLanguage(LanguageOption.ES)
        composeTestRule.onNodeWithTag(TestTags.PAGE_TITLE).assertTextEquals("Tareas")

        // When
        mockServer.enqueueGetTasks()
        switchLanguage(LanguageOption.EN)

        // Then
        composeTestRule.onNodeWithTag(TestTags.PAGE_TITLE).assertTextEquals("Tasks")
    }
}
