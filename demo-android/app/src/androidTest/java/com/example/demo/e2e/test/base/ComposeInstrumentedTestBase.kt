package com.example.demo.e2e.test.base

import android.os.Build
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.services.storage.TestStorage
import com.example.demo.BuildConfig
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.MainPage
import com.example.demo.e2e.provider.StepProvider
import com.example.demo.e2e.provider.SupportProvider
import com.example.demo.e2e.provider.ValidationProvider
import io.qameta.allure.android.rules.LogcatRule
import io.qameta.allure.android.rules.ScreenshotRule
import io.qameta.allure.android.rules.WindowHierarchyRule
import io.qameta.allure.kotlin.util.PropertiesUtils
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
        private const val ALLURE_ENV_FILE = "environment.properties"
        private var allureEnvironmentWritten = false

        @AfterClass
        @JvmStatic
        fun writeAllureEnvironment() {
            if (allureEnvironmentWritten) {
                return
            }
            allureEnvironmentWritten = true

            val resultsDir = PropertiesUtils.resultsDirectoryPath
            val envPath = "$resultsDir/$ALLURE_ENV_FILE"
            val properties = Properties().apply {
                setProperty("Framework", ALLURE_FRAMEWORK)
                setProperty("Device", "${Build.MANUFACTURER} ${Build.MODEL}")
                setProperty("Android.API", Build.VERSION.SDK_INT.toString())
                setProperty("Android.Release", Build.VERSION.RELEASE)
                setProperty("API.Base.URL", BuildConfig.API_BASE_URL)
            }

            if (useTestStorage()) {
                TestStorage().openOutputFile(envPath).use { stream ->
                    properties.store(stream, null)
                }
            } else {
                val outputDir = File(
                    InstrumentationRegistry.getInstrumentation().targetContext.filesDir,
                    resultsDir,
                )
                outputDir.mkdirs()
                File(outputDir, ALLURE_ENV_FILE).outputStream().use { stream ->
                    properties.store(stream, null)
                }
            }
        }

        private fun useTestStorage(): Boolean =
            PropertiesUtils.loadAllureProperties()
                .getProperty("allure.results.useTestStorage", "false")
                .toBoolean()
    }
}
