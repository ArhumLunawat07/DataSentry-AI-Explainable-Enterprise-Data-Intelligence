package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Dataset
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.demo.DemoDatasets
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IbmBluePrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ScoreMedAmber
import com.example.ui.theme.TealAccent

@Composable
fun UploadScreen(
    onUploadCsv: (String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var datasetName by remember { mutableStateOf("") }
    var datasetDescription by remember { mutableStateOf("") }
    var csvContent by remember {
        mutableStateOf(
            """
            TRANSACTION_ID,CUSTOMER_ID,AMOUNT,STATUS,TIMESTAMP
            TXN_101,CUST_901,150.00,COMPLETED,2026-03-01
            TXN_102,, -45.00,PENDING,2026-03-02
            TXN_103,CUST_903,999999.00,COMPLETED,2026-03-03
            TXN_101,CUST_901,150.00,COMPLETED,2026-03-01
            TXN_104,CUST_904,220.50,FAILED,2026-03-04
            """.trimIndent()
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .testTag("upload_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Dataset Ingestion & Benchmarking",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Import business datasets into IBM Db2 with automated schema and anomaly discovery.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Section 1: Pre-configured Hackathon Benchmark Datasets
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_benchmarks_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FlashOn, contentDescription = null, tint = ScoreMedAmber, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "1-Click Benchmark Datasets for TechX Judges",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Preset 1: Retail
                    val retailPreset = remember { DemoDatasets.getRetailTransactionsDemo() }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                datasetName = "Retail_Omnichannel_Sales_Demo"
                                datasetDescription = retailPreset.entity.description
                                csvContent = retailPreset.rawCsv
                            }
                            .testTag("load_preset_retail"),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.DataObject, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Retail Omni-channel Transactions (100 rows)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text("Contains negative prices, missing customer IDs, invalid emails", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset 2: Finance
                    val finPreset = remember { DemoDatasets.getFinancialLedgerDemo() }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                datasetName = "Banking_General_Ledger_Demo"
                                datasetDescription = finPreset.entity.description
                                csvContent = finPreset.rawCsv
                            }
                            .testTag("load_preset_finance"),
                        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = TealAccent, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Core Banking General Ledger (80 rows)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                                Text("Contains unbalanced debits/credits, missing GL account codes", color = Color(0xFF94A3B8), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        // Section 2: Upload or Edit Custom CSV
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("csv_editor_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Import Custom Business Dataset",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = datasetName,
                        onValueChange = { datasetName = it },
                        label = { Text("Dataset Name (e.g., Enterprise_Sales_Q1)") },
                        modifier = Modifier.fillMaxWidth().testTag("input_dataset_name"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = datasetDescription,
                        onValueChange = { datasetDescription = it },
                        label = { Text("Dataset Description / Business Context") },
                        modifier = Modifier.fillMaxWidth().testTag("input_dataset_desc"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "CSV Content (Header row + Comma-separated values)",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.height(4.dp))

                    OutlinedTextField(
                        value = csvContent,
                        onValueChange = { csvContent = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .testTag("input_csv_content"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedTextColor = Color(0xFFA7F3D0),
                            unfocusedTextColor = Color(0xFFA7F3D0)
                        ),
                        textStyle = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            val finalName = datasetName.ifBlank { "Custom_Dataset_${System.currentTimeMillis() % 10000}" }
                            onUploadCsv(finalName, datasetDescription, csvContent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IbmBluePrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_import_db2")
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Import & Profile in IBM Db2", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
