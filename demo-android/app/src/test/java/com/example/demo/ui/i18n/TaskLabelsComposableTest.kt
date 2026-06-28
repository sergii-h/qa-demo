package com.example.demo.ui.i18n

import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.locale.AppLocale
import com.example.demo.testing.AppLocaleTestSupport
import com.example.demo.testing.DemoComposeTestTheme
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

private const val LABEL_TAG = "label"

@RunWith(ParameterizedRobolectricTestRunner::class)
class TaskStatusLabelComposableTest(
    private val languageTag: String,
    private val status: TaskStatus,
    private val expectedLabel: String,
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUpLocale() {
        AppLocale.setLanguage(RuntimeEnvironment.getApplication(), languageTag)
    }

    @After
    fun tearDownLocale() {
        AppLocaleTestSupport.resetToEnglish()
    }

    @Test
    fun shouldRenderStatusLabelWhenComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskStatusLabel(status), modifier = Modifier.testTag(LABEL_TAG))
            }
        }

        // Then
        composeTestRule.onNodeWithTag(LABEL_TAG).assertTextEquals(expectedLabel)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{1} [{0}]")
        fun parameters() = listOf(
            arrayOf(AppLocale.ENGLISH, TaskStatus.TODO, "To Do"),
            arrayOf(AppLocale.ENGLISH, TaskStatus.IN_PROGRESS, "In Progress"),
            arrayOf(AppLocale.ENGLISH, TaskStatus.DONE, "Done"),
            arrayOf(AppLocale.SPANISH, TaskStatus.TODO, "Por hacer"),
            arrayOf(AppLocale.SPANISH, TaskStatus.IN_PROGRESS, "En progreso"),
            arrayOf(AppLocale.SPANISH, TaskStatus.DONE, "Hecho"),
        )
    }
}

@RunWith(ParameterizedRobolectricTestRunner::class)
class TaskPriorityLabelComposableTest(
    private val languageTag: String,
    private val priority: TaskPriority,
    private val expectedLabel: String,
) {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Before
    fun setUpLocale() {
        AppLocale.setLanguage(RuntimeEnvironment.getApplication(), languageTag)
    }

    @After
    fun tearDownLocale() {
        AppLocaleTestSupport.resetToEnglish()
    }

    @Test
    fun shouldRenderPriorityLabelWhenComposableInvoked() {
        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                Text(taskPriorityLabel(priority), modifier = Modifier.testTag(LABEL_TAG))
            }
        }

        // Then
        composeTestRule.onNodeWithTag(LABEL_TAG).assertTextEquals(expectedLabel)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{1} [{0}]")
        fun parameters() = listOf(
            arrayOf(AppLocale.ENGLISH, TaskPriority.LOW, "Low"),
            arrayOf(AppLocale.ENGLISH, TaskPriority.MEDIUM, "Medium"),
            arrayOf(AppLocale.ENGLISH, TaskPriority.HIGH, "High"),
            arrayOf(AppLocale.SPANISH, TaskPriority.LOW, "Baja"),
            arrayOf(AppLocale.SPANISH, TaskPriority.MEDIUM, "Media"),
            arrayOf(AppLocale.SPANISH, TaskPriority.HIGH, "Alta"),
        )
    }
}
