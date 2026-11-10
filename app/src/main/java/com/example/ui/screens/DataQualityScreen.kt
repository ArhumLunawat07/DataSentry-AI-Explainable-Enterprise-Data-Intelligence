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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
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

@Composable
fun DataQualityScreen(
    runs: List<QualityRunEntity>,
    issues: List<QualityIssueEntity>,
    onSelectIssue: (QualityIssueEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val latestRun = runs.firstOrNull()
    var selectedSeverityFilter by remember { mutableStateOf("ALL") }

    val filteredIssues = remember(issues, selectedSeverityFilter) {
        if (selectedSeverityFilter == "ALL") issues
        else issues.filter { it.severity.equals(selectedSeverityFilter, ignoreCase = true) }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .testTag("data_quality_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Automated Data Profiling",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Transparent scoring based on completeness, uniqueness, format validity, and statistical distribution.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Run Summary Card
        if (latestRun != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("quality_run_summary_card"),
                    colors = CardDefaults.cardColors(containerColor = NavySurface),
                    shape = RoundedCornerShape(12.dp),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("LATEST PROFILING RUN", fontSize = 10.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                                Text("Execution Status: ${latestRun.status}", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                                Text("Latency: ${latestRun.executionTimeMs} ms", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                            QualityScoreGauge(score = latestRun.overallScore, size = 80.dp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Metric counters row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(NavySurfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("MISSING", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${latestRun.missingValuesCount}", fontSize = 14.sp, color = if (latestRun.missingValuesCount > 0) ScoreLowRed else Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("DUPLICATES", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${latestRun.duplicateCount}", fontSize = 14.sp, color = if (latestRun.duplicateCount > 0) ScoreMedAmber else Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("FORMAT ERRORS", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${latestRun.formatErrorsCount}", fontSize = 14.sp, color = if (latestRun.formatErrorsCount > 0) ScoreMedAmber else Color.White, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("ANOMALIES", fontSize = 9.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                Text("${latestRun.anomalyCount}", fontSize = 14.sp, color = if (latestRun.anomalyCount > 0) ScoreLowRed else Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Severity Filter Chips
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.FilterList, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Filter by Severity:", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("ALL", "CRITICAL", "HIGH", "MEDIUM", "LOW").forEach { filter ->
                    val isSelected = selectedSeverityFilter == filter
                    item {
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedSeverityFilter = filter },
                            label = { Text(filter, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = IbmBluePrimary,
                                selectedLabelColor = Color.White,
                                containerColor = NavySurfaceVariant,
                                labelColor = Color(0xFFCBD5E1)
                            )
                        )
                    }
                }
            }
        }

        // Issues Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Discovered Issues (${filteredIssues.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Tap to inspect root cause",
                    fontSize = 11.sp,
                    color = CyanAccent
                )
            }
        }

        // Issues List
        items(filteredIssues) { issue ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectIssue(issue) }
                    .testTag("issue_card_${issue.id}"),
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

                        // Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(NavySurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = issue.issueType.replace("_", " "),
                                fontSize = 10.sp,
                                color = CyanAccent,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Fact indicator vs Hypothesis
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(ScoreHighGreen)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ACTUAL DETECTED DEFECT IN DB2",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ScoreHighGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = issue.description,
                        fontSize = 12.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // AI Hypothesis
                    Surface(
                        color = NavySurfaceVariant,
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "AI Root Cause Hypothesis (${(issue.confidenceScore * 100).toInt()}% confidence)",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CyanAccent
                                )
                                Text(
                                    text = issue.rootCauseHypothesis,
                                    fontSize = 11.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Impact: ${issue.affectedRecordsCount} rows affected",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Investigate", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                            Icon(Icons.Default.ArrowForward, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
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
