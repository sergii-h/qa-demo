package com.example.demo.testing

import android.content.Context
import com.example.demo.locale.AppLocale
import org.robolectric.RuntimeEnvironment

object AppLocaleTestSupport {
    fun resetToEnglish() {
        val context = RuntimeEnvironment.getApplication()
        context.getSharedPreferences("demo_locale", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        AppLocale.setLanguage(context, AppLocale.ENGLISH)
    }
}
