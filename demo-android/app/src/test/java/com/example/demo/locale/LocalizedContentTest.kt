package com.example.demo.locale

import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.demo.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class LocalizedContentTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldProvideSpanishStringsWhenLanguageTagIsSpanish() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                val context = LocalContext.current
                Text(context.getString(R.string.status_todo))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Por hacer").assertIsDisplayed()
    }

    @Test
    fun shouldProvideEnglishStringsWhenLanguageTagIsEnglish() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.ENGLISH) {
                val context = LocalContext.current
                Text(context.getString(R.string.status_todo))
            }
        }

        // Then
        composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
    }
}
