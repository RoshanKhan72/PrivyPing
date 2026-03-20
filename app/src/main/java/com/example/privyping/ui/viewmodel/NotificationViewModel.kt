package com.example.privyping.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.privyping.ui.analysis.AnalysisEngine
import com.example.privyping.ui.model.NotificationItem
import com.example.privyping.ui.repository.NotificationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class NotificationViewModel(
    application: Application
) : AndroidViewModel(application) {

    // ✅ Directly expose flow from repository to avoid infinite loops and redundant analysis
    val notifications: StateFlow<List<NotificationItem>> = NotificationRepository.notifications

    init {
        NotificationRepository.loadFromRoom(application.applicationContext)
    }

    /**
     * Re-load all notifications from the database to sync the UI
     */
    fun refreshNotifications(onComplete: (Int) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val oldSize = notifications.value.size
            NotificationRepository.loadFromRoom(getApplication())
            // Brief delay to allow the StateFlow to update if needed, 
            // though loadFromRoom handles the update internally.
            val newSize = NotificationRepository.notifications.value.size
            val addedCount = newSize - oldSize
            onComplete(if (addedCount > 0) addedCount else 0)
        }
    }

    /**
     * Manual re-analysis triggered by user
     */
    fun reAnalyze(item: NotificationItem) {
        viewModelScope.launch(Dispatchers.Default) {
            val result = AnalysisEngine.analyze(item)

            val updated = item.copy(
                riskLevel = result.riskLevel,
                confidence = result.confidence,
                summary = result.summary
            )

            NotificationRepository.updateNotification(updated)
        }
    }

    fun clearAllNotifications() {
        NotificationRepository.clearAll(getApplication())
    }

    fun deleteSelected(ids: List<String>) {
        ids.forEach { id ->
            NotificationRepository.deleteNotification(
                getApplication(),
                id
            )
        }
    }

    fun deleteNotification(item: NotificationItem) {
        NotificationRepository.deleteNotification(
            getApplication(),
            item.id
        )
    }

    // --- Soft delete functions ---

    fun softDelete(item: NotificationItem) {
        NotificationRepository.softDelete(item.id)
    }

    fun softDeleteBatch(items: List<NotificationItem>) {
        NotificationRepository.softDeleteBatch(items.map { it.id })
    }

    fun restoreNotification(item: NotificationItem) {
        NotificationRepository.restore(item.id)
    }

    fun restoreBatch(items: List<NotificationItem>) {
        NotificationRepository.restoreBatch(items.map { it.id })
    }

    fun hardDeleteNotification(item: NotificationItem) {
        NotificationRepository.hardDelete(getApplication(), item.id)
    }

    fun hardDeleteBatch(items: List<NotificationItem>) {
        NotificationRepository.hardDeleteBatch(getApplication(), items.map { it.id })
    }

    // ---- utility ----

    private var lastKnownCount = 0

    fun checkForNewNotifications(
        onNoNew: () -> Unit
    ) {
        val currentCount = notifications.value.size

        if (currentCount == lastKnownCount) {
            onNoNew()
        } else {
            lastKnownCount = currentCount
        }
    }
}
