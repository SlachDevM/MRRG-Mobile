package com.slachdevm.mrrgmobile.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.slachdevm.mrrgmobile.BuildConfig
import com.slachdevm.mrrgmobile.data.api.RetrofitClient
import com.slachdevm.mrrgmobile.data.repository.AuthRepository
import com.slachdevm.mrrgmobile.data.session.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MrrgFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "New FCM token: $token")
        }

        // Initialize dependencies manually for the background service
        val sessionManager = SessionManager(applicationContext)
        val authRepository = AuthRepository(
            authApi = RetrofitClient.authApi,
            userApi = RetrofitClient.userApi,
            sessionManager = sessionManager
        )

        // Only update backend if user is already logged in
        if (authRepository.isLoggedIn()) {
            serviceScope.launch {
                authRepository.updateFcmToken(token)
                    .onSuccess {
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "FCM token successfully updated on backend after refresh")
                        }
                    }
                    .onFailure { error ->
                        Log.e(TAG, "Failed to update FCM token on backend after refresh", error)
                    }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "FCM message received")
            Log.d(TAG, "From: ${message.from}")
            Log.d(TAG, "Data: ${message.data}")
        }

        message.notification?.let { notification ->
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Notification title: ${notification.title}")
                Log.d(TAG, "Notification body: ${notification.body}")
            }
        }

        val title = message.notification?.title
            ?: message.data["title"]
            ?: "MRRG"

        val body = message.notification?.body
            ?: message.data["body"]
            ?: "You have a new notification"

        val jobId = message.data["jobId"]?.toLongOrNull()

        NotificationHelper(this).showNotification(
            title = title,
            body = body,
            jobId = jobId
        )
    }

    companion object {
        private const val TAG = "MRRG_FCM"
    }
}
