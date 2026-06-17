package com.slachdevm.mrrgmobile.data.api

import com.slachdevm.mrrgmobile.domain.model.Job
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface JobApi {
    @GET("/api/jobs")
    suspend fun getAllJobs(): Response<List<Job>>

    @GET("/api/jobs/pending")
    suspend fun getPendingJobs(): Response<List<Job>>

    @GET("/api/jobs/done")
    suspend fun getCompletedJobs(): Response<List<Job>>

    @GET("/api/jobs/scheduled")
    suspend fun getScheduledJobs(
        @Query("weekStart") weekStart: Long,
        @Query("weekEnd") weekEnd: Long
    ): Response<List<Job>>

    @GET("/api/jobs/{id}")
    suspend fun getJobDetails(@Path("id") id: Long): Response<Job>

    @POST("/api/jobs")
    suspend fun createJob(@Body job: Job): Response<Job>

    @PUT("/api/jobs/{id}")
    suspend fun updateJob(@Path("id") id: Long, @Body job: Job): Response<Job>

    @PUT("/api/jobs/{id}/complete")
    suspend fun completeJob(@Path("id") id: Long): Response<Unit>

    @PUT("/api/jobs/{id}/confirm")
    suspend fun confirmJob(@Path("id") id: Long): Response<Unit>

    @PUT("/api/jobs/{id}/archive")
    suspend fun archiveJob(@Path("id") id: Long): Response<Unit>

    @DELETE("/api/jobs/{id}")
    suspend fun deleteJob(@Path("id") id: Long): Response<Unit>
}
