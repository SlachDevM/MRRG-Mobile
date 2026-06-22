package com.slachdevm.mrrgmobile.data.session

import android.content.Context
import android.content.SharedPreferences
import com.slachdevm.mrrgmobile.domain.model.UserRole

class SessionManager(context: Context) {
    private var prefs: SharedPreferences =
        context.getSharedPreferences("mrrg_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val USER_TOKEN = "user_token"
        private const val USER_ID = "user_id"
        private const val USER_NAME = "user_name"
        private const val USER_ROLE = "user_role"
    }

    fun saveAuthToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    fun saveUserId(userId: Long) {
        val editor = prefs.edit()
        editor.putLong(USER_ID, userId)
        editor.apply()
    }

    fun fetchUserId(): Long {
        return prefs.getLong(USER_ID, -1L)
    }

    fun saveUserName(name: String) {
        val editor = prefs.edit()
        editor.putString(USER_NAME, name)
        editor.apply()
    }

    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    fun saveUserRole(role: UserRole) {
        val editor = prefs.edit()
        editor.putString(USER_ROLE, role.name)
        editor.apply()
    }

    fun fetchUserRole(): UserRole? {
        val roleName = prefs.getString(USER_ROLE, null)
        return try {
            if (roleName != null) UserRole.valueOf(roleName) else null
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.remove(USER_ID)
        editor.remove(USER_NAME)
        editor.remove(USER_ROLE)
        editor.apply()
    }

    fun clearAuthToken() {
        val editor = prefs.edit()
        editor.remove(USER_TOKEN)
        editor.apply()
    }
}
