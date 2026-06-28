package com.example.demo.locale

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
        // Given
        val tag = "content"

        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                val context = LocalContext.current
                Text(context.getString(R.string.status_todo), modifier = Modifier.testTag(tag))
            }
        }

        // Then
        composeTestRule.onNodeWithTag(tag).assertTextEquals("Por hacer")
    }

    @Test
    fun shouldProvideEnglishStringsWhenLanguageTagIsEnglish() {
        // Given
        val tag = "content"

        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.ENGLISH) {
                val context = LocalContext.current
                Text(context.getString(R.string.status_todo), modifier = Modifier.testTag(tag))
            }
        }

        // Then
        composeTestRule.onNodeWithTag(tag).assertTextEquals("To Do")
    }
}
