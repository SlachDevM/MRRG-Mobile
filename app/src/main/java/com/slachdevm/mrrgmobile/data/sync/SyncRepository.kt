package com.slachdevm.mrrgmobile.data.sync

import android.util.Log
import com.google.gson.Gson
import com.slachdevm.mrrgmobile.BuildConfig
import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.data.dto.JobDto
import com.slachdevm.mrrgmobile.data.dto.toDomain
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.data.local.dao.PendingSyncDao
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncType
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.util.ErrorUtils
import com.slachdevm.mrrgmobile.data.util.SessionExpiredException

class SyncRepository(
    private val jobApi: JobApi,
    private val jobDao: JobDao,
    private val pendingSyncDao: PendingSyncDao
) {
    private val gson = Gson()

    suspend fun synchronize(): Result<Unit> {
        return try {
            val pendingItems = pendingSyncDao.getPendingItems()

            pendingItems.forEach { item ->
                runCatching {
                    when (item.type) {
                        PendingSyncType.UPDATE_JOB -> {
                            syncUpdateJob(
                                syncId = item.id,
                                jobId = item.entityId,
                                payload = item.payload
                            )
                        }
                    }
                }.onFailure { throwable ->
                    if (throwable is SessionExpiredException) {
                        return Result.failure(throwable)
                    }

                    pendingSyncDao.upsertPendingItem(
                        item.copy(
                            retryCount = item.retryCount + 1,
                            lastError = throwable.message
                        )
                    )

                    if (BuildConfig.DEBUG) {
                        Log.w(
                            TAG,
                            "Failed to sync ${item.type} for entity ${item.entityId}",
                            throwable
                        )
                    }
                }
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncUpdateJob(
        syncId: Long,
        jobId: Long,
        payload: String
    ) {
        val jobDto = gson.fromJson(payload, JobDto::class.java)

        val response = jobApi.updateJob(jobId, jobDto)

        val body = response.body()
        if (response.isSuccessful && body != null) {
            val updatedJob = body.toDomain()

            updatedJob.toEntity()?.let { jobDao.upsertJob(it) }

            pendingSyncDao.deletePendingItem(syncId)
        } else {
            val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to sync job update")
            throw Exception(errorMessage)
        }
    }

    companion object {
        private const val TAG = "SyncRepository"
    }
}
