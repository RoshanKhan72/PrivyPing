package com.example.privyping.ui.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.privyping.ui.model.NotificationItem
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: NotificationItem)

    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    suspend fun getAllOnce(): List<NotificationItem>

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun deleteById(id: String)
    @Query("DELETE FROM notifications")
    suspend fun clearAll()
    @Query("UPDATE notifications SET isDeleted = :deleted WHERE id = :id")
    suspend fun updateIsDeleted(id: String, deleted: Boolean)



}

