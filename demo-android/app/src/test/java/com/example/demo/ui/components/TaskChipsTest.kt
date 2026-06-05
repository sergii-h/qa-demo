package com.example.demo.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.ui.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskChipsTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayTodoStatusChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                StatusChip(status = TaskStatus.TODO)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO)).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayInProgressStatusChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                StatusChip(status = TaskStatus.IN_PROGRESS)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.IN_PROGRESS)).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayDoneStatusChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                StatusChip(status = TaskStatus.DONE)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.DONE)).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayLowPriorityChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                PriorityChip(priority = TaskPriority.LOW)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.LOW)).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayMediumPriorityChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                PriorityChip(priority = TaskPriority.MEDIUM)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.MEDIUM)).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayHighPriorityChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                PriorityChip(priority = TaskPriority.HIGH)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.HIGH)).assertIsDisplayed()
    }
}
