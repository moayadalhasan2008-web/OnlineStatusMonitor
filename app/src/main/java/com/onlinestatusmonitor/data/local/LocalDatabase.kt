package com.onlinestatusmonitor.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.onlinestatusmonitor.data.local.dao.ActivityLogDao
import com.onlinestatusmonitor.data.local.dao.MonitoredNumberDao
import com.onlinestatusmonitor.data.local.entity.ActivityLogEntity
import com.onlinestatusmonitor.data.local.entity.MonitoredNumberEntity

/**
 * Local persistence.
 *
 * Stores monitored numbers, activity logs, and statistics (total online/offline time).
 */
@Database(
    entities = [
        MonitoredNumberEntity::class,
        ActivityLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {
    abstract fun monitoredNumberDao(): MonitoredNumberDao
    abstract fun activityLogDao(): ActivityLogDao

    companion object {
        fun build(context: Context): LocalDatabase {
            return Room.databaseBuilder(
                context,
                LocalDatabase::class.java,
                "online_status_monitor.db"
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
