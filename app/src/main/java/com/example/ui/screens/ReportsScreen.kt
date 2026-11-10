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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AiReportEntity
import com.example.ui.components.QualityScoreGauge
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IbmBluePrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.ScoreHighGreen
import com.example.ui.theme.ScoreLowRed
import com.example.ui.theme.ScoreMedAmber
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ReportsScreen(
    reports: List<AiReportEntity>,
    selectedReport: AiReportEntity?,
    onSelectReport: (AiReportEntity) -> Unit,
    onGenerateReport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeReport = selectedReport ?: reports.firstOrNull()

    val findings = remember(activeReport?.keyFindingsJson) {
        if (activeReport != null) parseJsonArray(activeReport.keyFindingsJson) else emptyList()
    }
    val recommendations = remember(activeReport?.recommendationsJson) {
        if (activeReport != null) parseJsonArray(activeReport.recommendationsJson) else emptyList()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .testTag("reports_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Executive Intelligence Reports",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Persisted in IBM Db2 table DATASENTRY.AI_REPORTS",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        }

        // Available Reports List
        if (reports.size > 1) {
            item {
                Text(
                    text = "Saved Audit Reports (${reports.size})",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
            }

            items(reports) { rep ->
                val isSelected = rep.id == activeReport?.id
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectReport(rep) }
                        .testTag("report_item_${rep.id}"),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) NavySurfaceVariant else NavySurface
                    ),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) CyanAccent else NavyCardBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = rep.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                            Text(
                                text = "Dataset: ${rep.datasetName} • Score: ${rep.qualityScore}/100",
                                fontSize = 11.sp,
                                color = CyanAccent
                            )
                        }
                        Text(
                            text = formatDate(rep.generatedAt),
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // Active Report Full View
        if (activeReport != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("active_report_view"),
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = activeReport.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "Generated ${formatDate(activeReport.generatedAt)}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            QualityScoreGauge(score = activeReport.qualityScore, size = 70.dp)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Executive Summary
                        Text("Executive Summary", fontWeight = FontWeight.Bold, color = CyanAccent, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeReport.executiveSummary,
                            fontSize = 12.sp,
                            color = Color(0xFFCBD5E1),
                            lineHeight = 18.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Key Findings
                        Text("Key Discovered Findings", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        findings.forEach { finding ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("• ", color = ScoreMedAmber, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Text(text = finding, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Actionable Recommendations
                        Text("Actionable Recommendations", fontWeight = FontWeight.Bold, color = ScoreHighGreen, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        recommendations.forEachIndexed { idx, rec ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text("${idx + 1}. ", color = ScoreHighGreen, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                Text(text = rec, fontSize = 11.sp, color = Color(0xFFE2E8F0))
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Business Impact
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ScoreLowRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Business Impact", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = activeReport.businessImpact,
                            fontSize = 11.sp,
                            color = Color(0xFFCBD5E1)
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

private fun parseJsonArray(jsonStr: String): List<String> {
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            list.add(array.getString(i))
        }
        list
    } catch (e: Exception) {
        listOf("Standard data quality findings logged.")
    }
}
