package com.example.data.demo

import com.example.data.model.ColumnInfo
import com.example.data.model.DatasetEntity
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object DemoDatasets {

    data class PreloadedDataset(
        val entity: DatasetEntity,
        val headers: List<String>,
        val rows: List<Map<String, String>>,
        val rawCsv: String
    )

    fun getRetailTransactionsDemo(): PreloadedDataset {
        val id = "dataset_retail_tx_001"
        val headers = listOf(
            "TRANSACTION_ID",
            "CUSTOMER_ID",
            "STORE_ID",
            "TXN_TIMESTAMP",
            "PAYMENT_METHOD",
            "ITEM_SKU",
            "QUANTITY",
            "UNIT_PRICE",
            "TOTAL_AMOUNT",
            "CUSTOMER_EMAIL",
            "SHIPPING_ZIP"
        )

        val csvLines = mutableListOf<String>()
        csvLines.add(headers.joinToString(","))

        val rows = mutableListOf<Map<String, String>>()

        // Generate 120 realistic rows with deliberate data quality problems
        val paymentMethods = listOf("CREDIT_CARD", "PAYPAL", "APPLE_PAY", "WIRE_TRANSFER", "STORE_CREDIT")
        val skus = listOf("SKU-ELEC-4091", "SKU-HOME-1022", "SKU-APPA-9901", "SKU-AUTO-3312", "SKU-GROC-5541")

        for (i in 1..100) {
            val txnId = "TXN-2026-${String.format("%05d", if (i in 15..17) 10014 else i)}" // duplicate IDs injected
            val custId = if (i in listOf(4, 12, 28, 45, 63, 82)) "" else "CUST-${1000 + (i % 30)}" // missing customer IDs
            val storeId = "STR-${100 + (i % 8)}"
            val timestamp = "2026-03-${String.format("%02d", 1 + (i % 25))} 14:${String.format("%02d", (i * 7) % 60)}:00"
            val payment = paymentMethods[i % paymentMethods.size]
            val sku = skus[i % skus.size]
            val quantity = if (i == 33) "-2" else "${1 + (i % 5)}" // negative quantity anomaly
            val unitPrice = if (i == 48) "-49.99" else String.format("%.2f", 19.99 + (i * 3.5) % 150) // negative price
            val totalAmount = if (i == 77) "999999.00" else if (unitPrice.startsWith("-")) "-99.98" else String.format("%.2f", (quantity.toDoubleOrNull() ?: 1.0) * (unitPrice.toDoubleOrNull() ?: 20.0)) // extreme outlier
            val email = when (i) {
                7 -> "j.smith_invalid_domain" // format error
                22 -> "alex@@corporate..com" // format error
                55 -> "maria.chen.nospam" // format error
                else -> "user${1000 + i}@enterprise-corp.com"
            }
            val zip = if (i in listOf(9, 39, 71)) "" else "${10001 + (i * 13) % 80000}" // missing zip

            val rowMap = mapOf(
                "TRANSACTION_ID" to txnId,
                "CUSTOMER_ID" to custId,
                "STORE_ID" to storeId,
                "TXN_TIMESTAMP" to timestamp,
                "PAYMENT_METHOD" to payment,
                "ITEM_SKU" to sku,
                "QUANTITY" to quantity,
                "UNIT_PRICE" to unitPrice,
                "TOTAL_AMOUNT" to totalAmount,
                "CUSTOMER_EMAIL" to email,
                "SHIPPING_ZIP" to zip
            )
            rows.add(rowMap)
            csvLines.add(headers.joinToString(",") { rowMap[it] ?: "" })
        }

        val columnsJson = JSONArray(headers.map { col ->
            JSONObject(mapOf(
                "name" to col,
                "inferredType" to when {
                    col.contains("AMOUNT") || col.contains("PRICE") -> "DECIMAL"
                    col.contains("QUANTITY") -> "INTEGER"
                    col.contains("TIMESTAMP") -> "TIMESTAMP"
                    else -> "VARCHAR"
                },
                "nullCount" to when (col) {
                    "CUSTOMER_ID" -> 6
                    "SHIPPING_ZIP" -> 3
                    else -> 0
                },
                "uniqueCount" to when (col) {
                    "TRANSACTION_ID" -> 97
                    "PAYMENT_METHOD" -> 5
                    else -> 40
                }
            ))
        }).toString()

        val entity = DatasetEntity(
            id = id,
            name = "Global_Retail_Omnichannel_Txns_2026",
            description = "High-volume retail sales transactions across web, mobile, and in-store POS with intentional anomaly injection for Db2 TechX evaluation.",
            rowCount = rows.size,
            columnCount = headers.size,
            columnsJson = columnsJson,
            fileSizeFormatted = "184 KB",
            createdAt = System.currentTimeMillis() - 7200000,
            status = "ACTIVE",
            isDemo = true,
            db2TableName = "DATASENTRY.RETAIL_TRANSACTIONS"
        )

        return PreloadedDataset(entity, headers, rows, csvLines.joinToString("\n"))
    }

    fun getFinancialLedgerDemo(): PreloadedDataset {
        val id = "dataset_financial_gl_002"
        val headers = listOf(
            "ENTRY_ID",
            "ACCOUNT_CODE",
            "FISCAL_PERIOD",
            "CURRENCY",
            "DEBIT_AMOUNT",
            "CREDIT_AMOUNT",
            "BALANCED_FLAG",
            "APPROVER_USER"
        )

        val csvLines = mutableListOf<String>()
        csvLines.add(headers.joinToString(","))
        val rows = mutableListOf<Map<String, String>>()

        for (i in 1..80) {
            val entryId = "GL-2026-${String.format("%04d", if (i in 10..11) 1009 else i)}"
            val account = if (i in listOf(5, 25, 60)) "" else "ACC-${4000 + (i % 20)}"
            val period = "2026-Q1"
            val currency = if (i == 42) "INVALID" else "USD"
            val debit = if (i % 2 == 0) String.format("%.2f", 500.0 + (i * 25.0)) else "0.00"
            val credit = if (i % 2 != 0) String.format("%.2f", 500.0 + ((i - 1) * 25.0)) else "0.00"
            val balanced = if (i in listOf(18, 52)) "N" else "Y"
            val approver = if (i in listOf(12, 33)) "" else "FIN_EXEC_${(i % 5) + 1}"

            val rowMap = mapOf(
                "ENTRY_ID" to entryId,
                "ACCOUNT_CODE" to account,
                "FISCAL_PERIOD" to period,
                "CURRENCY" to currency,
                "DEBIT_AMOUNT" to debit,
                "CREDIT_AMOUNT" to credit,
                "BALANCED_FLAG" to balanced,
                "APPROVER_USER" to approver
            )
            rows.add(rowMap)
            csvLines.add(headers.joinToString(",") { rowMap[it] ?: "" })
        }

        val columnsJson = JSONArray(headers.map { col ->
            JSONObject(mapOf(
                "name" to col,
                "inferredType" to if (col.contains("AMOUNT")) "DECIMAL" else "VARCHAR",
                "nullCount" to if (col == "ACCOUNT_CODE") 3 else if (col == "APPROVER_USER") 2 else 0,
                "uniqueCount" to 30
            ))
        }).toString()

        val entity = DatasetEntity(
            id = id,
            name = "Core_Banking_General_Ledger",
            description = "Enterprise accounting ledger records with audit anomalies, unbalanced credits, and missing general ledger codes.",
            rowCount = rows.size,
            columnCount = headers.size,
            columnsJson = columnsJson,
            fileSizeFormatted = "92 KB",
            createdAt = System.currentTimeMillis() - 14400000,
            status = "ACTIVE",
            isDemo = true,
            db2TableName = "DATASENTRY.FINANCIAL_LEDGER"
        )

        return PreloadedDataset(entity, headers, rows, csvLines.joinToString("\n"))
    }
}
