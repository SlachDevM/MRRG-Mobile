package com.slachdevm.mrrgmobile.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.slachdevm.mrrgmobile.BuildConfig
import kotlinx.coroutines.tasks.await

class FcmTokenManager {

    suspend fun getToken(): String? {
        return try {
            val token = FirebaseMessaging.getInstance().token.await()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Current FCM token: $token")
            }
            token
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get FCM token", e)
            null
        }
    }

    companion object {
        private const val TAG = "MRRG_FCM"
    }
}