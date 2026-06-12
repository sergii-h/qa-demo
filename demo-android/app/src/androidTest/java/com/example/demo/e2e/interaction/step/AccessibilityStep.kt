package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.tryPerformAccessibilityChecks
import com.example.demo.MainActivity
import io.qameta.allure.kotlin.Allure

class AccessibilityStep(
    private val rule: AndroidComposeTestRule<*, MainActivity>,
) {
    fun analyze() {
        Allure.step("Analyze page accessibility") {
            rule.onRoot().tryPerformAccessibilityChecks()
        }
    }
}
