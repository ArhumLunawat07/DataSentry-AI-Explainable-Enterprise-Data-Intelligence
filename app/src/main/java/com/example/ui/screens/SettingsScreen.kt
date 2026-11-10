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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.data.db2.Db2Manager
import com.example.data.model.Db2ConfigEntity
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
fun SettingsScreen(
    db2Config: Db2ConfigEntity?,
    isTestingConnection: Boolean,
    onTestConnection: (Db2ConfigEntity) -> Unit,
    onSaveConfig: (Db2ConfigEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentConfig = db2Config ?: Db2ConfigEntity()

    var host by remember(currentConfig) { mutableStateOf(currentConfig.host) }
    var port by remember(currentConfig) { mutableStateOf(currentConfig.port.toString()) }
    var database by remember(currentConfig) { mutableStateOf(currentConfig.database) }
    var username by remember(currentConfig) { mutableStateOf(currentConfig.username) }
    var schema by remember(currentConfig) { mutableStateOf(currentConfig.schemaName) }
    var useSsl by remember(currentConfig) { mutableStateOf(currentConfig.useSsl) }
    var vectorSupport by remember(currentConfig) { mutableStateOf(currentConfig.vectorSupportEnabled) }
    var showDdl by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .padding(16.dp)
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "IBM Db2 Connection & Engine Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Configure IBM Db2 LUW or Db2 on Cloud connection parameters and vector search capabilities.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF94A3B8)
            )
        }

        // Live Connection Status Banner
        item {
            val isConnected = currentConfig.isConnected
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connection_status_card"),
                colors = CardDefaults.cardColors(
                    containerColor = if (isConnected) Color(0xFF064E3B) else Color(0xFF451A03)
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isConnected) ScoreHighGreen.copy(alpha = 0.5f) else ScoreMedAmber.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isConnected) ScoreHighGreen.copy(alpha = 0.2f) else ScoreMedAmber.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isConnected) ScoreHighGreen else ScoreMedAmber,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (isConnected) "CONNECTED: IBM Db2 Enterprise" else "DEMO MODE (Db2 Disconnected)",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Text(
                            text = if (isConnected)
                                "Version: ${currentConfig.versionInfo} • SSL Enabled"
                            else
                                "Synthetic dataset mode active. Synthetic records are never represented as live Db2 records.",
                            fontSize = 11.sp,
                            color = if (isConnected) Color(0xFFA7F3D0) else Color(0xFFFDE68A)
                        )
                    }
                }
            }
        }

        // Configuration Form Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connection_form_card"),
                colors = CardDefaults.cardColors(containerColor = NavySurface),
                shape = RoundedCornerShape(12.dp),
                border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.Brush.linearGradient(listOf(NavyCardBorder, NavyCardBorder)))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "IBM Db2 Server Credentials",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = host,
                        onValueChange = { host = it },
                        label = { Text("Db2 Hostname / AppDomain URL") },
                        modifier = Modifier.fillMaxWidth().testTag("input_db2_host"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyanAccent,
                            unfocusedBorderColor = NavyCardBorder,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = port,
                            onValueChange = { port = it },
                            label = { Text("Port (SSL)") },
                            modifier = Modifier.weight(1f).testTag("input_db2_port"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = database,
                            onValueChange = { database = it },
                            label = { Text("Database (e.g. BLUDB)") },
                            modifier = Modifier.weight(1f).testTag("input_db2_database"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("User (e.g. db2admin)") },
                            modifier = Modifier.weight(1f).testTag("input_db2_user"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = schema,
                            onValueChange = { schema = it },
                            label = { Text("Schema Name") },
                            modifier = Modifier.weight(1f).testTag("input_db2_schema"),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyanAccent,
                                unfocusedBorderColor = NavyCardBorder,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // SSL Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Use SSL/TLS Encryption", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Enforces encrypted network transport on port 32551", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = useSsl,
                            onCheckedChange = { useSsl = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = IbmBluePrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Vector Search Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("IBM Db2 Native Vector Search", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            Text("Utilizes VECTOR(768, FLOAT) and VECTOR_DISTANCE with relational fallback", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                        Switch(
                            checked = vectorSupport,
                            onCheckedChange = { vectorSupport = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = CyanAccent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                val updated = currentConfig.copy(
                                    host = host,
                                    port = port.toIntOrNull() ?: 32551,
                                    database = database,
                                    username = username,
                                    schemaName = schema,
                                    useSsl = useSsl,
                                    vectorSupportEnabled = vectorSupport
                                )
                                onTestConnection(updated)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IbmBluePrimary),
                            shape = RoundedCornerShape(8.dp),
                            enabled = !isTestingConnection,
                            modifier = Modifier.weight(1f).testTag("btn_test_db2_connection")
                        ) {
                            if (isTestingConnection) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Connecting...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Test Connection", fontSize = 12.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                val updated = currentConfig.copy(
                                    host = host,
                                    port = port.toIntOrNull() ?: 32551,
                                    database = database,
                                    username = username,
                                    schemaName = schema,
                                    useSsl = useSsl,
                                    vectorSupportEnabled = vectorSupport
                                )
                                onSaveConfig(updated)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).testTag("btn_save_db2_config")
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Config", fontSize = 12.sp, color = CyanAccent)
                        }
                    }
                }
            }
        }

        // Schema DDL Viewer
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ddl_viewer_card"),
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Code, contentDescription = null, tint = TealAccent, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("IBM Db2 DDL Schema Script", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                        }
                        Text(
                            text = if (showDdl) "Hide Script" else "View DDL",
                            color = CyanAccent,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { showDdl = !showDdl }
                        )
                    }

                    if (showDdl) {
                        Spacer(modifier = Modifier.height(10.dp))
                        val ddl = remember(schema, vectorSupport) {
                            Db2Manager.generateDb2DDL(schema, vectorSupport)
                        }
                        Surface(
                            color = NavyDark,
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = ddl,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFFA7F3D0),
                                modifier = Modifier.padding(10.dp)
                            )
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
