package com.example.demo

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.demo.locale.AppLocale
import com.example.demo.locale.LocalizedContent
import com.example.demo.ui.DemoNavHost
import com.example.demo.ui.theme.DemoTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLocale.init(this)
        val repository = (application as DemoApp).taskRepository
        setContent {
            val languageTag by AppLocale.languageTag.collectAsState()
            LocalizedContent(languageTag = languageTag) {
                DemoTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }
    }
}
