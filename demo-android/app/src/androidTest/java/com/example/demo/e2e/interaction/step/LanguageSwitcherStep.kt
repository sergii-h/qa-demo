package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.LanguageSwitcherDropdown
import io.qameta.allure.kotlin.Allure

class LanguageSwitcherStep(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val languageSwitcher = LanguageSwitcherDropdown(rule)

    fun selectLanguage(language: String) {
        Allure.step("Select language '$language'") {
            languageSwitcher.dropdown().performClick()
            when (language) {
                "ES" -> languageSwitcher.spanishOption().performClick()
                "EN" -> languageSwitcher.englishOption().performClick()
                else -> error("Unsupported language: $language")
            }
        }
    }
}
