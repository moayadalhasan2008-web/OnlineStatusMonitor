package com.onlinestatusmonitor

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.onlinestatusmonitor.core.di.ServiceLocator
import com.onlinestatusmonitor.core.notifications.NotificationManager
import kotlinx.coroutines.runBlocking

/**
 * Application entry point.
 *
 * NOTE:
 * - This project simulates "online status monitoring" (randomized real-time status changes),
 *   because real presence monitoring requires a server/API from the target platform.
 */
class OnlineStatusMonitorApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize app-wide graph.
        ServiceLocator.init(this)

        // Apply Day/Night according to Settings.
        runBlocking {
            val settings = ServiceLocator.settingsRepository.getSnapshot()
            AppCompatDelegate.setDefaultNightMode(
                if (settings.darkMode) AppCompatDelegate.MODE_NIGHT_YES
                else AppCompatDelegate.MODE_NIGHT_NO
            )
        }

        // Create notification channels up-front.
        NotificationManager(this).ensureChannels()
    }
}

