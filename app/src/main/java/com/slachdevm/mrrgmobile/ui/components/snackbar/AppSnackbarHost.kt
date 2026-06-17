package com.slachdevm.mrrgmobile.ui.components.snackbar

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

@Composable
fun AppSnackbarHost(
    snackbarHostState: SnackbarHostState
) {
    var currentType by remember { mutableStateOf<SnackbarType?>(null) }

    LaunchedEffect(Unit) {
        AppSnackbarManager.messages.collect { snackbarMessage ->
            currentType = snackbarMessage.type

            snackbarHostState.showSnackbar(
                message = snackbarMessage.message
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState
    ) { snackbarData ->
        val backgroundColor = when (currentType) {
            SnackbarType.SUCCESS -> Color(0xFF2E7D32)
            SnackbarType.ERROR -> MaterialTheme.colorScheme.error
            SnackbarType.INFO -> MaterialTheme.colorScheme.primary
            null -> MaterialTheme.colorScheme.inverseSurface
        }

        Snackbar(
            containerColor = backgroundColor,
            contentColor = Color.White
        ) {
            Text(snackbarData.visuals.message)
        }
    }
}