package com.onlinestatusmonitor.data.settings

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException

data class AppSettings(
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val alarmEnabled: Boolean,
    val vibrationEnabled: Boolean
)

/**
 * Settings persistence backed by DataStore.
 */
class SettingsRepository(context: Context) {
    private val dataStore = PreferenceDataStoreFactory.create(
        produceFile = { context.preferencesDataStoreFile("settings") }
    )

    private object Keys {
        val DARK_MODE = booleanPreferencesKey("pref_dark_mode")
        val NOTIFICATIONS = booleanPreferencesKey("pref_notifications")
        val ALARM = booleanPreferencesKey("pref_alarm")
        val VIBRATION = booleanPreferencesKey("pref_vibration")
    }

    val settingsFlow: Flow<AppSettings> = dataStore.data
        .catch { ex ->
            if (ex is IOException) emit(emptyPreferences()) else throw ex
        }
        .map { prefs ->
            AppSettings(
                darkMode = prefs[Keys.DARK_MODE] ?: true,
                notificationsEnabled = prefs[Keys.NOTIFICATIONS] ?: true,
                alarmEnabled = prefs[Keys.ALARM] ?: true,
                vibrationEnabled = prefs[Keys.VIBRATION] ?: true
            )
        }

    suspend fun getSnapshot(): AppSettings = settingsFlow.first()

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { it[Keys.DARK_MODE] = enabled }
    }

    suspend fun setNotifications(enabled: Boolean) {
        dataStore.edit { it[Keys.NOTIFICATIONS] = enabled }
    }

    suspend fun setAlarm(enabled: Boolean) {
        dataStore.edit { it[Keys.ALARM] = enabled }
    }

    suspend fun setVibration(enabled: Boolean) {
        dataStore.edit { it[Keys.VIBRATION] = enabled }
    }
}

