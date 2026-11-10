package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.AiReportEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DataRowEntity
import com.example.data.model.DatasetEntity
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.data.model.QueryAuditEntity

@Database(
    entities = [
        DatasetEntity::class,
        DataRowEntity::class,
        QualityRunEntity::class,
        QualityIssueEntity::class,
        AiReportEntity::class,
        ChatMessageEntity::class,
        QueryAuditEntity::class,
        Db2ConfigEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun datasetDao(): DatasetDao
    abstract fun dataRowDao(): DataRowDao
    abstract fun qualityRunDao(): QualityRunDao
    abstract fun qualityIssueDao(): QualityIssueDao
    abstract fun aiReportDao(): AiReportDao
    abstract fun chatHistoryDao(): ChatHistoryDao
    abstract fun queryAuditDao(): QueryAuditDao
    abstract fun db2ConfigDao(): Db2ConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "datasentry_ai_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
