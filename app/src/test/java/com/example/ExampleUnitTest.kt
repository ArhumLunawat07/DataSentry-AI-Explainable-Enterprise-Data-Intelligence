package com.example

import com.example.data.db2.Db2Manager
import com.example.data.profiling.DataProfiler
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun dataProfiler_detectsAnomaliesNullsAndDuplicates() {
        val headers = listOf("TXN_ID", "CUST_ID", "AMOUNT", "EMAIL")
        val rows = listOf(
            mapOf("TXN_ID" to "T1", "CUST_ID" to "C1", "AMOUNT" to "100.00", "EMAIL" to "user1@corp.com"),
            mapOf("TXN_ID" to "T2", "CUST_ID" to "", "AMOUNT" to "-45.00", "EMAIL" to "invalid_email"),
            mapOf("TXN_ID" to "T1", "CUST_ID" to "C1", "AMOUNT" to "100.00", "EMAIL" to "user1@corp.com"), // duplicate
            mapOf("TXN_ID" to "T3", "CUST_ID" to "C3", "AMOUNT" to "999999.00", "EMAIL" to "user3@corp.com") // outlier
        )

        val result = DataProfiler.profileDataset(
            datasetId = "test_ds_1",
            headers = headers,
            rows = rows,
            datasetName = "TEST_TABLE"
        )

        assertNotNull(result)
        assertTrue("Overall score should reflect penalties", result.qualityRun.overallScore < 100)
        assertTrue("Missing value detected in CUST_ID", result.qualityRun.missingValuesCount >= 1)
        assertTrue("Duplicate detected", result.qualityRun.duplicateCount >= 1)
        assertTrue("Anomalies detected in AMOUNT", result.qualityRun.anomalyCount >= 1)
    }

    @Test
    fun db2Manager_generatesCompliantDdlForAllSevenTables() {
        val ddl = Db2Manager.generateDb2DDL(schemaName = "DATASENTRY", enableVector = true)

        assertTrue("Contains DATASETS table", ddl.contains("CREATE TABLE DATASENTRY.DATASETS"))
        assertTrue("Contains DATA_ROWS table", ddl.contains("CREATE TABLE DATASENTRY.DATA_ROWS"))
        assertTrue("Contains QUALITY_RUNS table", ddl.contains("CREATE TABLE DATASENTRY.QUALITY_RUNS"))
        assertTrue("Contains QUALITY_ISSUES table", ddl.contains("CREATE TABLE DATASENTRY.QUALITY_ISSUES"))
        assertTrue("Contains AI_REPORTS table", ddl.contains("CREATE TABLE DATASENTRY.AI_REPORTS"))
        assertTrue("Contains CHAT_HISTORY table", ddl.contains("CREATE TABLE DATASENTRY.CHAT_HISTORY"))
        assertTrue("Contains QUERY_AUDIT table", ddl.contains("CREATE TABLE DATASENTRY.QUERY_AUDIT"))
        assertTrue("Contains VECTOR column definition", ddl.contains("VECTOR(768, FLOAT)"))
    }

    @Test
    fun db2Manager_vectorFallbackGeneratesRelationalQuery() {
        val (vectorSql, vectorDesc) = Db2Manager.buildSemanticSearchSql("DATASENTRY", "missing customer", true)
        assertTrue("Uses VECTOR_DISTANCE", vectorSql.contains("VECTOR_DISTANCE"))
        assertTrue(vectorDesc.contains("Vector"))

        val (relationalSql, relDesc) = Db2Manager.buildSemanticSearchSql("DATASENTRY", "missing customer", false)
        assertTrue("Uses LIKE for relational fallback", relationalSql.contains("LIKE"))
        assertTrue(relDesc.contains("Relational"))
    }
}
