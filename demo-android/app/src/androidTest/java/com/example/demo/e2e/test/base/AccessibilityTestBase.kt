package com.example.demo.e2e.test.base

import android.os.Build
import androidx.compose.ui.test.junit4.accessibility.enableAccessibilityChecks
import org.junit.Assume
import org.junit.Before
import java.lang.annotation.Inherited

@MustBeDocumented
@Inherited
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
annotation class Accessibility

@Accessibility
abstract class AccessibilityTestBase : MockedBackendTestBase() {
    @Before
    fun setUpAccessibilityHarness() {
        Assume.assumeTrue(
            "Accessibility checks require API 34+",
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE,
        )
        composeTestRule.enableAccessibilityChecks()
    }
}
