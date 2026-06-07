package com.example.demo.locale

import android.app.Application
import android.content.Context
import com.example.demo.R
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
class AppLocaleTest {

    private val application: Application = RuntimeEnvironment.getApplication()
    private lateinit var defaultLocale: Locale

    @Before
    fun setUp() {
        defaultLocale = Locale.getDefault()
        application.getSharedPreferences("demo_locale", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()
        AppLocale.setLanguage(application, AppLocale.ENGLISH)
    }

    @After
    fun tearDown() {
        Locale.setDefault(defaultLocale)
        AppLocale.setLanguage(application, AppLocale.ENGLISH)
    }

    @Test
    fun shouldLoadStoredLanguageTagWhenInitInvoked() {
        // Given
        AppLocale.setLanguage(application, AppLocale.SPANISH)

        // When
        AppLocale.init(application)

        // Then
        assertThat(AppLocale.languageTag.value).isEqualTo(AppLocale.SPANISH)
    }

    @Test
    fun shouldReturnStoredLanguageTagWhenPreferenceExists() {
        // Given
        AppLocale.setLanguage(application, AppLocale.SPANISH)

        // When
        val storedTag = AppLocale.getStoredLanguageTag(application)

        // Then
        assertThat(storedTag).isEqualTo(AppLocale.SPANISH)
    }

    @Test
    fun shouldDefaultToEnglishWhenNoPreferenceAndDeviceLocaleIsEnglish() {
        // Given
        Locale.setDefault(Locale.ENGLISH)

        // When
        val storedTag = AppLocale.getStoredLanguageTag(application)
        val defaultTag = AppLocale.defaultLanguageTag()

        // Then
        assertThat(storedTag).isEqualTo(AppLocale.ENGLISH)
        assertThat(defaultTag).isEqualTo(AppLocale.ENGLISH)
    }

    @Test
    fun shouldDefaultToSpanishWhenDeviceLocaleIsSpanish() {
        // Given
        Locale.setDefault(Locale.forLanguageTag("es-MX"))

        // When
        val defaultTag = AppLocale.defaultLanguageTag()

        // Then
        assertThat(defaultTag).isEqualTo(AppLocale.SPANISH)
    }

    @Test
    fun shouldNotUpdateFlowWhenLanguageUnchanged() {
        // Given
        AppLocale.setLanguage(application, AppLocale.ENGLISH)

        // When
        AppLocale.setLanguage(application, AppLocale.ENGLISH)

        // Then
        assertThat(AppLocale.languageTag.value).isEqualTo(AppLocale.ENGLISH)
    }

    @Test
    fun shouldUpdateFlowWhenLanguageChanges() {
        // Given
        AppLocale.setLanguage(application, AppLocale.ENGLISH)

        // When
        AppLocale.setLanguage(application, AppLocale.SPANISH)

        // Then
        assertThat(AppLocale.languageTag.value).isEqualTo(AppLocale.SPANISH)
    }

    @Test
    fun shouldReturnFormattedStringWhenFormatArgsProvided() {
        // When
        val message = AppLocale.getString(application, R.string.error_request_failed, 503)

        // Then
        assertThat(message).isEqualTo("Request failed (503)")
    }

    @Test
    fun shouldReturnSpanishStringWhenLocalizedContextUsesSpanish() {
        // When
        val localized = AppLocale.localizedContext(application, AppLocale.SPANISH)

        // Then
        assertThat(localized.getString(R.string.status_todo)).isEqualTo("Por hacer")
    }

    @Test
    fun shouldUseCurrentLanguageTagWhenLocalizedContextOmitsTag() {
        // Given
        AppLocale.setLanguage(application, AppLocale.SPANISH)

        // When
        val localized = AppLocale.localizedContext(application)

        // Then
        assertThat(localized.getString(R.string.status_todo)).isEqualTo("Por hacer")
    }

    @Test
    fun shouldReturnSpanishStringFromLocalizedApplication() {
        // Given
        AppLocale.setLanguage(application, AppLocale.SPANISH)

        // When
        val localized = AppLocale.localizedApplication(application)

        // Then
        assertThat(localized.getString(R.string.status_todo)).isEqualTo("Por hacer")
    }
}
