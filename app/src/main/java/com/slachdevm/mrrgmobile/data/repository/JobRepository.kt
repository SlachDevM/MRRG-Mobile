package com.slachdevm.mrrgmobile.data.repository

import com.slachdevm.mrrgmobile.data.api.JobApi
import com.slachdevm.mrrgmobile.domain.model.Job

class JobRepository(private val jobApi: JobApi) {

    suspend fun getPendingJobs(): Result<List<Job>> {
        return try {
            val response = jobApi.getPendingJobs()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch pending jobs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getScheduledJobs(weekStart: Long, weekEnd: Long): Result<List<Job>> {
        return try {
            val response = jobApi.getScheduledJobs(weekStart, weekEnd)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch scheduled jobs: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getJobDetails(id: Long): Result<Job> {
        return try {
            val response = jobApi.getJobDetails(id)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Failed to fetch job details: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
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
