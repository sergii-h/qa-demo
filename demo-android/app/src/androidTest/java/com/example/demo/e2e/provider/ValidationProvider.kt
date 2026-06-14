package com.example.demo.e2e.provider

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.validation.AccessibilityValidator
import com.example.demo.e2e.interaction.validation.LanguageValidator
import com.example.demo.e2e.interaction.validation.TaskValidator
import com.example.demo.e2e.interaction.validation.TasksValidator

class ValidationProvider(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    val tasks: TasksValidator = TasksValidator(rule)
    val task: TaskValidator = TaskValidator(rule)
    val language: LanguageValidator = LanguageValidator(rule)
    val accessibility: AccessibilityValidator = AccessibilityValidator()
}
