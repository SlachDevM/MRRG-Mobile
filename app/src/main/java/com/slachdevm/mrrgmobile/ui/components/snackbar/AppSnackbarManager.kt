package com.slachdevm.mrrgmobile.ui.components.snackbar

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object AppSnackbarManager {

    private val _messages = MutableSharedFlow<SnackbarMessage>(
        extraBufferCapacity = 1
    )

    val messages = _messages.asSharedFlow()

    fun showSuccess(message: String) {
        _messages.tryEmit(SnackbarMessage(message, SnackbarType.SUCCESS))
    }

    fun showError(message: String) {
        _messages.tryEmit(SnackbarMessage(message, SnackbarType.ERROR))
    }

    fun showInfo(message: String) {
        _messages.tryEmit(SnackbarMessage(message, SnackbarType.INFO))
    }
}