package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure

class NavigationStep(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val mainPage = MainPage(rule)

    fun openMainPage() {
        Allure.step("Open main page") {
            mainPage.waitUntilReady()
            mainPage.createTaskButton().assertIsDisplayed()
        }
    }

    fun refreshMainPage() {
        Allure.step("Refresh main page") {
            mainPage.pullToRefresh()
            mainPage.waitUntilRefreshComplete()
        }
    }
}
