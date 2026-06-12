package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.ui.TestTags

class LanguageSwitcherDropdown(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun dropdown() = node(TestTags.LANGUAGE_SWITCHER)

    fun englishOption() = node(TestTags.LANGUAGE_OPTION_EN)

    fun spanishOption() = node(TestTags.LANGUAGE_OPTION_ES)
}
