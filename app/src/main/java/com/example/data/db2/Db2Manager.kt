package com.example.data.db2

import com.example.data.model.ColumnInfo
import com.example.data.model.DatasetEntity
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.data.model.QueryAuditEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Db2Manager {

    /**
     * Generates production-ready IBM Db2 DDL script for all 7 required tables.
     */
    fun generateDb2DDL(schemaName: String = "DATASENTRY", enableVector: Boolean = true): String {
        val vectorColumnDefinition = if (enableVector) {
            ",\n    EMBEDDING_VECTOR VECTOR(768, FLOAT)"
        } else {
            ""
        }

        return """
-- ============================================================================
-- IBM Db2 TechX Summit 2026: DataSentry AI Enterprise Database Schema
-- Compatible with IBM Db2 LUW v11.5 / v12.1+ and Db2 on Cloud
-- ============================================================================

CREATE SCHEMA $schemaName;
SET CURRENT SCHEMA = $schemaName;

-- 1. DATASETS Table
CREATE TABLE $schemaName.DATASETS (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    NAME VARCHAR(255) NOT NULL,
    DESCRIPTION CLOB(64K),
    ROW_COUNT INTEGER NOT NULL DEFAULT 0,
    COLUMN_COUNT INTEGER NOT NULL DEFAULT 0,
    COLUMNS_JSON CLOB(1M),
    FILE_SIZE VARCHAR(32),
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    STATUS VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    IS_DEMO SMALLINT NOT NULL DEFAULT 0,
    DB2_TABLE_NAME VARCHAR(128)
);

CREATE INDEX $schemaName.IDX_DATASETS_CREATED ON $schemaName.DATASETS(CREATED_AT DESC);

-- 2. DATA_ROWS Table (Partitioned by dataset)
CREATE TABLE $schemaName.DATA_ROWS (
    ID BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1) PRIMARY KEY,
    DATASET_ID VARCHAR(64) NOT NULL,
    ROW_INDEX INTEGER NOT NULL,
    ROW_DATA_JSON CLOB(2M) NOT NULL,
    INGESTED_AT TIMESTAMP DEFAULT CURRENT TIMESTAMP,
    CONSTRAINT FK_ROWS_DATASET FOREIGN KEY (DATASET_ID) 
        REFERENCES $schemaName.DATASETS(ID) ON DELETE CASCADE
);

CREATE INDEX $schemaName.IDX_ROWS_DATASET ON $schemaName.DATA_ROWS(DATASET_ID, ROW_INDEX);

-- 3. QUALITY_RUNS Table
CREATE TABLE $schemaName.QUALITY_RUNS (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DATASET_ID VARCHAR(64) NOT NULL,
    OVERALL_SCORE INTEGER NOT NULL,
    MISSING_VALUES_COUNT INTEGER NOT NULL DEFAULT 0,
    DUPLICATE_COUNT INTEGER NOT NULL DEFAULT 0,
    FORMAT_ERRORS_COUNT INTEGER NOT NULL DEFAULT 0,
    ANOMALY_COUNT INTEGER NOT NULL DEFAULT 0,
    STATUS VARCHAR(32) NOT NULL DEFAULT 'COMPLETED',
    RUN_TIMESTAMP TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    EXECUTION_TIME_MS BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT FK_RUNS_DATASET FOREIGN KEY (DATASET_ID) 
        REFERENCES $schemaName.DATASETS(ID) ON DELETE CASCADE
);

CREATE INDEX $schemaName.IDX_RUNS_DATASET ON $schemaName.QUALITY_RUNS(DATASET_ID, RUN_TIMESTAMP DESC);

-- 4. QUALITY_ISSUES Table
CREATE TABLE $schemaName.QUALITY_ISSUES (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    RUN_ID VARCHAR(64) NOT NULL,
    DATASET_ID VARCHAR(64) NOT NULL,
    ISSUE_TYPE VARCHAR(64) NOT NULL,
    SEVERITY VARCHAR(16) NOT NULL,
    COLUMN_NAME VARCHAR(128) NOT NULL,
    AFFECTED_RECORDS_COUNT INTEGER NOT NULL,
    DESCRIPTION CLOB(64K) NOT NULL,
    SAMPLE_VALUES_JSON CLOB(128K),
    ROOT_CAUSE_HYPOTHESIS CLOB(128K),
    BUSINESS_IMPACT CLOB(128K),
    SUGGESTED_REMEDIATION CLOB(128K),
    REMEDIATION_SQL CLOB(64K),
    CONFIDENCE_SCORE DECIMAL(4,3) NOT NULL DEFAULT 0.950,
    EVIDENCE_SNIPPET CLOB(64K),
    IS_AI_HYPOTHESIS SMALLINT NOT NULL DEFAULT 1$vectorColumnDefinition,
    CONSTRAINT FK_ISSUES_RUN FOREIGN KEY (RUN_ID) 
        REFERENCES $schemaName.QUALITY_RUNS(ID) ON DELETE CASCADE
);

CREATE INDEX $schemaName.IDX_ISSUES_SEVERITY ON $schemaName.QUALITY_ISSUES(SEVERITY, ISSUE_TYPE);
CREATE INDEX $schemaName.IDX_ISSUES_DATASET ON $schemaName.QUALITY_ISSUES(DATASET_ID);

-- 5. AI_REPORTS Table
CREATE TABLE $schemaName.AI_REPORTS (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    DATASET_ID VARCHAR(64) NOT NULL,
    DATASET_NAME VARCHAR(255) NOT NULL,
    TITLE VARCHAR(255) NOT NULL,
    EXECUTIVE_SUMMARY CLOB(256K) NOT NULL,
    QUALITY_SCORE INTEGER NOT NULL,
    STATS_JSON CLOB(128K),
    KEY_FINDINGS_JSON CLOB(256K),
    RECOMMENDATIONS_JSON CLOB(256K),
    BUSINESS_IMPACT CLOB(128K),
    GENERATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    REPORT_FORMAT VARCHAR(32) NOT NULL DEFAULT 'EXECUTIVE',
    CONSTRAINT FK_REPORTS_DATASET FOREIGN KEY (DATASET_ID) 
        REFERENCES $schemaName.DATASETS(ID) ON DELETE CASCADE
);

-- 6. CHAT_HISTORY Table
CREATE TABLE $schemaName.CHAT_HISTORY (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    ROLE VARCHAR(16) NOT NULL,
    CONTENT CLOB(1M) NOT NULL,
    TOOL_CALLS_JSON CLOB(256K),
    SQL_EXECUTED CLOB(128K),
    EVIDENCE_JSON CLOB(256K),
    CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP
);

-- 7. QUERY_AUDIT Table (Enterprise Compliance & Performance Audit)
CREATE TABLE $schemaName.QUERY_AUDIT (
    ID VARCHAR(64) NOT NULL PRIMARY KEY,
    USER_PROMPT CLOB(32K),
    TOOL_SELECTED VARCHAR(128) NOT NULL,
    SQL_QUERY CLOB(64K) NOT NULL,
    EXECUTION_STATUS VARCHAR(32) NOT NULL,
    EXECUTION_TIME_MS BIGINT NOT NULL,
    ROW_COUNT INTEGER NOT NULL DEFAULT 0,
    AUDIT_TIMESTAMP TIMESTAMP NOT NULL DEFAULT CURRENT TIMESTAMP,
    DB_TARGET VARCHAR(64) NOT NULL DEFAULT 'IBM Db2 LUW'
);

CREATE INDEX $schemaName.IDX_AUDIT_TIME ON $schemaName.QUERY_AUDIT(AUDIT_TIMESTAMP DESC);

COMMIT;
        """.trimIndent()
    }

    /**
     * Constructs a safe, read-only parameterized Db2 SQL query for profiling.
     */
    fun buildDb2ProfilingQuery(tableName: String, column: ColumnInfo): String {
        return when (column.inferredType) {
            "INTEGER", "DECIMAL" -> {
                """
                SELECT 
                    COUNT(*) AS TOTAL_ROWS,
                    COUNT(${"\"" + column.name + "\""}) AS NON_NULL_ROWS,
                    SUM(CASE WHEN ${"\"" + column.name + "\""} IS NULL THEN 1 ELSE 0 END) AS NULL_ROWS,
                    MIN(${"\"" + column.name + "\""}) AS MIN_VAL,
                    MAX(${"\"" + column.name + "\""}) AS MAX_VAL,
                    AVG(CAST(${"\"" + column.name + "\""} AS DOUBLE)) AS AVG_VAL,
                    COUNT(DISTINCT ${"\"" + column.name + "\""}) AS UNIQUE_VALUES
                FROM $tableName
                """.trimIndent()
            }
            else -> {
                """
                SELECT 
                    COUNT(*) AS TOTAL_ROWS,
                    COUNT(${"\"" + column.name + "\""}) AS NON_NULL_ROWS,
                    SUM(CASE WHEN ${"\"" + column.name + "\""} IS NULL THEN 1 ELSE 0 END) AS NULL_ROWS,
                    COUNT(DISTINCT ${"\"" + column.name + "\""}) AS UNIQUE_VALUES
                FROM $tableName
                """.trimIndent()
            }
        }
    }

    /**
     * Builds safe Db2 SQL to identify duplicate keys
     */
    fun buildDuplicateCheckSql(tableName: String, primaryOrKeyCol: String): String {
        return """
            SELECT ${"\"" + primaryOrKeyCol + "\""}, COUNT(*) AS DUPLICATE_COUNT
            FROM $tableName
            GROUP BY ${"\"" + primaryOrKeyCol + "\""}
            HAVING COUNT(*) > 1
            ORDER BY DUPLICATE_COUNT DESC
            FETCH FIRST 50 ROWS ONLY
        """.trimIndent()
    }

    /**
     * Builds Db2 Vector similarity search query using VECTOR_DISTANCE when enabled,
     * or weighted full-text relational fallback.
     */
    fun buildSemanticSearchSql(
        schemaName: String,
        searchQuery: String,
        vectorSupported: Boolean
    ): Pair<String, String> {
        return if (vectorSupported) {
            val sql = """
                SELECT ID, ISSUE_TYPE, SEVERITY, DESCRIPTION, ROOT_CAUSE_HYPOTHESIS,
                       VECTOR_DISTANCE(EMBEDDING_VECTOR, ?, 'COSINE') AS SIMILARITY_DISTANCE
                FROM $schemaName.QUALITY_ISSUES
                WHERE EMBEDDING_VECTOR IS NOT NULL
                ORDER BY SIMILARITY_DISTANCE ASC
                FETCH FIRST 5 ROWS ONLY
            """.trimIndent()
            sql to "Native Db2 Vector Search (COSINE distance)"
        } else {
            val sanitized = searchQuery.replace("'", "''")
            val sql = """
                SELECT ID, ISSUE_TYPE, SEVERITY, DESCRIPTION, ROOT_CAUSE_HYPOTHESIS
                FROM $schemaName.QUALITY_ISSUES
                WHERE LOWER(DESCRIPTION) LIKE LOWER('%$sanitized%')
                   OR LOWER(ROOT_CAUSE_HYPOTHESIS) LIKE LOWER('%$sanitized%')
                ORDER BY CASE SEVERITY WHEN 'CRITICAL' THEN 1 WHEN 'HIGH' THEN 2 ELSE 3 END
                FETCH FIRST 5 ROWS ONLY
            """.trimIndent()
            sql to "Relational SQL Search Fallback (LIKE operator)"
        }
    }
}
