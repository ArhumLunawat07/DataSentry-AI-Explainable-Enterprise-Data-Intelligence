package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DataSentryBottomBar
import com.example.ui.components.DataSentryTopBar
import com.example.ui.screens.CopilotScreen
import com.example.ui.screens.DataQualityScreen
import com.example.ui.screens.DatasetsScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.RootCauseScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SqlInspectorScreen
import com.example.ui.screens.UploadScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AppDestination
import com.example.ui.viewmodel.DataSentryViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DataSentryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
                val datasets by viewModel.datasets.collectAsStateWithLifecycle()
                val issues by viewModel.issues.collectAsStateWithLifecycle()
                val runs by viewModel.runs.collectAsStateWithLifecycle()
                val reports by viewModel.reports.collectAsStateWithLifecycle()
                val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
                val queryAudits by viewModel.queryAudits.collectAsStateWithLifecycle()
                val db2Config by viewModel.db2Config.collectAsStateWithLifecycle()

                val selectedDatasetId by viewModel.selectedDatasetId.collectAsStateWithLifecycle()
                val selectedIssue by viewModel.selectedIssue.collectAsStateWithLifecycle()
                val selectedReport by viewModel.selectedReport.collectAsStateWithLifecycle()
                val previewRows by viewModel.previewRows.collectAsStateWithLifecycle()

                val chatInput by viewModel.chatInput.collectAsStateWithLifecycle()
                val isSendingChat by viewModel.isSendingChat.collectAsStateWithLifecycle()
                val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()
                val isTestingConnection by viewModel.isTestingConnection.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(statusMessage) {
                    statusMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearStatusMessage()
                    }
                }

                // Handle back press to return to Overview when inside sub-screens
                if (currentDestination != AppDestination.OVERVIEW) {
                    BackHandler {
                        viewModel.navigateTo(AppDestination.OVERVIEW)
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        DataSentryTopBar(
                            db2Config = db2Config,
                            onOpenSettings = { viewModel.navigateTo(AppDestination.SETTINGS) }
                        )
                    },
                    bottomBar = {
                        DataSentryBottomBar(
                            currentDestination = currentDestination,
                            onNavigate = { viewModel.navigateTo(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentDestination) {
                            AppDestination.OVERVIEW -> {
                                OverviewScreen(
                                    datasets = datasets,
                                    issues = issues,
                                    runs = runs,
                                    onNavigate = { viewModel.navigateTo(it) },
                                    onSelectDataset = { viewModel.selectDataset(it) },
                                    onSelectIssue = { viewModel.selectIssue(it) }
                                )
                            }
                            AppDestination.DATASETS -> {
                                DatasetsScreen(
                                    datasets = datasets,
                                    selectedDatasetId = selectedDatasetId,
                                    previewRows = previewRows,
                                    onSelectDataset = { viewModel.selectDataset(it) },
                                    onReRunProfiling = { viewModel.reRunProfiling(it) },
                                    onDeleteDataset = { viewModel.deleteDataset(it) }
                                )
                            }
                            AppDestination.UPLOAD -> {
                                UploadScreen(
                                    onUploadCsv = { name, desc, csv ->
                                        viewModel.uploadCustomCsv(name, desc, csv)
                                    }
                                )
                            }
                            AppDestination.DATA_QUALITY -> {
                                DataQualityScreen(
                                    runs = runs,
                                    issues = issues,
                                    onSelectIssue = { viewModel.selectIssue(it) }
                                )
                            }
                            AppDestination.ROOT_CAUSE -> {
                                RootCauseScreen(
                                    issue = selectedIssue,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                            }
                            AppDestination.AI_COPILOT -> {
                                CopilotScreen(
                                    messages = chatMessages,
                                    chatInput = chatInput,
                                    isSending = isSendingChat,
                                    onInputChange = { viewModel.setChatInput(it) },
                                    onSendMessage = { viewModel.sendChatMessage(it) },
                                    onClearChat = { viewModel.clearChat() }
                                )
                            }
                            AppDestination.REPORTS -> {
                                ReportsScreen(
                                    reports = reports,
                                    selectedReport = selectedReport,
                                    onSelectReport = { viewModel.selectReport(it) },
                                    onGenerateReport = { viewModel.sendChatMessage("generate report") }
                                )
                            }
                            AppDestination.SQL_INSPECTOR -> {
                                SqlInspectorScreen(
                                    queryAudits = queryAudits
                                )
                            }
                            AppDestination.SETTINGS -> {
                                SettingsScreen(
                                    db2Config = db2Config,
                                    isTestingConnection = isTestingConnection,
                                    onTestConnection = { viewModel.testDb2Connection(it) },
                                    onSaveConfig = { viewModel.saveDb2Config(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
