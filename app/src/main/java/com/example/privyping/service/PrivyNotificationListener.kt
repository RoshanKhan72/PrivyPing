package com.example.privyping.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.example.privyping.R
import com.example.privyping.ui.analysis.AnalysisEngine
import com.example.privyping.ui.analysis.RiskLevel
import com.example.privyping.ui.model.NotificationItem
import com.example.privyping.ui.repository.NotificationRepository

class PrivyNotificationListener : NotificationListenerService() {

    override fun onListenerConnected() {
        android.util.Log.d("PrivyPing", "LISTENER CONNECTED ✅")
    }

    // 🔹 Track active calls
    private val activeCalls = mutableSetOf<String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        android.util.Log.e("PrivyPing_DEBUG", "onNotificationPosted CALLED")

        // 🚫 Prevent infinite loop
        if (sbn.packageName == packageName) return

        val notification = sbn.notification ?: return
        android.util.Log.d("PrivyPing", "POSTED: ${sbn.packageName}")

        if (notification.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val extras = notification.extras ?: return

        val messageText =
            extras.getCharSequence("android.bigText")?.toString()
                ?: extras.getCharSequence("android.text")?.toString()
                ?: extras.getCharSequenceArray("android.textLines")?.joinToString("\n")
                ?: return

        val senderName =
            extras.getCharSequence("android.subText")?.toString()
                ?: extras.getCharSequence("android.title")?.toString()
                ?: "Unknown"

        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        val isCall =
            notification.category == Notification.CATEGORY_CALL ||
                    sbn.packageName == "com.android.dialer"

        if (isCall) {
            val callKey = "$appName|$senderName"
            if (!activeCalls.add(callKey)) return
        }

        // 📦 Create notification item
        val item = NotificationItem(
            id = "${sbn.key}_${sbn.postTime}",
            appName = appName,
            packageName = sbn.packageName,
            senderName = senderName,
            message = messageText,
            timestamp = sbn.postTime
        )

        // 🧠 Analyze risk
        val result = AnalysisEngine.analyze(item)

        val analyzedItem = item.copy(
            riskLevel = result.riskLevel,
            confidence = result.confidence,
            summary = result.summary
        )

        // 💾 Save locally
        NotificationRepository.addNotification(
            applicationContext,
            analyzedItem
        )

        // 🚨 Show system notification (MEDIUM / HIGH only)
        if (result.riskLevel == RiskLevel.MEDIUM || result.riskLevel == RiskLevel.HIGH) {
            showRiskNotification(
                result.riskLevel,
                appName,
                analyzedItem.message
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        if (notification.category != Notification.CATEGORY_CALL) return

        val extras = notification.extras ?: return
        val sender =
            extras.getCharSequence("android.subText")?.toString()
                ?: extras.getCharSequence("android.title")?.toString()
                ?: return

        val pm = applicationContext.packageManager
        val appName = try {
            val appInfo = pm.getApplicationInfo(sbn.packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: Exception) {
            sbn.packageName
        }

        activeCalls.remove("$appName|$sender")
    }

    // 🔔 SYSTEM NOTIFICATION (COLOR + PRECAUTIONS)
    private fun showRiskNotification(
        risk: RiskLevel,
        appName: String,
        message: String
    ) {
        val manager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "privyping_risk_alerts"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Risk Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val titleText = when (risk) {
            RiskLevel.HIGH -> "High Risk Detected"
            RiskLevel.MEDIUM -> "Medium Risk Detected"
            else -> return
        }

        val coloredTitle = SpannableString(titleText).apply {
            val color = when (risk) {
                RiskLevel.HIGH -> Color.RED
                RiskLevel.MEDIUM -> Color.YELLOW
                else -> Color.WHITE
            }
            setSpan(
                ForegroundColorSpan(color),
                0,
                titleText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        val safeMessage = message
            .replace("\n", " ")
            .take(120)

        val precautions = when (risk) {
            RiskLevel.MEDIUM -> """
                • Avoid clicking links immediately
                • Verify the sender carefully
                • Do not share personal information
            """.trimIndent()

            RiskLevel.HIGH -> """
                • DO NOT click any links
                • DO NOT share OTP, PIN, or passwords
                • This message may be a scam
                • Verify only via official app or website
            """.trimIndent()

            else -> ""
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(coloredTitle)
            .setContentText("$appName: $safeMessage")
            .setStyle(
                NotificationCompat.BigTextStyle().bigText(
                    "$appName\n\n$safeMessage\n\nPrecautions:\n$precautions"
                )
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(System.currentTimeMillis().toInt(), notification)
    }
}
