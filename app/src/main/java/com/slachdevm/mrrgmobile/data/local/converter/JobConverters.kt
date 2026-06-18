package com.slachdevm.mrrgmobile.data.local.converter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.slachdevm.mrrgmobile.domain.model.JobStatus

class JobConverters {

    private val gson = Gson()

    @TypeConverter
    fun fromJobStatus(status: JobStatus): String = status.name

    @TypeConverter
    fun toJobStatus(value: String): JobStatus =
        runCatching { JobStatus.valueOf(value) }.getOrDefault(JobStatus.PENDING)

    @TypeConverter
    fun fromStringList(value: List<String>): String =
        gson.toJson(value)

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return runCatching {
            gson.fromJson<List<String>>(value, type)
        }.getOrDefault(emptyList())
    }
}