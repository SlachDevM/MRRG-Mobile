package com.slachdevm.mrrgmobile.ui.components.snackbar

enum class SnackbarType {
    SUCCESS,
    ERROR,
    INFO
}

data class SnackbarMessage(
    val message: String,
    val type: SnackbarType
)