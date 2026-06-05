package com.example.demo.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

@Composable
fun LocalizedContent(languageTag: String, content: @Composable () -> Unit) {
    val baseContext = LocalContext.current
    val localizedContext = remember(languageTag) {
        AppLocale.localizedContext(baseContext, languageTag)
    }
    val configuration = remember(languageTag) {
        Configuration(localizedContext.resources.configuration).apply {
            setLocale(Locale.forLanguageTag(languageTag))
        }
    }

    CompositionLocalProvider(
        LocalContext provides localizedContext,
        LocalConfiguration provides configuration
    ) {
        key(languageTag) {
            content()
        }
    }
}
