package com.onlinestatusmonitor.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager.IMPORTANCE_DEFAULT
import android.app.NotificationManager.IMPORTANCE_HIGH
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.onlinestatusmonitor.R
import com.onlinestatusmonitor.data.settings.AppSettings
import com.onlinestatusmonitor.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * App-level notification + alert helper.
 *
 * IMPORTANT:
 * The class is named "NotificationManager" per the user's requested structure.
 * To avoid clashing with android.app.NotificationManager, we use fully qualified names where needed.
 */
class NotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_MONITORING_ID = "monitoring"
        const val CHANNEL_ALERTS_ID = "alerts"

        const val NOTIF_ID_FOREGROUND = 1001
        const val NOTIF_ID_ALERT = 2001
    }

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    fun ensureChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val monitoring = NotificationChannel(
            CHANNEL_MONITORING_ID,
            context.getString(R.string.notif_channel_monitoring),
            IMPORTANCE_DEFAULT
        ).apply {
            description = "Foreground monitoring status"
            setShowBadge(false)
        }

        val alerts = NotificationChannel(
            CHANNEL_ALERTS_ID,
            context.getString(R.string.notif_channel_alerts),
            IMPORTANCE_HIGH
        ).apply {
            description = "Alerts when a monitored number becomes online"
            enableVibration(true)
        }

        val sys = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        sys.createNotificationChannel(monitoring)
        sys.createNotificationChannel(alerts)
    }

    fun buildForegroundNotification(): android.app.Notification {
        val pi = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_MONITORING_ID)
            .setSmallIcon(android.R.drawable.presence_online)
            .setContentTitle(context.getString(R.string.notif_monitoring_title))
            .setContentText(context.getString(R.string.notif_monitoring_text))
            .setContentIntent(pi)
            .setOngoing(true)
            .build()
    }

    fun showOnlineAlert(
        numberDisplay: String,
        settings: AppSettings
    ) {
        if (!settings.notificationsEnabled) return

        val pi = PendingIntent.getActivity(
            context,
            1,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val title = context.getString(R.string.notif_online_title, numberDisplay)
        val notification = NotificationCompat.Builder(context, CHANNEL_ALERTS_ID)
            .setSmallIcon(android.R.drawable.presence_away)
            .setContentTitle(title)
            .setContentText(context.getString(R.string.notif_online_text))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pi)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIF_ID_ALERT, notification)

        if (settings.vibrationEnabled) vibratePattern()
        if (settings.alarmEnabled) playAlarmForShortTime()
    }

    private fun vibratePattern() {
        val timings = longArrayOf(0, 250, 120, 250, 120, 500)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createWaveform(
                timings,
                intArrayOf(0, 255, 0, 255, 0, 255),
                -1
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(effect)
            }
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator).vibrate(timings, -1)
        }
    }

    private fun playAlarmForShortTime() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val ringtone = RingtoneManager.getRingtone(context, uri) ?: return
        ringtone.play()

        // Stop after a short burst so it doesn't get "stuck" in the simulation.
        scope.launch {
            delay(4_000)
            try {
                ringtone.stop()
            } catch (_: Throwable) {
            }
        }
    }
}
