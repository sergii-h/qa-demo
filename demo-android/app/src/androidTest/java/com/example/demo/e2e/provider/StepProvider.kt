package com.example.demo.e2e.provider

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.step.AccessibilityStep
import com.example.demo.e2e.interaction.step.CreateTaskStep
import com.example.demo.e2e.interaction.step.EditTaskStep
import com.example.demo.e2e.interaction.step.LanguageSwitcherStep
import com.example.demo.e2e.interaction.step.NavigationStep
import com.example.demo.e2e.interaction.step.TaskTableStep

class StepProvider(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    val tasks: TaskTableStep = TaskTableStep(rule)
    val language: LanguageSwitcherStep = LanguageSwitcherStep(rule)
    val navigation: NavigationStep = NavigationStep(rule)
    val accessibility: AccessibilityStep = AccessibilityStep(rule)
}
