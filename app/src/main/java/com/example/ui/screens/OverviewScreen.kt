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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DatasetEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.ui.components.MetricKpiCard
import com.example.ui.components.QualityScoreGauge
import com.example.ui.components.SeverityBadge
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
import com.example.ui.viewmodel.AppDestination

@Composable
fun OverviewScreen(
    datasets: List<DatasetEntity>,
    issues: List<QualityIssueEntity>,
    runs: List<QualityRunEntity>,
    onNavigate: (AppDestination) -> Unit,
    onSelectDataset: (String) -> Unit,
    onSelectIssue: (QualityIssueEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRecords = datasets.sumOf { it.rowCount }
    val latestRun = runs.firstOrNull()
    val averageScore = if (runs.isNotEmpty()) runs.map { it.overallScore }.average().toInt() else 85
    val criticalIssues = issues.filter { it.severity == "CRITICAL" }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(horizontal = 16.dp)
            .testTag("overview_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Hero Score Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_score_banner"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(16.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, IbmBluePrimary.copy(alpha = 0.3f))))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "DATA HEALTH INDEX",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Enterprise Reliability",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Monitored across IBM Db2 schema DATASENTRY with real-time root cause inference.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row {
                            Button(
                                onClick = { onNavigate(AppDestination.AI_COPILOT) },
                                colors = ButtonDefaults.buttonColors(containerColor = IbmBluePrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("ask_copilot_btn")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Ask Copilot", fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedButton(
                                onClick = { onNavigate(AppDestination.DATA_QUALITY) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("view_issues_btn")
                            ) {
                                Text("Quality Audit", fontSize = 12.sp, color = CyanAccent)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))
                    QualityScoreGauge(score = averageScore, size = 110.dp)
                }
            }
        }

        // 4 KPI Metrics
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricKpiCard(
                        title = "TOTAL DATASETS",
                        value = "${datasets.size}",
                        subtitle = "Persistent in Db2",
                        icon = Icons.Default.Layers,
                        iconColor = IbmBluePrimary,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "TOTAL RECORDS",
                        value = "${String.format("%,d", totalRecords)}",
                        subtitle = "Indexed rows",
                        icon = Icons.Default.DataObject,
                        iconColor = TealAccent,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricKpiCard(
                        title = "CRITICAL DEFECTS",
                        value = "${criticalIssues.size}",
                        subtitle = "Action required",
                        icon = Icons.Default.Warning,
                        iconColor = ScoreLowRed,
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "AI INVESTIGATIONS",
                        value = "${issues.size}",
                        subtitle = "Hypotheses generated",
                        icon = Icons.Default.BugReport,
                        iconColor = ScoreMedAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Quick Navigation to Upload Demo
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(AppDestination.UPLOAD) }
                    .testTag("upload_demo_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CyanAccent.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DataObject, contentDescription = null, tint = CyanAccent)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Upload / Demo Dataset Workflow", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text("Import CSV or load TechX demo datasets directly into Db2", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color(0xFF64748B))
                }
            }
        }

        // Active Datasets Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Active Datasets in Db2",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "View All (${datasets.size})",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(AppDestination.DATASETS) }
                )
            }
        }

        items(datasets) { dataset ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onSelectDataset(dataset.id)
                        onNavigate(AppDestination.DATASETS)
                    }
                    .testTag("dataset_card_${dataset.name}"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = dataset.name,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        if (dataset.isDemo) {
                            Text(
                                text = "DEMO DATASET",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ScoreMedAmber,
                                modifier = Modifier
                                    .background(Color(0xFF451A03), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dataset.description,
                        fontSize = 12.sp,
                        color = Color(0xFF94A3B8),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Rows: ${dataset.rowCount}  •  Cols: ${dataset.columnCount}  •  ${dataset.fileSizeFormatted}",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = dataset.db2TableName,
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // Top Critical Issues
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Priority Root Causes to Resolve",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "All Issues (${issues.size})",
                    fontSize = 12.sp,
                    color = CyanAccent,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onNavigate(AppDestination.DATA_QUALITY) }
                )
            }
        }

        items(issues.take(4)) { issue ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectIssue(issue) }
                    .testTag("priority_issue_${issue.columnName}"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(10.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SeverityBadge(severity = issue.severity)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = issue.columnName,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = "${issue.affectedRecordsCount} records",
                            fontSize = 11.sp,
                            color = ScoreMedAmber,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = issue.description,
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Cause: ${issue.rootCauseHypothesis}",
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
