package com.example.privyping.ui.utils

import android.content.Context
import android.provider.Settings

fun isNotificationAccessGranted(context: Context): Boolean {
    val enabledListeners = Settings.Secure.getString(
        context.contentResolver,
        "enabled_notification_listeners"
    ) ?: ""

    return enabledListeners.contains(context.packageName)
}
