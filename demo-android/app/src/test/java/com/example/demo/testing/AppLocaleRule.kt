package com.example.demo.testing

import org.junit.rules.TestWatcher
import org.junit.runner.Description

class AppLocaleRule : TestWatcher() {
    override fun starting(description: Description) {
        AppLocaleTestSupport.resetToEnglish()
    }

    override fun finished(description: Description) {
        AppLocaleTestSupport.resetToEnglish()
    }
}
