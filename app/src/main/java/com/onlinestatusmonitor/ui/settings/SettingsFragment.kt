package com.onlinestatusmonitor.ui.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.onlinestatusmonitor.R
import com.onlinestatusmonitor.core.di.ServiceLocator
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // Bind Preference UI directly to DataStore so the UI always reflects persisted values.
        preferenceManager.preferenceDataStore = object : PreferenceDataStore() {
            override fun getBoolean(key: String?, defValue: Boolean): Boolean {
                if (key == null) return defValue
                val snapshot = runBlocking { ServiceLocator.settingsRepository.getSnapshot() }
                return when (key) {
                    "pref_dark_mode" -> snapshot.darkMode
                    "pref_notifications" -> snapshot.notificationsEnabled
                    "pref_alarm" -> snapshot.alarmEnabled
                    "pref_vibration" -> snapshot.vibrationEnabled
                    else -> defValue
                }
            }

            override fun putBoolean(key: String?, value: Boolean) {
                if (key == null) return
                runBlocking {
                    when (key) {
                        "pref_dark_mode" -> ServiceLocator.settingsRepository.setDarkMode(value)
                        "pref_notifications" -> ServiceLocator.settingsRepository.setNotifications(value)
                        "pref_alarm" -> ServiceLocator.settingsRepository.setAlarm(value)
                        "pref_vibration" -> ServiceLocator.settingsRepository.setVibration(value)
                    }
                }
            }
        }

        setPreferencesFromResource(R.xml.preferences, rootKey)

        val prefDark = findPreference<SwitchPreferenceCompat>("pref_dark_mode")

        prefDark?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            lifecycleScope.launch {
                AppCompatDelegate.setDefaultNightMode(
                    if (enabled) AppCompatDelegate.MODE_NIGHT_YES
                    else AppCompatDelegate.MODE_NIGHT_NO
                )
            }
            true
        }
    }
}
