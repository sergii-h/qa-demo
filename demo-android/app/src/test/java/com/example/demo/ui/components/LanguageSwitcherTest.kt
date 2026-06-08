package com.example.demo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.locale.AppLocale
import com.example.demo.testing.AppLocaleRule
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(ParameterizedRobolectricTestRunner::class)
class LanguageSwitcherDisplayTest(
    private val scenario: String,
    private val languageTag: String
) {

    @get:Rule
    val appLocaleRule = AppLocaleRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        AppLocale.setLanguage(context, languageTag)
    }

    @Test
    fun shouldShowLanguageSwitcherWhenCurrentLanguageIsSet() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                LanguageSwitcher()
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun languages() = listOf(
            arrayOf("English", AppLocale.ENGLISH),
            arrayOf("Spanish", AppLocale.SPANISH)
        )
    }
}

@RunWith(RobolectricTestRunner::class)
class LanguageSwitcherTest {

    @get:Rule
    val appLocaleRule = AppLocaleRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun shouldSwitchToSpanishWhenSpanishOptionSelected() {
        // Given
        composeTestRule.setContent {
            DemoComposeTestTheme {
                LanguageSwitcher()
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_ES).performClick()

        // Then
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).assertIsDisplayed()
        assertThat(AppLocale.languageTag.value).isEqualTo(AppLocale.SPANISH)
    }

    @Test
    fun shouldSwitchToEnglishWhenEnglishOptionSelected() {
        // Given
        AppLocale.setLanguage(context, AppLocale.SPANISH)
        composeTestRule.setContent {
            DemoComposeTestTheme {
                LanguageSwitcher()
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_EN).performClick()

        // Then
        assertThat(AppLocale.languageTag.value).isEqualTo(AppLocale.ENGLISH)
    }
}
