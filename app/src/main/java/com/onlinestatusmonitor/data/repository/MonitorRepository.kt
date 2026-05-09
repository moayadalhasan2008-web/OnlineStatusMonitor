package com.onlinestatusmonitor.data.repository

import com.onlinestatusmonitor.data.local.dao.ActivityLogDao
import com.onlinestatusmonitor.data.local.dao.MonitoredNumberDao
import com.onlinestatusmonitor.data.local.entity.ActivityLogEntity
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for monitoring state.
 */
class MonitorRepository(
    private val numberDao: MonitoredNumberDao,
    private val logDao: ActivityLogDao
) {
    fun observeNumbers(): Flow<List<MonitoredNumberEntity>> = numberDao.observeAll()

    fun observeLatestLogs(limit: Int = 200): Flow<List<ActivityLogEntity>> = logDao.observeLatest(limit)

    fun observeLogsForNumber(numberId: Long, limit: Int = 200): Flow<List<ActivityLogEntity>> =
        logDao.observeLatestForNumber(numberId, limit)

    suspend fun addNumber(countryCode: String, phoneNumber: String): Long {
        val entity = MonitoredNumberEntity(
            countryCode = countryCode,
            phoneNumber = phoneNumber.trim(),
            isMonitoring = true,
            isOnline = false,
            lastStatusChangeEpochMs = System.currentTimeMillis(),
            lastActiveEpochMs = null,
            totalOnlineMs = 0,
            totalOfflineMs = 0
        )
        val id = numberDao.insert(entity)
        logDao.insert(
            ActivityLogEntity(
                numberId = id,
                timestampEpochMs = System.currentTimeMillis(),
                message = "Added number: ${entity.display()}"
            )
        )
        return id
    }

    suspend fun setMonitoringEnabled(enabled: Boolean) {
        numberDao.setMonitoringForAll(enabled)
    }

    suspend fun getNumbersOnce(): List<MonitoredNumberEntity> = numberDao.getAllOnce()

    suspend fun updateNumber(entity: MonitoredNumberEntity) = numberDao.update(entity)

    suspend fun log(numberId: Long, message: String) {
        logDao.insert(
            ActivityLogEntity(
                numberId = numberId,
                timestampEpochMs = System.currentTimeMillis(),
                message = message
            )
        )
    }
}

