package com.example.demo

import android.app.Application
import com.example.demo.data.remote.ApiClient
import com.example.demo.repository.TaskRepository

class DemoApp : Application() {
    val taskRepository: TaskRepository by lazy {
        TaskRepository(ApiClient.createTaskApi(BuildConfig.API_BASE_URL))
    }
}
