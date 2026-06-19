package com.slachdevm.mrrgmobile.data.sync

import android.util.Log
import com.google.gson.Gson
import com.slachdevm.mrrgmobile.BuildConfig
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.data.local.dao.PendingSyncDao
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncType
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.domain.model.Job

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
                }.onFailure { error ->
                    pendingSyncDao.upsertPendingItem(
                        item.copy(
                            retryCount = item.retryCount + 1,
                            lastError = error.message
                        )
                    )

                    if (BuildConfig.DEBUG) {
                        Log.w(
                            "SyncRepository",
                            "Failed to sync ${item.type} for entity ${item.entityId}",
                            error
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
        val job = gson.fromJson(payload, Job::class.java)

        val response = jobApi.updateJob(jobId, job)

        if (!response.isSuccessful || response.body() == null) {
            throw Exception("Failed to sync job update: ${response.code()}")
        }

        val updatedJob = response.body()!!

        updatedJob.toEntity()?.let { jobDao.upsertJob(it) }

        pendingSyncDao.deletePendingItem(syncId)
    }
}