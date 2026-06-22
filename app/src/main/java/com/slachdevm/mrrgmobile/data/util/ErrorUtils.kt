package com.slachdevm.mrrgmobile.data.util

import com.google.gson.JsonParser
import retrofit2.Response

object ErrorUtils {
    fun extractErrorMessage(response: Response<*>, fallbackMessage: String): String {
        return try {
            val errorBody = response.errorBody()?.string()
            if (errorBody.isNullOrBlank()) {
                fallbackMessage
            } else if (errorBody.trim().startsWith("{")) {
                val jsonObject = JsonParser.parseString(errorBody).asJsonObject
                jsonObject.get("message")?.asString ?: fallbackMessage
            } else {
                errorBody
            }
        } catch (_: Exception) {
            fallbackMessage
        }
    }
}
