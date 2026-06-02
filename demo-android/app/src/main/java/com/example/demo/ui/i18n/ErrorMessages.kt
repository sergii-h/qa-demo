package com.example.demo.ui.i18n

import android.content.Context
import com.example.demo.R
import com.example.demo.data.remote.ApiClient
import com.example.demo.locale.AppLocale
import retrofit2.HttpException

fun mapTaskError(context: Context, throwable: Throwable): String {
    if (throwable is HttpException) {
        val message = ApiClient.parseErrorMessage(
            throwable.response()?.errorBody()?.string()
        )
        if (!message.isNullOrBlank()) {
            return message
        }
        return when (throwable.code()) {
            400 -> AppLocale.getString(context, R.string.error_invalid_task_data)
            404 -> AppLocale.getString(context, R.string.error_task_not_found)
            409 -> AppLocale.getString(context, R.string.error_title_already_exists)
            else -> AppLocale.getString(
                context,
                R.string.error_request_failed,
                throwable.code()
            )
        }
    }
    return throwable.message ?: AppLocale.getString(context, R.string.error_something_went_wrong)
}

fun isDuplicateTitleError(throwable: Throwable): Boolean =
    throwable is HttpException && throwable.code() == 409
