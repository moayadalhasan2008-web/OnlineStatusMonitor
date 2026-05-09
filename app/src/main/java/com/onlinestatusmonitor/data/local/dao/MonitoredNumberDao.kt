package com.onlinestatusmonitor.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MonitoredNumberDao {

    @Query("SELECT * FROM monitored_numbers ORDER BY id DESC")
    fun observeAll(): Flow<List<MonitoredNumberEntity>>

    @Query("SELECT * FROM monitored_numbers ORDER BY id DESC")
    suspend fun getAllOnce(): List<MonitoredNumberEntity>

    @Query("SELECT * FROM monitored_numbers WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): MonitoredNumberEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: MonitoredNumberEntity): Long

    @Update
    suspend fun update(entity: MonitoredNumberEntity)

    @Delete
    suspend fun delete(entity: MonitoredNumberEntity)

    @Query("UPDATE monitored_numbers SET isMonitoring = :enabled")
    suspend fun setMonitoringForAll(enabled: Boolean)
}

