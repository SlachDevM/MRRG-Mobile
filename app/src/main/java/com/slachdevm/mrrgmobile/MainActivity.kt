package com.slachdevm.mrrgmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.slachdevm.mrrgmobile.fcm.NotificationHelper
import com.slachdevm.mrrgmobile.ui.components.snackbar.AppSnackbarHost
import com.slachdevm.mrrgmobile.ui.navigation.AppNavigation
import com.slachdevm.mrrgmobile.ui.permissions.NotificationPermissionHelper
import com.slachdevm.mrrgmobile.ui.theme.MRRGMobileTheme
import com.slachdevm.mrrgmobile.ui.theme.ThemeMode
import com.slachdevm.mrrgmobile.data.preferences.ThemePreferenceManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initialJobId = intent.getLongExtra(
            NotificationHelper.EXTRA_JOB_ID,
            -1L
        ).takeIf { it != -1L }
        val activationToken = intent?.data?.getQueryParameter("token")
        setContent {
            val themePreferenceManager = remember {
                ThemePreferenceManager(applicationContext)
            }

            val themeMode by themePreferenceManager.themeMode.collectAsState(
                initial = ThemeMode.SYSTEM
            )

            val coroutineScope = rememberCoroutineScope()
            val snackbarHostState = remember { SnackbarHostState() }
            MRRGMobileTheme (
                themeMode = themeMode
            ) {
                Scaffold(
                    snackbarHost = {
                        AppSnackbarHost(snackbarHostState)
                    }
                ) { innerPadding ->
                    AppNavigation(
                        modifier = Modifier.padding(innerPadding),
                        initialJobId = initialJobId,
                        activationToken = activationToken,
                        themeMode = themeMode,
                        onThemeModeChange = { selectedThemeMode ->
                            coroutineScope.launch {
                                themePreferenceManager.saveThemeMode(selectedThemeMode)
                            }
                        }
                    )
                }
            }
        }
        NotificationPermissionHelper(this)
            .requestIfNeeded()
    }
}
