package com.slachdevm.mrrgmobile.ui.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

class NotificationPermissionHelper(
    private val activity: ComponentActivity
) {

    private val launcher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            // Rien à faire pour l'instant.
            // Si refusé, les notifications système seront simplement désactivées.
        }

    fun requestIfNeeded() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }
}