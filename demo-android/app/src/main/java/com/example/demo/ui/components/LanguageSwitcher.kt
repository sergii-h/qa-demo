package com.example.demo.ui.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import com.example.demo.R
import com.example.demo.locale.AppLocale

@Composable
fun LanguageSwitcher(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val languageTag by AppLocale.languageTag.collectAsState()
    var expanded by remember { mutableStateOf(false) }
    val accessibilityLabel = stringResource(R.string.language_label)

    val selectedLabel = when (languageTag) {
        AppLocale.SPANISH -> stringResource(R.string.language_es)
        else -> stringResource(R.string.language_en)
    }

    TextButton(
        onClick = { expanded = true },
        modifier = modifier.semantics { contentDescription = accessibilityLabel }
    ) {
        Text(
            text = selectedLabel,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false }
    ) {
        DropdownMenuItem(
            text = { Text(stringResource(R.string.language_en)) },
            onClick = {
                expanded = false
                AppLocale.setLanguage(context, AppLocale.ENGLISH)
            }
        )
        DropdownMenuItem(
            text = { Text(stringResource(R.string.language_es)) },
            onClick = {
                expanded = false
                AppLocale.setLanguage(context, AppLocale.SPANISH)
            }
        )
    }
}
