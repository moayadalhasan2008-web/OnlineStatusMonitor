package com.onlinestatusmonitor.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.content.ContextCompat
import com.onlinestatusmonitor.core.di.ServiceLocator
import com.onlinestatusmonitor.core.notifications.NotificationManager
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Foreground service that simulates real-time presence changes.
 *
 * Why simulation?
 * True "online status" monitoring requires an authorized server/API for the target platform.
 * This service provides a production-ready architecture that you can later connect to real APIs.
 */
class MonitorService : Service() {

    companion object {
        private const val ACTION_START = "com.onlinestatusmonitor.action.START"
        private const val ACTION_STOP = "com.onlinestatusmonitor.action.STOP"

        fun start(context: Context) {
            val intent = Intent(context, MonitorService::class.java).setAction(ACTION_START)
            ContextCompat.startForegroundService(context, intent)
        }

        fun stop(context: Context) {
            val intent = Intent(context, MonitorService::class.java).setAction(ACTION_STOP)
            context.startService(intent)
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    private var loopJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startMonitoring()
            ACTION_STOP -> stopMonitoring()
        }
        return START_STICKY
    }

    private fun startMonitoring() {
        if (loopJob?.isActive == true) return

        val notif = NotificationManager(this).apply { ensureChannels() }.buildForegroundNotification()
        startForeground(NotificationManager.NOTIF_ID_FOREGROUND, notif)

        loopJob = scope.launch {
            ServiceLocator.monitorRepository.setMonitoringEnabled(true)
            ServiceLocator.monitorRepository.log(
                numberId = 0,
                message = "Monitoring started"
            )

            // Main loop: every few seconds, pick a monitored number and flip its status.
            while (isActive) {
                delay(3_500) // tuned for responsive feel on Samsung S22 Ultra

                val candidates = ServiceLocator.monitorRepository.getNumbersOnce()
                    .filter { it.isMonitoring }

                if (candidates.isEmpty()) continue

                val chosen = candidates.random()

                // 35% chance to change state each tick (keeps it lively without spamming).
                if (Random.nextFloat() < 0.35f) {
                    toggleStatus(chosen)
                }
            }
        }
    }

    private suspend fun toggleStatus(number: MonitoredNumberEntity) {
        val now = System.currentTimeMillis()
        val elapsed = (now - number.lastStatusChangeEpochMs).coerceAtLeast(0)

        val becameOnline = !number.isOnline
        val updated = if (number.isOnline) {
            number.copy(
                isOnline = false,
                lastStatusChangeEpochMs = now,
                lastActiveEpochMs = now,
                totalOnlineMs = number.totalOnlineMs + elapsed
            )
        } else {
            number.copy(
                isOnline = true,
                lastStatusChangeEpochMs = now,
                totalOfflineMs = number.totalOfflineMs + elapsed
            )
        }

        ServiceLocator.monitorRepository.updateNumber(updated)

        val msg = if (becameOnline) "Went ONLINE: ${updated.display()}" else "Went OFFLINE: ${updated.display()}"
        ServiceLocator.monitorRepository.log(updated.id, msg)

        if (becameOnline) {
            val settings = ServiceLocator.settingsRepository.getSnapshot()
            NotificationManager(this).showOnlineAlert(updated.display(), settings)
        }
    }

    private fun stopMonitoring() {
        loopJob?.cancel()
        loopJob = null

        scope.launch {
            ServiceLocator.monitorRepository.setMonitoringEnabled(false)
            ServiceLocator.monitorRepository.log(
                numberId = 0,
                message = "Monitoring stopped"
            )
        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        loopJob?.cancel()
        super.onDestroy()
    }
}
