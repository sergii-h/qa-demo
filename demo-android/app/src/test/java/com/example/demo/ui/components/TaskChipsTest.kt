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
import org.robolectric.ParameterizedRobolectricTestRunner

@RunWith(ParameterizedRobolectricTestRunner::class)
class StatusChipDisplayTest(
    private val status: TaskStatus
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayStatusChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                StatusChip(status = status)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.statusTag(status)).assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun statuses(): List<TaskStatus> = TaskStatus.entries
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class PriorityChipDisplayTest(
    private val priority: TaskPriority
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun shouldDisplayPriorityChipWhenRendered() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                PriorityChip(priority = priority)
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.priorityTag(priority)).assertIsDisplayed()
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun priorities(): List<TaskPriority> = TaskPriority.entries
    }
}
