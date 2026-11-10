package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "datasets")
data class DatasetEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val rowCount: Int,
    val columnCount: Int,
    val columnsJson: String, // serialized List<ColumnInfo>
    val fileSizeFormatted: String,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val isDemo: Boolean = false,
    val db2TableName: String = ""
)

@Entity(tableName = "data_rows")
data class DataRowEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val datasetId: String,
    val rowIndex: Int,
    val rowDataJson: String // Map<String, String> as JSON
)

@Entity(tableName = "quality_runs")
data class QualityRunEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val datasetId: String,
    val overallScore: Int, // 0 - 100
    val missingValuesCount: Int,
    val duplicateCount: Int,
    val formatErrorsCount: Int,
    val anomalyCount: Int,
    val status: String = "COMPLETED",
    val runTimestamp: Long = System.currentTimeMillis(),
    val executionTimeMs: Long = 0
)

@Entity(tableName = "quality_issues")
data class QualityIssueEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val runId: String,
    val datasetId: String,
    val issueType: String, // MISSING_VALUE, DUPLICATE, FORMAT_ERROR, STATISTICAL_ANOMALY, TYPE_MISMATCH
    val severity: String, // CRITICAL, HIGH, MEDIUM, LOW
    val columnName: String,
    val affectedRecordsCount: Int,
    val description: String,
    val sampleValuesJson: String,
    val rootCauseHypothesis: String,
    val businessImpact: String,
    val suggestedRemediation: String,
    val remediationSql: String = "",
    val confidenceScore: Double = 0.95, // 0.0 - 1.0
    val evidenceSnippet: String = "",
    val isAiGeneratedHypothesis: Boolean = true
)

@Entity(tableName = "ai_reports")
data class AiReportEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val datasetId: String,
    val datasetName: String,
    val title: String,
    val executiveSummary: String,
    val qualityScore: Int,
    val statsJson: String,
    val keyFindingsJson: String,
    val recommendationsJson: String,
    val businessImpact: String,
    val generatedAt: Long = System.currentTimeMillis(),
    val reportFormat: String = "EXECUTIVE"
)

@Entity(tableName = "chat_history")
data class ChatMessageEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "assistant"
    val content: String,
    val toolCallsJson: String = "[]",
    val sqlExecuted: String = "",
    val evidenceJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "query_audit")
data class QueryAuditEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val userPrompt: String,
    val toolSelected: String,
    val sqlQuery: String,
    val executionStatus: String, // SUCCESS, FAILED
    val executionTimeMs: Long,
    val rowCount: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val dbTarget: String = "IBM Db2 LUW"
)

@Entity(tableName = "db2_config")
data class Db2ConfigEntity(
    @PrimaryKey val id: Int = 1,
    val host: String = "db2w-techx2026.databases.appdomain.cloud",
    val port: Int = 32551,
    val database: String = "BLUDB",
    val username: String = "db2admin",
    val schemaName: String = "DATASENTRY",
    val useSsl: Boolean = true,
    val isConnected: Boolean = false,
    val lastPingTimestamp: Long = 0,
    val versionInfo: String = "IBM Db2 LUW v12.1.0 Enterprise",
    val vectorSupportEnabled: Boolean = true
)

data class ColumnInfo(
    val name: String,
    val inferredType: String, // VARCHAR, INTEGER, DECIMAL, TIMESTAMP, BOOLEAN
    val nullCount: Int = 0,
    val uniqueCount: Int = 0,
    val sampleValues: List<String> = emptyList()
)

enum class IssueSeverity {
    CRITICAL, HIGH, MEDIUM, LOW
}

enum class IssueType {
    MISSING_VALUE,
    DUPLICATE,
    FORMAT_ERROR,
    STATISTICAL_ANOMALY,
    TYPE_MISMATCH,
    LOGICAL_INCONSISTENCY
}

data class ToolInvocation(
    val toolName: String,
    val argumentsSummary: String,
    val sqlQuery: String = "",
    val executionTimeMs: Long = 0,
    val status: String = "COMPLETED",
    val recordsFound: Int = 0,
    val outputSummary: String = ""
)
