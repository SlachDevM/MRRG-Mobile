package com.slachdevm.mrrgmobile.ui.components.snackbar

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

object AppSnackbarManager {

    private val _messages = MutableSharedFlow<SnackbarMessage>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    val messages = _messages.asSharedFlow()

    var lastMessageType: SnackbarType? = null
        private set

    fun showSuccess(message: String) {
        lastMessageType = SnackbarType.SUCCESS

        scope.launch {
            _messages.emit(
                SnackbarMessage(message, SnackbarType.SUCCESS)
            )
        }
    }

    fun showError(message: String) {
        lastMessageType = SnackbarType.ERROR

        scope.launch {
            _messages.emit(
                SnackbarMessage(message, SnackbarType.ERROR)
            )
        }
    }

    fun showInfo(message: String) {
        lastMessageType = SnackbarType.INFO

        scope.launch {
            _messages.emit(
                SnackbarMessage(message, SnackbarType.INFO)
            )
        }
    }
}