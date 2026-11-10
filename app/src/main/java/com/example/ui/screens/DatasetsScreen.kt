package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ColumnInfo
import com.example.data.model.DatasetEntity
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IbmBluePrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ScoreHighGreen
import com.example.ui.theme.ScoreLowRed
import com.example.ui.theme.ScoreMedAmber
import com.example.ui.theme.TealAccent
import org.json.JSONArray

@Composable
fun DatasetsScreen(
    datasets: List<DatasetEntity>,
    selectedDatasetId: String?,
    previewRows: List<Map<String, String>>,
    onSelectDataset: (String) -> Unit,
    onReRunProfiling: (String) -> Unit,
    onDeleteDataset: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeDataset = datasets.firstOrNull { it.id == selectedDatasetId } ?: datasets.firstOrNull()
    val columns = remember(activeDataset?.columnsJson) {
        if (activeDataset != null) parseColumns(activeDataset.columnsJson) else emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .testTag("datasets_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Enterprise Datasets in IBM Db2",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Manage tables, schema definitions, and persistent row records.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Dataset Switcher Tabs
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.testTag("dataset_switcher_row")
            ) {
                items(datasets) { dataset ->
                    val isSelected = dataset.id == activeDataset?.id
                    FilterChip(
                        selected = isSelected,
                        onClick = { onSelectDataset(dataset.id) },
                        label = {
                            Text(
                                text = dataset.name,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IbmBluePrimary,
                            selectedLabelColor = Color.White,
                            containerColor = NavySurfaceVariant,
                            labelColor = Color(0xFFCBD5E1)
                        ),
                        border = BorderStroke(1.dp, if (isSelected) CyanAccent else NavyCardBorder)
                    )
                }
            }
        }

        if (activeDataset != null) {
            // Dataset Details Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("dataset_details_card"),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeDataset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Table: ${activeDataset.db2TableName}",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = CyanAccent
                                )
                            }

                            Row {
                                Button(
                                    onClick = { onReRunProfiling(activeDataset.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = IbmBluePrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("rerun_profiling_btn")
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Re-Profile", fontSize = 11.sp)
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = { onDeleteDataset(activeDataset.id) },
                                    modifier = Modifier.testTag("delete_dataset_btn")
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Dataset", tint = ScoreLowRed)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeDataset.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("RECORDS", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text("${activeDataset.rowCount}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("COLUMNS", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text("${activeDataset.columnCount}", fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FILE SIZE", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(activeDataset.fileSizeFormatted, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MODE", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                                Text(if (activeDataset.isDemo) "SYNTHETIC" else "PRODUCTION", fontSize = 12.sp, color = if (activeDataset.isDemo) ScoreMedAmber else ScoreHighGreen, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Schema Information
            item {
                Text(
                    text = "Schema & Inferred Db2 Types",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            items(columns) { col ->
                Surface(
                    color = NavySurface,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, NavyCardBorder),
                    modifier = Modifier.fillMaxWidth().testTag("col_item_${col.name}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = col.name, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(
                                text = "Nulls: ${col.nullCount}  •  Unique Values: ${col.uniqueCount}",
                                fontSize = 11.sp,
                                color = if (col.nullCount > 0) ScoreLowRed else Color(0xFF94A3B8)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(IbmBluePrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = col.inferredType,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }

            // Live Data Preview Table
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Data Records Preview (${previewRows.size} sample rows)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            item {
                val horizontalScroll = rememberScrollState()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(NavySurface)
                        .border(1.dp, NavyCardBorder, RoundedCornerShape(8.dp))
                        .horizontalScroll(horizontalScroll)
                        .testTag("data_preview_table")
                ) {
                    Column {
                        // Header row
                        Row(
                            modifier = Modifier
                                .background(NavySurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 10.dp)
                        ) {
                            columns.forEach { col ->
                                Text(
                                    text = col.name,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.width(150.dp)
                                )
                            }
                        }

                        // Data rows
                        previewRows.take(15).forEachIndexed { index, rowMap ->
                            Row(
                                modifier = Modifier
                                    .background(if (index % 2 == 0) NavySurface else NavyDark)
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {
                                columns.forEach { col ->
                                    val cellVal = rowMap[col.name] ?: ""
                                    val isAnomaly = (cellVal.startsWith("-") && (col.name.contains("PRICE") || col.name.contains("AMOUNT") || col.name.contains("QUANTITY"))) ||
                                            cellVal.isBlank()
                                    Text(
                                        text = if (cellVal.isBlank()) "<NULL>" else cellVal,
                                        fontSize = 11.sp,
                                        color = if (cellVal.isBlank()) ScoreLowRed else if (isAnomaly) ScoreMedAmber else Color(0xFFCBD5E1),
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = if (isAnomaly) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.width(150.dp),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun parseColumns(json: String): List<ColumnInfo> {
    return try {
        val array = JSONArray(json)
        val list = mutableListOf<ColumnInfo>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ColumnInfo(
                    name = obj.optString("name", "COL_$i"),
                    inferredType = obj.optString("inferredType", "VARCHAR"),
                    nullCount = obj.optInt("nullCount", 0),
                    uniqueCount = obj.optInt("uniqueCount", 0)
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}
