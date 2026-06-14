package com.example.demo.e2e.test.base

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.MainPage
import com.example.demo.e2e.provider.StepProvider
import com.example.demo.e2e.provider.SupportProvider
import com.example.demo.e2e.provider.ValidationProvider
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.rules.WindowHierarchyRule
import org.junit.AfterClass
import org.junit.Before
import org.junit.rules.RuleChain
import java.io.File
import java.util.Properties

abstract class ComposeInstrumentedTestBase {
    protected val composeTestRule = createAndroidComposeRule<MainActivity>()
    protected val support = SupportProvider()
    protected val steps = StepProvider(composeTestRule)
    protected val validate = ValidationProvider(composeTestRule)

    protected fun support(): SupportProvider = support

    protected fun allureComposeRuleChain(
        composeTestRule: AndroidComposeTestRule<*, MainActivity> = this.composeTestRule,
    ): RuleChain = RuleChain
        .outerRule(ScreenshotRule(ScreenshotRule.Mode.FAILURE, "screenshot"))
        .around(LogcatRule())
        .around(WindowHierarchyRule())
        .around(composeTestRule)

    @Before
    fun setUpComposeHarness() {
        MainPage(composeTestRule).waitUntilReady()
    }

    companion object {
        private const val ALLURE_FRAMEWORK = "Android Compose"
        private const val ALLURE_RESULTS_DIR = "allure-results"
        private const val ALLURE_ENV_FILE = "environment.properties"

        @AfterClass
        @JvmStatic
        fun writeAllureEnvironment() {
            val context = InstrumentationRegistry.getInstrumentation().targetContext
            val resultsDir = File(context.filesDir, ALLURE_RESULTS_DIR)
            resultsDir.mkdirs()

            val envFile = File(resultsDir, ALLURE_ENV_FILE)
            if (envFile.exists()) {
                return
            }

            Properties().apply {
                setProperty("Framework", ALLURE_FRAMEWORK)
            }.store(envFile.outputStream(), null)
        }
    }
}
