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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MonitorService : Service() {

    companion object {

        private const val ACTION_START =
            "com.onlinestatusmonitor.action.START"

        private const val ACTION_STOP =
            "com.onlinestatusmonitor.action.STOP"

        // يتم تغييره من خدمة الواتساب
        var detectedOnline = false

        // آخر ظهور
        var lastSeen = 0L

        fun start(context: Context) {

            val intent =
                Intent(
                    context,
                    MonitorService::class.java
                ).setAction(ACTION_START)

            ContextCompat.startForegroundService(
                context,
                intent
            )
        }

        fun stop(context: Context) {

            val intent =
                Intent(
                    context,
                    MonitorService::class.java
                ).setAction(ACTION_STOP)

            context.startService(intent)
        }
    }

    private val scope =
        CoroutineScope(
            SupervisorJob() + Dispatchers.Default
        )

    private var loopJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {

            ACTION_START -> startMonitoring()

            ACTION_STOP -> stopMonitoring()
        }

        return START_STICKY
    }

    private fun startMonitoring() {

        if (loopJob?.isActive == true) return

        val notif =
            NotificationManager(this)
                .apply { ensureChannels() }
                .buildForegroundNotification()

        startForeground(
            NotificationManager.NOTIF_ID_FOREGROUND,
            notif
        )

        loopJob = scope.launch {

            ServiceLocator.monitorRepository
                .setMonitoringEnabled(true)

            ServiceLocator.monitorRepository.log(
                numberId = 0,
                message = "Monitoring started"
            )

            while (isActive) {

                delay(2000)

                val candidates =
                    ServiceLocator.monitorRepository
                        .getNumbersOnce()
                        .filter { it.isMonitoring }

                if (candidates.isEmpty()) continue

                val chosen = candidates.random()

                // استخدام الحالة الحقيقية القادمة من واتساب
                if (
                    detectedOnline != chosen.isOnline
                ) {

                    toggleStatus(
                        chosen,
                        detectedOnline
                    )
                }
            }
        }
    }

    private suspend fun toggleStatus(
        number: MonitoredNumberEntity,
        online: Boolean
    ) {

        val now =
            System.currentTimeMillis()

        val elapsed =
            (
                now -
                number.lastStatusChangeEpochMs
            ).coerceAtLeast(0)

        val updated =
            if (online) {

                number.copy(
                    isOnline = true,
                    lastStatusChangeEpochMs = now,
                    totalOfflineMs =
                        number.totalOfflineMs + elapsed
                )

            } else {

                number.copy(
                    isOnline = false,
                    lastStatusChangeEpochMs = now,
                    lastActiveEpochMs = now,
                    totalOnlineMs =
                        number.totalOnlineMs + elapsed
                )
            }

        ServiceLocator.monitorRepository
            .updateNumber(updated)

        val msg =
            if (online)
                "ONLINE: ${updated.display()}"
            else
                "OFFLINE: ${updated.display()}"

        ServiceLocator.monitorRepository
            .log(updated.id, msg)

        if (online) {

            val settings =
                ServiceLocator.settingsRepository
                    .getSnapshot()

            NotificationManager(this)
                .showOnlineAlert(
                    updated.display(),
                    settings
                )
        }
    }

    private fun stopMonitoring() {

        loopJob?.cancel()

        loopJob = null

        scope.launch {

            ServiceLocator.monitorRepository
                .setMonitoringEnabled(false)

            ServiceLocator.monitorRepository.log(
                numberId = 0,
                message = "Monitoring stopped"
            )
        }

        stopForeground(
            STOP_FOREGROUND_REMOVE
        )

        stopSelf()
    }

    override fun onDestroy() {

        loopJob?.cancel()

        super.onDestroy()
    }
}