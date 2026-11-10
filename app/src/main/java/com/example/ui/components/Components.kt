package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.ToolInvocation
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IbmBlueLight
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
fun DataSentryTopBar(
    db2Config: Db2ConfigEntity?,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_app_bar"),
        color = NavyDark,
        tonalElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("brand_header")
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(listOf(IbmBluePrimary, CyanAccent))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Assessment,
                        contentDescription = "DataSentry Logo",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "DataSentry",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "AI",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = CyanAccent,
                            modifier = Modifier
                                .background(NavySurfaceVariant, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = "IBM Db2 Enterprise Intelligence",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            // Connection Indicator (Accurately reflects Db2 or Demo Mode)
            val isConnected = db2Config?.isConnected == true
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isConnected) Color(0xFF064E3B) else Color(0xFF451A03))
                    .border(
                        1.dp,
                        if (isConnected) ScoreHighGreen.copy(alpha = 0.5f) else ScoreMedAmber.copy(alpha = 0.5f),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable(onClick = onOpenSettings)
                    .padding(horizontal = 10.dp, vertical = 5.dp)
                    .testTag("db2_connection_badge"),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) ScoreHighGreen else ScoreMedAmber)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isConnected) "Db2 CONNECTED" else "DEMO MODE (Db2)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isConnected) ScoreHighGreen else ScoreMedAmber
                )
            }
        }
    }
}

@Composable
fun DataSentryBottomBar(
    currentDestination: AppDestination,
    onNavigate: (AppDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier.testTag("bottom_nav_bar"),
        containerColor = NavyDark,
        tonalElevation = 8.dp
    ) {
        val navItems = listOf(
            Triple(AppDestination.OVERVIEW, Icons.Default.Dashboard, "Overview"),
            Triple(AppDestination.DATASETS, Icons.Default.Layers, "Datasets"),
            Triple(AppDestination.DATA_QUALITY, Icons.Default.Assessment, "Quality"),
            Triple(AppDestination.AI_COPILOT, Icons.Default.AutoAwesome, "Copilot"),
            Triple(AppDestination.REPORTS, Icons.Default.Description, "Reports"),
            Triple(AppDestination.SQL_INSPECTOR, Icons.Default.Code, "SQL")
        )

        navItems.forEach { (destination, icon, label) ->
            val selected = currentDestination == destination
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = CyanAccent,
                    indicatorColor = IbmBluePrimary,
                    unselectedIconColor = Color(0xFF64748B),
                    unselectedTextColor = Color(0xFF64748B)
                ),
                modifier = Modifier.testTag("nav_item_${destination.name.lowercase()}")
            )
        }
    }
}

@Composable
fun QualityScoreGauge(
    score: Int,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    val animatedScore by animateFloatAsState(
        targetValue = score.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "score_anim"
    )

    val strokeColor = when {
        score >= 85 -> ScoreHighGreen
        score >= 65 -> ScoreMedAmber
        else -> ScoreLowRed
    }

    val gradeText = when {
        score >= 90 -> "GRADE A"
        score >= 80 -> "GRADE B"
        score >= 65 -> "GRADE C"
        else -> "CRITICAL"
    }

    Box(
        modifier = modifier
            .size(size)
            .testTag("quality_score_gauge"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 10.dp.toPx()
            // Track circle
            drawArc(
                color = NavyCardBorder,
                startAngle = 135f,
                sweepAngle = 270f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Progress arc
            val sweep = (animatedScore / 100f) * 270f
            drawArc(
                brush = Brush.sweepGradient(
                    listOf(strokeColor.copy(alpha = 0.6f), strokeColor, CyanAccent)
                ),
                startAngle = 135f,
                sweepAngle = sweep,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$score",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
            Text(
                text = "/ 100",
                fontSize = 12.sp,
                color = Color(0xFF94A3B8)
            )
            Text(
                text = gradeText,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = strokeColor
            )
        }
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconColor: Color = CyanAccent,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("kpi_card_${title.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = NavySurface),
        shape = RoundedCornerShape(12.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
    ) {
        Row(
            modifier = Modifier
                .padding(14.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64748B),
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
fun SeverityBadge(severity: String, modifier: Modifier = Modifier) {
    val (bgColor, fgColor) = when (severity.uppercase()) {
        "CRITICAL" -> Color(0xFF7F1D1D) to Color(0xFFFCA5A5)
        "HIGH" -> Color(0xFF7C2D12) to Color(0xFFFDBA74)
        "MEDIUM" -> Color(0xFF78350F) to Color(0xFFFDE68A)
        else -> Color(0xFF1E3A8A) to Color(0xFF93C5FD)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(bgColor)
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .testTag("severity_badge_${severity.lowercase()}")
    ) {
        Text(
            text = severity.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = fgColor
        )
    }
}

@Composable
fun ToolExecutionCard(
    tool: ToolInvocation,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tool_card_${tool.toolName.lowercase().replace(" ", "_")}"),
        colors = CardDefaults.cardColors(containerColor = NavySurfaceVariant),
        shape = RoundedCornerShape(8.dp),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(IbmBluePrimary.copy(alpha = 0.4f), CyanAccent.copy(alpha = 0.2f))))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "⚡", fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = tool.toolName,
                        fontWeight = FontWeight.Bold,
                        color = CyanAccent,
                        fontSize = 12.sp
                    )
                }
                Text(
                    text = "${tool.executionTimeMs}ms • ${tool.recordsFound} rows",
                    fontSize = 10.sp,
                    color = Color(0xFF94A3B8)
                )
            }

            if (tool.sqlQuery.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = NavyDark,
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tool.sqlQuery,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        color = Color(0xFFA7F3D0),
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            if (tool.outputSummary.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.outputSummary,
                    fontSize = 11.sp,
                    color = Color(0xFFCBD5E1)
                )
            }
        }
    }
}
