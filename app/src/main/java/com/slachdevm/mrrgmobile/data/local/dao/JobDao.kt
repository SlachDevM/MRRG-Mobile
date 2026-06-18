package com.slachdevm.mrrgmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.slachdevm.mrrgmobile.data.local.entity.JobEntity

@Dao
interface JobDao {

    @Query(
        """
        SELECT * FROM jobs
        WHERE jobDate BETWEEN :startMillis AND :endMillis
        ORDER BY jobDate ASC, jobStartHour ASC
        """
    )
    suspend fun getScheduledJobs(
        startMillis: Long,
        endMillis: Long
    ): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): JobEntity?

    @Upsert
    suspend fun upsertJobs(jobs: List<JobEntity>)

    @Upsert
    suspend fun upsertJob(job: JobEntity)
}