package com.example.privyping.ui.repository

import android.content.Context
import com.example.privyping.ui.model.NotificationItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

object NotificationRepository {

    private val _notifications =
        MutableStateFlow<List<NotificationItem>>(emptyList())

    val notifications: StateFlow<List<NotificationItem>> = _notifications

    // ✅ Add single notification (in-memory only)
    fun addNotification(item: NotificationItem) {
        _notifications.update { listOf(item) + it }
    }

    // ✅ Add single notification + persist to Room
    fun addNotification(context: Context, item: NotificationItem) {
        _notifications.update { listOf(item) + it }

        CoroutineScope(Dispatchers.IO).launch {
            PrivyDatabase.getInstance(context)
                .notificationDao()
                .insert(item)
        }
    }


    // ✅ Delete single notification (in-memory)
    fun deleteNotification(context: Context, id: String) {
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }

        CoroutineScope(Dispatchers.IO).launch {
            PrivyDatabase.getInstance(context)
                .notificationDao()
                .deleteById(id)
        }
    }



    fun clearAll(context: Context) {
        _notifications.update { emptyList() }

        CoroutineScope(Dispatchers.IO).launch {
            PrivyDatabase.getInstance(context)
                .notificationDao()
                .clearAll()
        }
    }

    // --- Soft delete logic ---

    fun softDelete(id: String) {
        _notifications.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = true) else it
            }
        }
    }

    fun softDeleteBatch(ids: List<String>) {
        _notifications.update { list ->
            list.map {
                if (it.id in ids) it.copy(isDeleted = true) else it
            }
        }
    }

    fun restore(id: String) {
        _notifications.update { list ->
            list.map {
                if (it.id == id) it.copy(isDeleted = false) else it
            }
        }
    }

    fun restoreBatch(ids: List<String>) {
        _notifications.update { list ->
            list.map {
                if (it.id in ids) it.copy(isDeleted = false) else it
            }
        }
    }

    fun hardDelete(context: Context, id: String) {
        // First remove from in-memory list completely
        _notifications.update { list ->
            list.filterNot { it.id == id }
        }
        
        // Then delete from Room
        CoroutineScope(Dispatchers.IO).launch {
            PrivyDatabase.getInstance(context)
                .notificationDao().deleteById(id)
        }
    }

    fun hardDeleteBatch(context: Context, ids: List<String>) {
        _notifications.update { list ->
            list.filterNot { it.id in ids }
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dao = PrivyDatabase.getInstance(context).notificationDao()
            ids.forEach { id ->
                dao.deleteById(id)
            }
        }
    }



    fun deleteByIds(ids: List<String>) {
        _notifications.update { list ->
            list.filterNot { it.id in ids }
        }
    }

    // ✅ Delete SELECTED notifications (selection mode)
    fun deleteSelected(items: Set<NotificationItem>) {
        _notifications.update { list ->
            list.filterNot { it in items }
        }
    }

    // ✅ Update notification by ID (SAFE)
    fun updateNotification(updated: NotificationItem) {
        _notifications.update { list ->
            list.map {
                if (it.id == updated.id) updated else it
            }
        }
    }

    fun refresh() {
        // no-op
        // StateFlow re-emits automatically
    }

    // ✅ Load persisted data from Room INTO StateFlow
    fun loadFromRoom(context: Context) {
        val dao = PrivyDatabase.getInstance(context).notificationDao()

        CoroutineScope(Dispatchers.IO).launch {
            val stored = dao.getAllOnce()
            _notifications.value = stored
        }
    }
}
