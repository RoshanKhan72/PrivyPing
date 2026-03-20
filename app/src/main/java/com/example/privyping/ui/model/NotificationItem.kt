package com.example.privyping.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.privyping.ui.analysis.RiskLevel

@Entity(tableName = "notifications")
data class NotificationItem(

    // 🔴 IMMUTABLE ID — NEVER CHANGE AFTER CREATION
    @PrimaryKey
    val id: String,

    val appName: String,
    val packageName: String,
    val senderName: String,
    val message: String,
    val timestamp: Long,

    // Analysis fields (updated later, same ID)
    val riskLevel: RiskLevel = RiskLevel.LOW,
    val confidence: Int = 0,
    val summary: String = "",
    val isDeleted: Boolean = false // 👈 NEW
)
