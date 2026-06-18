package com.example.demo.e2e.test.translation

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.TRANSLATION
import com.example.demo.e2e.data.TaskResponse
import com.example.demo.e2e.test.base.MockedBackendTestBase
import com.example.demo.locale.AppLocale
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Before
import org.junit.Test

@Epic(TRANSLATION)
@Feature("Language support")
@TmsLink("104")
class LanguageSupportTest : MockedBackendTestBase() {

    private val targetContext: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private lateinit var testContext: TaskTestContext

    @Before
    fun setup() {
        testContext = TaskTestContext(
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
        )

        support().mock.api()
            .getTasks(testContext.createTaskResponse().toTask())
            .getTasks(testContext.createTaskResponse().toTask())
    }

    @Before
    fun resetLocale() {
        targetContext.getSharedPreferences("demo_locale", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        AppLocale.setLanguage(targetContext, AppLocale.ENGLISH)
    }

    @Test
    fun shouldSwitchUiToSpanishWhenEsSelected() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.language.selectLanguage("ES")

        // Then
        validate.language.uiIsInSpanish()
        validate.language.statusTagShowsText(TaskStatus.TODO, "Por hacer")
        validate.language.priorityTagShowsText(TaskPriority.LOW, "Baja")
    }
}
