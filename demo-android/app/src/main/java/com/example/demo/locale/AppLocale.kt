package com.example.demo.locale

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

object AppLocale {
    private const val PREFS_NAME = "demo_locale"
    private const val KEY_LANGUAGE = "language_tag"

    const val ENGLISH = "en"
    const val SPANISH = "es"

    private val _languageTag = MutableStateFlow(ENGLISH)
    val languageTag: StateFlow<String> = _languageTag.asStateFlow()

    fun init(context: Context) {
        _languageTag.value = getStoredLanguageTag(context)
    }

    fun getStoredLanguageTag(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_LANGUAGE, null)
            ?: defaultLanguageTag()
    }

    fun defaultLanguageTag(): String {
        val language = Locale.getDefault().language
        return if (language.startsWith(SPANISH)) SPANISH else ENGLISH
    }

    fun setLanguage(context: Context, languageTag: String) {
        if (_languageTag.value == languageTag) {
            return
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LANGUAGE, languageTag)
            .apply()
        _languageTag.value = languageTag
    }

    fun getString(context: Context, @StringRes resId: Int, vararg formatArgs: Any): String {
        val localized = localizedContext(context, _languageTag.value)
        return if (formatArgs.isEmpty()) {
            localized.getString(resId)
        } else {
            localized.getString(resId, *formatArgs)
        }
    }

    fun localizedContext(context: Context, languageTag: String = _languageTag.value): Context {
        val locale = Locale.forLanguageTag(languageTag)
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        return context.createConfigurationContext(configuration)
    }

    fun localizedApplication(application: Application): Context =
        localizedContext(application, _languageTag.value)
}
