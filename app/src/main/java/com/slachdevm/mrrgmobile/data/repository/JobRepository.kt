package com.slachdevm.mrrgmobile.data.repository

import com.google.gson.Gson
import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.data.local.dao.PendingSyncDao
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncEntity
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncType
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.data.local.mapper.toDomain
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.model.DataSourceResult

class JobRepository(
    private val jobApi: JobApi,
    private val jobDao: JobDao,
    private val pendingSyncDao: PendingSyncDao
) {

    private val gson = Gson()

    suspend fun getScheduledJobs(
        weekStart: Long,
        weekEnd: Long
    ): Result<DataSourceResult<List<Job>>> {
        return try {
            val response = jobApi.getScheduledJobs(weekStart, weekEnd)

            val body = response.body()
            if (response.isSuccessful && body != null) {
                val remoteJobs = body

                jobDao.upsertJobs(
                    remoteJobs.mapNotNull { it.toEntity() }
                )

                Result.success(
                    DataSourceResult(
                        data = remoteJobs,
                        isOfflineData = false
                    )
                )
            } else {
                val cachedJobs = jobDao.getScheduledJobs(weekStart, weekEnd)
                    .map { it.toDomain() }

                if (cachedJobs.isNotEmpty()) {
                    Result.success(
                        DataSourceResult(
                            data = cachedJobs,
                            isOfflineData = true
                        )
                    )
                } else {
                    Result.failure(
                        Exception("Failed to fetch scheduled jobs: ${response.code()}")
                    )
                }
            }
        } catch (e: Exception) {
            val cachedJobs = jobDao.getScheduledJobs(weekStart, weekEnd)
                .map { it.toDomain() }

            if (cachedJobs.isNotEmpty()) {
                Result.success(
                    DataSourceResult(
                        data = cachedJobs,
                        isOfflineData = true
                    )
                )
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun getJobDetails(id: Long): Result<Job> {
        return try {
            val response = jobApi.getJobDetails(id)

            val body = response.body()
            if (response.isSuccessful && body != null) {
                val remoteJob = body

                remoteJob.toEntity()?.let { jobDao.upsertJob(it) }

                Result.success(remoteJob)
            } else {
                val cachedJob = jobDao.getJobById(id)?.toDomain()

                if (cachedJob != null) {
                    Result.success(cachedJob)
                } else {
                    Result.failure(Exception("Failed to fetch job details: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            val cachedJob = jobDao.getJobById(id)?.toDomain()

            if (cachedJob != null) {
                Result.success(cachedJob)
            } else {
                Result.failure(e)
            }
        }
    }

    suspend fun completeJob(id: Long): Result<Unit> {
        return try {
            val response = jobApi.completeJob(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to complete job: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJob(id: Long, job: Job): Result<Job> {
        return try {
            val response = jobApi.updateJob(id, job)

            val body = response.body()
            if (response.isSuccessful && body != null) {
                val updatedJob = body

                updatedJob.toEntity()?.let { jobDao.upsertJob(it) }

                Result.success(updatedJob)
            } else {
                Result.failure(Exception("Failed to update job: ${response.code()}"))
            }
        } catch (e: Exception) {
            val localJob = job.copy(id = id)

            localJob.toEntity()?.let { jobDao.upsertJob(it) }

            val existingPendingItem = pendingSyncDao.getPendingItemByTypeAndEntityId(
                type = PendingSyncType.UPDATE_JOB,
                entityId = id
            )

            pendingSyncDao.upsertPendingItem(
                existingPendingItem?.copy(
                    payload = gson.toJson(localJob),
                    retryCount = 0,
                    lastError = null,
                    createdAt = System.currentTimeMillis()
                ) ?: PendingSyncEntity(
                    type = PendingSyncType.UPDATE_JOB,
                    entityId = id,
                    payload = gson.toJson(localJob)
                )
            )

            Result.success(localJob)
        }
    }
}
