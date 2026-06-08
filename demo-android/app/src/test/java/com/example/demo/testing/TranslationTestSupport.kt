package com.example.demo.testing

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import com.example.demo.locale.AppLocale
import com.google.common.truth.Truth.assertWithMessage
import java.util.Locale

fun recordingContext(
    baseContext: Context,
    languageTag: String,
    recorded: MutableList<Int>,
): Context {
    val localized = AppLocale.localizedContext(baseContext, languageTag)
    return object : ContextWrapper(localized) {
        private val recordingResources = RecordingResources(super.getResources(), recorded)

        override fun getResources(): Resources = recordingResources
    }
}

fun assertHasTranslations(
    recorded: List<Int>,
    languageTag: String,
    context: Context,
) {
    val unique = recorded.distinct()
    assertWithMessage("No string resources recorded. Expected at least one translation to be used.")
        .that(unique)
        .isNotEmpty()

    val localized = AppLocale.localizedContext(context, languageTag)
    val missing = unique.filter { resId ->
        runCatching { localized.getString(resId).isNotBlank() }.getOrDefault(false).not()
    }
    if (missing.isNotEmpty()) {
        val names = missing.joinToString(separator = "\n") { resId ->
            context.resources.getResourceEntryName(resId)
        }
        throw AssertionError("Missing translations for $languageTag:\n$names")
    }
}

@Composable
fun RecordingLocalizedContent(
    languageTag: String,
    recorded: MutableList<Int>,
    content: @Composable () -> Unit,
) {
    val baseContext = LocalContext.current
    val localizedContext = remember(languageTag) {
        recordingContext(baseContext, languageTag, recorded)
    }
    val configuration = remember(languageTag) {
        Configuration(localizedContext.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration,
    ) {
        key(languageTag) {
            content()
        }
    }
}

private class RecordingResources(
    private val wrapped: Resources,
    private val recorded: MutableList<Int>,
) : Resources(wrapped.assets, wrapped.displayMetrics, wrapped.configuration) {

    override fun getString(@StringRes id: Int): String {
        record(id)
        return wrapped.getString(id)
    }

    override fun getString(@StringRes id: Int, vararg formatArgs: Any): String {
        record(id)
        return wrapped.getString(id, *formatArgs)
    }

    override fun getText(@StringRes id: Int): CharSequence {
        record(id)
        return wrapped.getText(id)
    }

    private fun record(@StringRes id: Int) {
        recorded.add(id)
    }
}
