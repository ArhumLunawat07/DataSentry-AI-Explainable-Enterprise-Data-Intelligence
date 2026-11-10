package com.example.data.repository

import com.example.data.ai.AgentExecutionResult
import com.example.data.ai.AiAgent
import com.example.data.demo.DemoDatasets
import com.example.data.local.AppDatabase
import com.example.data.model.AiReportEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DataRowEntity
import com.example.data.model.DatasetEntity
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.data.model.QueryAuditEntity
import com.example.data.profiling.DataProfiler
import com.example.data.profiling.ProfileResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class DataSentryRepository(private val database: AppDatabase) {

    private val aiAgent = AiAgent(database)

    suspend fun initializeIfNeeded() = withContext(Dispatchers.IO) {
        val existing = database.datasetDao().getDatasetCount()
        if (existing == 0) {
            // Seed Demo Dataset 1: Retail
            val retail = DemoDatasets.getRetailTransactionsDemo()
            database.datasetDao().insertDataset(retail.entity)
            val retailRows = retail.rows.mapIndexed { idx, rowMap ->
                DataRowEntity(datasetId = retail.entity.id, rowIndex = idx, rowDataJson = JSONObject(rowMap).toString())
            }
            database.dataRowDao().insertRows(retailRows)
            val profileRetail = DataProfiler.profileDataset(retail.entity.id, retail.headers, retail.rows, retail.entity.name)
            database.qualityRunDao().insertRun(profileRetail.qualityRun)
            database.qualityIssueDao().insertIssues(profileRetail.issues)

            // Seed Demo Dataset 2: Finance
            val fin = DemoDatasets.getFinancialLedgerDemo()
            database.datasetDao().insertDataset(fin.entity)
            val finRows = fin.rows.mapIndexed { idx, rowMap ->
                DataRowEntity(datasetId = fin.entity.id, rowIndex = idx, rowDataJson = JSONObject(rowMap).toString())
            }
            database.dataRowDao().insertRows(finRows)
            val profileFin = DataProfiler.profileDataset(fin.entity.id, fin.headers, fin.rows, fin.entity.name)
            database.qualityRunDao().insertRun(profileFin.qualityRun)
            database.qualityIssueDao().insertIssues(profileFin.issues)

            // Seed initial Db2 Config
            database.db2ConfigDao().saveConfig(
                Db2ConfigEntity(
                    id = 1,
                    host = "db2w-techx2026.databases.appdomain.cloud",
                    port = 32551,
                    database = "BLUDB",
                    username = "db2admin",
                    schemaName = "DATASENTRY",
                    useSsl = true,
                    isConnected = false, // starts in clearly labeled Demo Mode until user connects
                    lastPingTimestamp = System.currentTimeMillis() - 60000,
                    versionInfo = "IBM Db2 on Cloud v12.1.0 Enterprise",
                    vectorSupportEnabled = true
                )
            )

            // Seed initial Executive Report
            val initialReport = AiReportEntity(
                id = "report_init_retail_001",
                datasetId = retail.entity.id,
                datasetName = retail.entity.name,
                title = "Initial Data Quality Audit: ${retail.entity.name}",
                executiveSummary = "DataSentry AI automated profiling completed for ${retail.entity.name}. Discovered critical missing foreign keys in CUSTOMER_ID, negative prices in UNIT_PRICE, and non-compliant email formats.",
                qualityScore = profileRetail.qualityRun.overallScore,
                statsJson = JSONObject(mapOf(
                    "totalRecords" to retail.rows.size,
                    "missing" to profileRetail.qualityRun.missingValuesCount,
                    "duplicates" to profileRetail.qualityRun.duplicateCount,
                    "anomalies" to profileRetail.qualityRun.anomalyCount
                )).toString(),
                keyFindingsJson = JSONArray(profileRetail.issues.take(4).map { "${it.severity}: ${it.description}" }).toString(),
                recommendationsJson = JSONArray(listOf(
                    "Implement Db2 CHECK constraint CHK_UNIT_PRICE_POS CHECK (UNIT_PRICE >= 0)",
                    "Configure mandatory foreign key validation before ingestion",
                    "Add unique index on TRANSACTION_ID"
                )).toString(),
                businessImpact = "Negative prices distort gross merchandise value reporting by 3.8%. Fix priority: CRITICAL.",
                generatedAt = System.currentTimeMillis() - 3600000,
                reportFormat = "EXECUTIVE"
            )
            database.aiReportDao().insertReport(initialReport)

            // Seed initial Welcome Chat Message
            database.chatHistoryDao().insertMessage(
                ChatMessageEntity(
                    role = "assistant",
                    content = "👋 Welcome to **DataSentry AI**! I am connected to your enterprise data intelligence layer.\n\n" +
                            "I have already profiled your initial datasets. Try asking:\n" +
                            "• *'Which dataset has the most errors?'*\n" +
                            "• *'Find duplicate transactions'* \n" +
                            "• *'Explain the negative price anomaly'* \n" +
                            "• *'Generate an executive summary report'*",
                    toolCallsJson = "[]",
                    sqlExecuted = "",
                    evidenceJson = "",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    fun getAllDatasetsFlow(): Flow<List<DatasetEntity>> = database.datasetDao().getAllDatasetsFlow()

    suspend fun getDatasetById(id: String): DatasetEntity? = withContext(Dispatchers.IO) {
        database.datasetDao().getDatasetById(id)
    }

    suspend fun getRowsForDataset(datasetId: String, limit: Int = 100): List<Map<String, String>> = withContext(Dispatchers.IO) {
        val entities = database.dataRowDao().getRowsForDataset(datasetId, limit)
        entities.map { entity ->
            val obj = JSONObject(entity.rowDataJson)
            val map = mutableMapOf<String, String>()
            obj.keys().forEach { k -> map[k] = obj.optString(k, "") }
            map
        }
    }

    fun getAllIssuesFlow(): Flow<List<QualityIssueEntity>> = database.qualityIssueDao().getAllIssuesFlow()

    suspend fun getIssuesForDataset(datasetId: String): List<QualityIssueEntity> = withContext(Dispatchers.IO) {
        database.qualityIssueDao().getIssuesForDataset(datasetId)
    }

    fun getAllRunsFlow(): Flow<List<QualityRunEntity>> = database.qualityRunDao().getAllRunsFlow()

    suspend fun getLatestRunForDataset(datasetId: String): QualityRunEntity? = withContext(Dispatchers.IO) {
        database.qualityRunDao().getLatestRunForDataset(datasetId)
    }

    fun getAllReportsFlow(): Flow<List<AiReportEntity>> = database.aiReportDao().getAllReportsFlow()

    suspend fun getReportById(id: String): AiReportEntity? = withContext(Dispatchers.IO) {
        database.aiReportDao().getReportById(id)
    }

    fun getChatMessagesFlow(): Flow<List<ChatMessageEntity>> = database.chatHistoryDao().getAllMessagesFlow()

    fun getRecentAuditsFlow(limit: Int = 50): Flow<List<QueryAuditEntity>> = database.queryAuditDao().getRecentAuditsFlow(limit)

    fun getDb2ConfigFlow(): Flow<Db2ConfigEntity?> = database.db2ConfigDao().getConfigFlow()

    suspend fun saveDb2Config(config: Db2ConfigEntity) = withContext(Dispatchers.IO) {
        database.db2ConfigDao().saveConfig(config)
    }

    suspend fun testDb2Connection(config: Db2ConfigEntity): Boolean = withContext(Dispatchers.IO) {
        // Validate host and port syntax and simulate Db2 network handshake
        val valid = config.host.isNotBlank() && config.port > 1024 && config.database.isNotBlank()
        val updated = config.copy(
            isConnected = valid,
            lastPingTimestamp = System.currentTimeMillis()
        )
        database.db2ConfigDao().saveConfig(updated)
        valid
    }

    suspend fun deleteDataset(datasetId: String) = withContext(Dispatchers.IO) {
        database.datasetDao().deleteDatasetCascade(datasetId)
    }

    suspend fun uploadCustomCsv(
        name: String,
        description: String,
        csvContent: String
    ): DatasetEntity = withContext(Dispatchers.IO) {
        val lines = csvContent.lines().filter { it.isNotBlank() }
        require(lines.size >= 2) { "CSV file must contain a header row and at least one data record." }

        val headers = lines.first().split(",").map { it.trim().replace("\"", "") }
        val rows = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val tokens = lines[i].split(",").map { it.trim().replace("\"", "") }
            val rowMap = mutableMapOf<String, String>()
            headers.forEachIndexed { idx, h ->
                rowMap[h] = if (idx < tokens.size) tokens[idx] else ""
            }
            rows.add(rowMap)
        }

        val datasetId = UUID.randomUUID().toString()
        val tableName = "DATASENTRY." + name.uppercase().replace(Regex("[^A-Z0-9_]"), "_")

        val profileResult = DataProfiler.profileDataset(datasetId, headers, rows, name)

        val columnsJson = JSONArray(profileResult.columnProfiles.map {
            JSONObject(mapOf(
                "name" to it.name,
                "inferredType" to it.inferredType,
                "nullCount" to it.nullCount,
                "uniqueCount" to it.uniqueCount
            ))
        }).toString()

        val entity = DatasetEntity(
            id = datasetId,
            name = name,
            description = description.ifBlank { "Custom uploaded dataset with ${rows.size} records." },
            rowCount = rows.size,
            columnCount = headers.size,
            columnsJson = columnsJson,
            fileSizeFormatted = "${(csvContent.length / 1024).coerceAtLeast(1)} KB",
            createdAt = System.currentTimeMillis(),
            status = "ACTIVE",
            isDemo = false,
            db2TableName = tableName
        )

        database.datasetDao().insertDataset(entity)

        val rowEntities = rows.mapIndexed { idx, rMap ->
            DataRowEntity(datasetId = datasetId, rowIndex = idx, rowDataJson = JSONObject(rMap).toString())
        }
        database.dataRowDao().insertRows(rowEntities)

        database.qualityRunDao().insertRun(profileResult.qualityRun)
        database.qualityIssueDao().insertIssues(profileResult.issues)

        entity
    }

    suspend fun runDataProfiling(datasetId: String): ProfileResult? = withContext(Dispatchers.IO) {
        val dataset = database.datasetDao().getDatasetById(datasetId) ?: return@withContext null
        val rows = getRowsForDataset(datasetId, 500)
        val headers = parseHeaders(dataset.columnsJson)
        val result = DataProfiler.profileDataset(datasetId, headers, rows, dataset.name)
        database.qualityRunDao().insertRun(result.qualityRun)
        database.qualityIssueDao().insertIssues(result.issues)
        result
    }

    suspend fun sendMessage(userText: String, activeDatasetId: String? = null): AgentExecutionResult = withContext(Dispatchers.IO) {
        // Record user message
        database.chatHistoryDao().insertMessage(
            ChatMessageEntity(
                role = "user",
                content = userText,
                timestamp = System.currentTimeMillis()
            )
        )

        val result = aiAgent.executeUserQuery(userText, activeDatasetId)

        // Record assistant response with tools and SQL
        val toolCallsJson = JSONArray(result.toolInvocations.map {
            JSONObject(mapOf(
                "toolName" to it.toolName,
                "arguments" to it.argumentsSummary,
                "sql" to it.sqlQuery,
                "latencyMs" to it.executionTimeMs,
                "status" to it.status,
                "recordsFound" to it.recordsFound,
                "output" to it.outputSummary
            ))
        }).toString()

        database.chatHistoryDao().insertMessage(
            ChatMessageEntity(
                role = "assistant",
                content = result.answer,
                toolCallsJson = toolCallsJson,
                sqlExecuted = result.sqlExecuted,
                evidenceJson = result.evidenceSnippet,
                timestamp = System.currentTimeMillis()
            )
        )

        result
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        database.chatHistoryDao().clearHistory()
    }

    private fun parseHeaders(columnsJson: String): List<String> {
        return try {
            val array = JSONArray(columnsJson)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getJSONObject(i).getString("name"))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
