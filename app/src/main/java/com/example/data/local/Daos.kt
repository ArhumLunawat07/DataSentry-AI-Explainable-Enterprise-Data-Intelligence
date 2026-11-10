package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.AiReportEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DataRowEntity
import com.example.data.model.DatasetEntity
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.data.model.QueryAuditEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DatasetDao {
    @Query("SELECT * FROM datasets ORDER BY createdAt DESC")
    fun getAllDatasetsFlow(): Flow<List<DatasetEntity>>

    @Query("SELECT * FROM datasets ORDER BY createdAt DESC")
    suspend fun getAllDatasets(): List<DatasetEntity>

    @Query("SELECT * FROM datasets WHERE id = :id LIMIT 1")
    suspend fun getDatasetById(id: String): DatasetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDataset(dataset: DatasetEntity)

    @Query("DELETE FROM datasets WHERE id = :id")
    suspend fun deleteDataset(id: String)

    @Query("DELETE FROM data_rows WHERE datasetId = :datasetId")
    suspend fun deleteDataRows(datasetId: String)

    @Query("DELETE FROM quality_runs WHERE datasetId = :datasetId")
    suspend fun deleteQualityRuns(datasetId: String)

    @Query("DELETE FROM quality_issues WHERE datasetId = :datasetId")
    suspend fun deleteQualityIssues(datasetId: String)

    @Query("DELETE FROM ai_reports WHERE datasetId = :datasetId")
    suspend fun deleteAiReports(datasetId: String)

    @Transaction
    suspend fun deleteDatasetCascade(id: String) {
        deleteDataset(id)
        deleteDataRows(id)
        deleteQualityRuns(id)
        deleteQualityIssues(id)
        deleteAiReports(id)
    }

    @Query("SELECT COUNT(*) FROM datasets")
    suspend fun getDatasetCount(): Int

    @Query("SELECT SUM(rowCount) FROM datasets")
    suspend fun getTotalRecordCount(): Int?
}

@Dao
interface DataRowDao {
    @Query("SELECT * FROM data_rows WHERE datasetId = :datasetId ORDER BY rowIndex ASC LIMIT :limit OFFSET :offset")
    suspend fun getRowsForDataset(datasetId: String, limit: Int = 100, offset: Int = 0): List<DataRowEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRows(rows: List<DataRowEntity>)

    @Query("SELECT COUNT(*) FROM data_rows WHERE datasetId = :datasetId")
    suspend fun getRowCount(datasetId: String): Int
}

@Dao
interface QualityRunDao {
    @Query("SELECT * FROM quality_runs ORDER BY runTimestamp DESC")
    fun getAllRunsFlow(): Flow<List<QualityRunEntity>>

    @Query("SELECT * FROM quality_runs WHERE datasetId = :datasetId ORDER BY runTimestamp DESC LIMIT 1")
    suspend fun getLatestRunForDataset(datasetId: String): QualityRunEntity?

    @Query("SELECT * FROM quality_runs WHERE id = :runId LIMIT 1")
    suspend fun getRunById(runId: String): QualityRunEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRun(run: QualityRunEntity)

    @Query("SELECT AVG(overallScore) FROM quality_runs")
    suspend fun getAverageQualityScore(): Double?
}

@Dao
interface QualityIssueDao {
    @Query("SELECT * FROM quality_issues ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END")
    fun getAllIssuesFlow(): Flow<List<QualityIssueEntity>>

    @Query("SELECT * FROM quality_issues WHERE datasetId = :datasetId ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END")
    suspend fun getIssuesForDataset(datasetId: String): List<QualityIssueEntity>

    @Query("SELECT * FROM quality_issues WHERE runId = :runId ORDER BY CASE severity WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 WHEN 'MEDIUM' THEN 3 ELSE 4 END")
    suspend fun getIssuesForRun(runId: String): List<QualityIssueEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIssues(issues: List<QualityIssueEntity>)

    @Query("SELECT COUNT(*) FROM quality_issues WHERE severity = 'CRITICAL'")
    suspend fun getCriticalIssueCount(): Int
}

@Dao
interface AiReportDao {
    @Query("SELECT * FROM ai_reports ORDER BY generatedAt DESC")
    fun getAllReportsFlow(): Flow<List<AiReportEntity>>

    @Query("SELECT * FROM ai_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): AiReportEntity?

    @Query("SELECT * FROM ai_reports WHERE datasetId = :datasetId ORDER BY generatedAt DESC LIMIT 1")
    suspend fun getLatestReportForDataset(datasetId: String): AiReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: AiReportEntity)

    @Query("DELETE FROM ai_reports WHERE id = :id")
    suspend fun deleteReport(id: String)
}

@Dao
interface ChatHistoryDao {
    @Query("SELECT * FROM chat_history ORDER BY timestamp ASC")
    fun getAllMessagesFlow(): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity)

    @Query("DELETE FROM chat_history")
    suspend fun clearHistory()
}

@Dao
interface QueryAuditDao {
    @Query("SELECT * FROM query_audit ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentAuditsFlow(limit: Int = 100): Flow<List<QueryAuditEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: QueryAuditEntity)

    @Query("DELETE FROM query_audit")
    suspend fun clearAudits()
}

@Dao
interface Db2ConfigDao {
    @Query("SELECT * FROM db2_config WHERE id = 1 LIMIT 1")
    suspend fun getConfig(): Db2ConfigEntity?

    @Query("SELECT * FROM db2_config WHERE id = 1 LIMIT 1")
    fun getConfigFlow(): Flow<Db2ConfigEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveConfig(config: Db2ConfigEntity)
}
