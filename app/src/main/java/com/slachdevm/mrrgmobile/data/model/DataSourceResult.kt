package com.slachdevm.mrrgmobile.data.model

data class DataSourceResult<T>(
    val data: T,
    val isOfflineData: Boolean
)