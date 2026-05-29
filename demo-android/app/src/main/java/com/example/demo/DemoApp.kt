package com.example.demo

import android.app.Application
import com.example.demo.repository.TaskRepository

class DemoApp : Application() {
    val taskRepository by lazy { TaskRepository() }
}
