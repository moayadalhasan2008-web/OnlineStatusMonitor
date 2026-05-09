package com.onlinestatusmonitor.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A phone number being monitored.
 *
 * Stats fields are accumulated locally:
 * - totalOnlineMs/totalOfflineMs: sum of durations.
 * - lastStatusChangeEpochMs: last time the "isOnline" state changed.
 * - lastActiveEpochMs: last time the user was known to be online (updated when they go offline).
 */
@Entity(
    tableName = "monitored_numbers",
    indices = [Index(value = ["countryCode", "phoneNumber"], unique = true)]
)
data class MonitoredNumberEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val countryCode: String,
    val phoneNumber: String,

    val isOnline: Boolean = false,
    val isMonitoring: Boolean = true,

    val lastStatusChangeEpochMs: Long = System.currentTimeMillis(),
    val lastActiveEpochMs: Long? = null,

    val totalOnlineMs: Long = 0,
    val totalOfflineMs: Long = 0
) {
    fun display(): String = "+$countryCode $phoneNumber"
}

