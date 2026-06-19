package com.slachdevm.mrrgmobile.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncEntity
import com.slachdevm.mrrgmobile.data.local.entity.PendingSyncType

@Dao
interface PendingSyncDao {

    @Query("SELECT * FROM pending_sync ORDER BY createdAt ASC")
    suspend fun getPendingItems(): List<PendingSyncEntity>

    @Upsert
    suspend fun upsertPendingItem(item: PendingSyncEntity)

    @Query("DELETE FROM pending_sync WHERE id = :id")
    suspend fun deletePendingItem(id: Long)

    @Query("SELECT COUNT(*) FROM pending_sync")
    suspend fun countPendingItems(): Int

    @Query(
        """
    SELECT * FROM pending_sync
    WHERE type = :type AND entityId = :entityId
    LIMIT 1
    """
    )
    suspend fun getPendingItemByTypeAndEntityId(
        type: PendingSyncType,
        entityId: Long
    ): PendingSyncEntity?
}