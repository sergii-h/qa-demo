package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity

class LanguageSwitcherDropdown(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun dropdown() = node("language-switcher")

    fun englishOption() = node("language-option-en")

    fun spanishOption() = node("language-option-es")
}
