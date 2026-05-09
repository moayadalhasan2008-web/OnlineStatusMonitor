package com.onlinestatusmonitor.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

object Formatters {
    private val dt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    fun formatDateTime(epochMs: Long): String = dt.format(Date(epochMs))

    fun formatDuration(ms: Long): String {
        val seconds = TimeUnit.MILLISECONDS.toSeconds(ms)
        val h = seconds / 3600
        val m = (seconds % 3600) / 60
        val s = seconds % 60
        return "%02dh %02dm %02ds".format(h, m, s)
    }
}

