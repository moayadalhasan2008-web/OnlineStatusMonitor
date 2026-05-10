package com.onlinestatusmonitor.service

import android.accessibilityservice.AccessibilityService
import android.media.RingtoneManager
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class WhatsAppAccessibilityService : AccessibilityService() {

    private var lastAlarmTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {

        val root = rootInActiveWindow ?: return

        val texts = mutableListOf<String>()

        scanNode(root, texts)

        val fullText = texts.joinToString(" ")

        val isOnline =
            fullText.contains("online", true) ||
            fullText.contains("typing", true)

        MonitorService.detectedOnline = isOnline

        if (isOnline) {

            MonitorService.lastSeen =
                System.currentTimeMillis()

            if (
                SystemClock.elapsedRealtime() -
                lastAlarmTime > 10000
            ) {

                playAlarm()

                lastAlarmTime =
                    SystemClock.elapsedRealtime()
            }
        }
    }

    private fun playAlarm() {

        try {

            val notification =
                RingtoneManager.getDefaultUri(
                    RingtoneManager.TYPE_NOTIFICATION
                )

            val ringtone =
                RingtoneManager.getRingtone(
                    applicationContext,
                    notification
                )

            ringtone.play()

        } catch (e: Exception) {

            e.printStackTrace()
        }
    }

    private fun scanNode(
        node: AccessibilityNodeInfo?,
        result: MutableList<String>
    ) {

        if (node == null) return

        node.text?.toString()?.let {

            result.add(it)
        }

        for (i in 0 until node.childCount) {

            scanNode(
                node.getChild(i),
                result
            )
        }
    }

    override fun onInterrupt() {
    }
}