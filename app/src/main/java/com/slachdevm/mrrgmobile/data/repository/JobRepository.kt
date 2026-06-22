package com.slachdevm.mrrgmobile.data.repository

import com.google.gson.Gson
import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.data.dto.toDomain
import com.slachdevm.mrrgmobile.data.dto.toDto
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.data.local.dao.PendingSyncDao
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncEntity
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncType
import com.slachdevm.mrrgmobile.data.local.mapper.toDomain
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.model.DataSourceResult
import com.slachdevm.mrrgmobile.data.util.ErrorUtils
import com.slachdevm.mrrgmobile.domain.model.Job
import java.time.LocalDate

class JobRepository(
    private val jobApi: JobApi,
    private val jobDao: JobDao,
    private val pendingSyncDao: PendingSyncDao
) {

    private val gson = Gson()

    suspend fun getScheduledJobs(
        startDate: LocalDate,
        endDate: LocalDate
    ): Result<DataSourceResult<List<Job>>> {
        val startStr = startDate.toString()
        val endStr = endDate.toString()
        
        return try {
            val response = jobApi.getScheduledJobs(startStr, endStr)
            val body = response.body()

            if (response.isSuccessful && body != null) {
                val domainJobs = body.map { it.toDomain() }

                jobDao.upsertJobs(
                    domainJobs.mapNotNull { it.toEntity() }
                )

                Result.success(
                    DataSourceResult(
                        data = domainJobs,
                        isOfflineData = false
                    )
                )
            } else {
                val cachedJobs = jobDao.getScheduledJobs(startStr, endStr)
                    .map { it.toDomain() }

                if (cachedJobs.isNotEmpty()) {
                    Result.success(
                        DataSourceResult(
                            data = cachedJobs,
                            isOfflineData = true
                        )
                    )
                } else {
                    val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to fetch scheduled jobs")
                    Result.failure(Exception(errorMessage))
                }
            }
        } catch (e: Exception) {
            val cachedJobs = jobDao.getScheduledJobs(startStr, endStr)
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
                val remoteJob = body.toDomain()

                remoteJob.toEntity()?.let { jobDao.upsertJob(it) }

                Result.success(remoteJob)
            } else {
                val cachedJob = jobDao.getJobById(id)?.toDomain()

                if (cachedJob != null) {
                    Result.success(cachedJob)
                } else {
                    val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to fetch job details")
                    Result.failure(Exception(errorMessage))
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
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to complete job")
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateJob(id: Long, job: Job): Result<Job> {
        return try {
            val response = jobApi.updateJob(id, job.toDto())
            val body = response.body()

            if (response.isSuccessful && body != null) {
                val updatedJob = body.toDomain()

                updatedJob.toEntity()?.let { jobDao.upsertJob(it) }

                Result.success(updatedJob)
            } else {
                val errorMessage = ErrorUtils.extractErrorMessage(response, "Failed to update job")
                Result.failure(Exception(errorMessage))
            }
        } catch (_: Exception) {
            val localJob = job.copy(id = id)

            localJob.toEntity()?.let { jobDao.upsertJob(it) }

            val existingPendingItem = pendingSyncDao.getPendingItemByTypeAndEntityId(
                type = PendingSyncType.UPDATE_JOB,
                entityId = id
            )

            pendingSyncDao.upsertPendingItem(
                existingPendingItem?.copy(
                    payload = gson.toJson(localJob.toDto()),
                    retryCount = 0,
                    lastError = null,
                    createdAt = System.currentTimeMillis()
                ) ?: PendingSyncEntity(
                    type = PendingSyncType.UPDATE_JOB,
                    entityId = id,
                    payload = gson.toJson(localJob.toDto())
                )
            )

            Result.success(localJob)
        }
    }
}
