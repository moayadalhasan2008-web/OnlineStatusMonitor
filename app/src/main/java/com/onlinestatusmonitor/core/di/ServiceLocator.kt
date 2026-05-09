package com.onlinestatusmonitor.core.di

import android.content.Context
import com.onlinestatusmonitor.data.local.LocalDatabase
import com.onlinestatusmonitor.data.repository.MonitorRepository
import com.onlinestatusmonitor.data.settings.SettingsRepository

/**
 * Simple service locator to keep the project self-contained and easy to run.
 * In production, replace with Hilt/Dagger.
 */
object ServiceLocator {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    val database: LocalDatabase by lazy {
        LocalDatabase.build(appContext)
    }

    val settingsRepository: SettingsRepository by lazy {
        SettingsRepository(appContext)
    }

    val monitorRepository: MonitorRepository by lazy {
        MonitorRepository(
            numberDao = database.monitoredNumberDao(),
            logDao = database.activityLogDao()
        )
    }
}

