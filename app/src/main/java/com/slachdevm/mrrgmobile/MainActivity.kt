package com.slachdevm.mrrgmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.slachdevm.mrrgmobile.fcm.NotificationHelper
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarHost
import com.slachdevm.mrrgmobile.ui.navigation.AppNavigation
import com.slachdevm.mrrgmobile.ui.permissions.NotificationPermissionHelper
import com.slachdevm.mrrgmobile.ui.theme.MRRGMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialJobId = intent.getLongExtra(
            NotificationHelper.EXTRA_JOB_ID,
            -1L
        ).takeIf { it != -1L }
        setContent {
            MRRGMobileTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    snackbarHost = {
                        AppSnackbarHost(snackbarHostState)
                    }
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        initialJobId = initialJobId
                    )
                }
            }
        }
        NotificationPermissionHelper(this)
            .requestIfNeeded()
    }
}
