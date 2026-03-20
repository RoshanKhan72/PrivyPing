package com.example.privyping.ui.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.privyping.ui.model.NotificationItem

@Database(
    entities = [NotificationItem::class],
    version = 2,
    exportSchema = false
)
abstract class PrivyDatabase : RoomDatabase() {

    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var INSTANCE: PrivyDatabase? = null

        fun getInstance(context: Context): PrivyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    PrivyDatabase::class.java,
                    "privyping.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
    }
}
