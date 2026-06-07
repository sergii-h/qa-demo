package com.example.demo.ui.i18n

import androidx.compose.material3.Text
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.locale.AppLocale
import com.example.demo.locale.LocalizedContent
import com.example.demo.testing.DemoComposeTestTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskLabelsComposableTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldShowTodoLabelWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskStatusLabel(TaskStatus.TODO))
            }
        }

        // Then
        composeTestRule.onNodeWithText("To Do").assertIsDisplayed()
    }

    @Test
    fun shouldShowInProgressLabelWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskStatusLabel(TaskStatus.IN_PROGRESS))
            }
        }

        // Then
        composeTestRule.onNodeWithText("In Progress").assertIsDisplayed()
    }

    @Test
    fun shouldShowDoneLabelWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskStatusLabel(TaskStatus.DONE))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Done").assertIsDisplayed()
    }

    @Test
    fun shouldShowLowLabelWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskPriorityLabel(TaskPriority.LOW))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Low").assertIsDisplayed()
    }

    @Test
    fun shouldShowMediumLabelWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskPriorityLabel(TaskPriority.MEDIUM))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Medium").assertIsDisplayed()
    }

    @Test
    fun shouldShowHighLabelWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskPriorityLabel(TaskPriority.HIGH))
            }
        }

        // Then
        composeTestRule.onNodeWithText("High").assertIsDisplayed()
    }

    @Test
    fun shouldShowTodoLabelInSpanishWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskStatusLabel(TaskStatus.TODO))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Por hacer").assertIsDisplayed()
    }

    @Test
    fun shouldShowInProgressLabelInSpanishWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskStatusLabel(TaskStatus.IN_PROGRESS))
            }
        }

        // Then
        composeTestRule.onNodeWithText("En progreso").assertIsDisplayed()
    }

    @Test
    fun shouldShowDoneLabelInSpanishWhenStatusComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskStatusLabel(TaskStatus.DONE))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Hecho").assertIsDisplayed()
    }

    @Test
    fun shouldShowLowLabelInSpanishWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskPriorityLabel(TaskPriority.LOW))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Baja").assertIsDisplayed()
    }

    @Test
    fun shouldShowMediumLabelInSpanishWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskPriorityLabel(TaskPriority.MEDIUM))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Media").assertIsDisplayed()
    }

    @Test
    fun shouldShowHighLabelInSpanishWhenPriorityComposableInvoked() {
        // When
        composeTestRule.setContent {
            LocalizedContent(languageTag = AppLocale.SPANISH) {
                Text(taskPriorityLabel(TaskPriority.HIGH))
            }
        }

        // Then
        composeTestRule.onNodeWithText("Alta").assertIsDisplayed()
    }
}
