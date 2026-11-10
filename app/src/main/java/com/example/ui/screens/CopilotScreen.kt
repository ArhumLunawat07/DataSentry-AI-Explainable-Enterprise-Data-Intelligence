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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ToolInvocation
import com.example.ui.components.ToolExecutionCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IbmBluePrimary
import com.example.ui.theme.NavyCardBorder
import com.example.ui.theme.NavyDark
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceVariant
import com.example.ui.theme.TealAccent
import org.json.JSONArray

@Composable
fun CopilotScreen(
    messages: List<ChatMessageEntity>,
    chatInput: String,
    isSending: Boolean,
    onInputChange: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    onClearChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestedPrompts = listOf(
        "Which dataset has the most errors?",
        "Find duplicate transactions",
        "Explain unusual sales patterns",
        "Which columns have missing values?",
        "What should I fix first?",
        "Generate an executive summary"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(NavyDark)
            .testTag("copilot_screen")
    ) {
        // Copilot Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavySurface)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(IbmBluePrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text("AI Enterprise Copilot", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                    Text("LangChain Agent + Google Gemini + IBM Db2", fontSize = 10.sp, color = CyanAccent)
                }
            }

            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_chat_btn")
            ) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear History", tint = Color(0xFF94A3B8))
            }
        }

        // Suggested Prompt Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyDark)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(suggestedPrompts) { prompt ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NavySurfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder),
                    modifier = Modifier
                        .clickable { onSendMessage(prompt) }
                        .testTag("chip_${prompt.take(15).lowercase().replace(" ", "_")}")
                ) {
                    Text(
                        text = prompt,
                        fontSize = 11.sp,
                        color = Color(0xFFE2E8F0),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Messages List
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages) { msg ->
                val isUser = msg.role == "user"
                val toolInvocations = remember(msg.toolCallsJson) {
                    parseToolInvocations(msg.toolCallsJson)
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("chat_msg_${msg.id}"),
                    horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                ) {
                    // Tool Calling Badges if assistant called tools
                    if (!isUser && toolInvocations.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(0.95f)
                                .padding(bottom = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "LangChain Tool Calls Executed:",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF94A3B8)
                            )
                            toolInvocations.forEach { tool ->
                                ToolExecutionCard(tool = tool)
                            }
                        }
                    }

                    // Message Bubble
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isUser) IbmBluePrimary else NavySurface,
                        border = if (!isUser) androidx.compose.foundation.BorderStroke(1.dp, NavyCardBorder) else null,
                        modifier = Modifier.fillMaxWidth(if (isUser) 0.85f else 0.95f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = msg.content,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 18.sp
                            )

                            // Grounded SQL preview if present
                            if (!isUser && msg.sqlExecuted.isNotBlank() && !msg.content.contains("```sql")) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = NavyDark,
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Code, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = msg.sqlExecuted,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 10.sp,
                                            color = Color(0xFFA7F3D0)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (isSending) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 8.dp)
                    ) {
                        CircularProgressIndicator(
                            color = CyanAccent,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "LangChain agent invoking Db2 schema inspector & profiler...",
                            fontSize = 11.sp,
                            color = CyanAccent,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }

        // Input Field
        Surface(
            color = NavySurface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = chatInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Ask about datasets, anomalies, or SQL...", fontSize = 12.sp, color = Color(0xFF64748B)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanAccent,
                        unfocusedBorderColor = NavyCardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    maxLines = 3,
                    textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = { onSendMessage(null) },
                    enabled = chatInput.isNotBlank() && !isSending,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (chatInput.isNotBlank() && !isSending) IbmBluePrimary else NavySurfaceVariant)
                        .testTag("send_chat_btn")
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send",
                        tint = if (chatInput.isNotBlank() && !isSending) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

private fun parseToolInvocations(jsonStr: String): List<ToolInvocation> {
    return try {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<ToolInvocation>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                ToolInvocation(
                    toolName = obj.optString("toolName", "Tool"),
                    argumentsSummary = obj.optString("arguments", "{}"),
                    sqlQuery = obj.optString("sql", ""),
                    executionTimeMs = obj.optLong("latencyMs", 0),
                    status = obj.optString("status", "SUCCESS"),
                    recordsFound = obj.optInt("recordsFound", 0),
                    outputSummary = obj.optString("output", "")
                )
            )
        }
        list
    } catch (e: Exception) {
        emptyList()
    }
}
