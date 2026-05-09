package com.onlinestatusmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Timeline of monitoring events.
 *
 * Examples:
 * - "Started monitoring"
 * - "Went ONLINE"
 * - "Went OFFLINE"
 */
@Entity(
    tableName = "activity_log",
    indices = [Index(value = ["numberId", "timestampEpochMs"])]
)
data class ActivityLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val numberId: Long,
    val timestampEpochMs: Long,
    val message: String
)

