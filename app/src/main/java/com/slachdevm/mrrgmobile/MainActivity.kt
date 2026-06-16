package com.slachdevm.mrrgmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.slachdevm.mrrgmobile.ui.navigation.AppNavigation
import com.slachdevm.mrrgmobile.ui.theme.MRRGMobileTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MRRGMobileTheme {
                AppNavigation()
            }
        }
    }
}
