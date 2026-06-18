package com.slachdevm.mrrgmobile.fcm

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MrrgFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Log.d(TAG, "New FCM token: $token")

        // Étape suivante :
        // envoyer ce token au backend pour l'associer au worker connecté.
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Log.d(TAG, "FCM message received")
        Log.d(TAG, "From: ${message.from}")
        Log.d(TAG, "Data: ${message.data}")

        message.notification?.let { notification ->
            Log.d(TAG, "Notification title: ${notification.title}")
            Log.d(TAG, "Notification body: ${notification.body}")
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