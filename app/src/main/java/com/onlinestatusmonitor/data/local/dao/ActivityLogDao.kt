package com.onlinestatusmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.onlinestatusmonitor.data.local.entity.ActivityLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityLogDao {

    @Query("SELECT * FROM activity_log WHERE numberId = :numberId ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeLatestForNumber(numberId: Long, limit: Int = 200): Flow<List<ActivityLogEntity>>

    @Query("SELECT * FROM activity_log ORDER BY timestampEpochMs DESC LIMIT :limit")
    fun observeLatest(limit: Int = 200): Flow<List<ActivityLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ActivityLogEntity)

    @Query("DELETE FROM activity_log WHERE numberId = :numberId")
    suspend fun deleteForNumber(numberId: Long)
}

