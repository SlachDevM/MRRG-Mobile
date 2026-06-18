package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.data.local.dao.JobDao
import com.slachdevm.mrrgmobile.domain.model.Job
import com.slachdevm.mrrgmobile.data.local.mapper.toDomain
import com.slachdevm.mrrgmobile.data.local.mapper.toEntity
import com.slachdevm.mrrgmobile.data.model.DataSourceResult

class JobRepository(
    private val jobApi: JobApi,
    private val jobDao: JobDao
) {

    suspend fun getScheduledJobs(
        weekStart: Long,
        weekEnd: Long
    ): Result<DataSourceResult<List<Job>>> {
        return try {
            val response = jobApi.getScheduledJobs(weekStart, weekEnd)

            if (response.isSuccessful && response.body() != null) {
                val remoteJobs = response.body()!!

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

            if (response.isSuccessful && response.body() != null) {
                val remoteJob = response.body()!!

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
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to update job: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
