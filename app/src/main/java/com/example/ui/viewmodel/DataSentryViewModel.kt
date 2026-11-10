package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AgentExecutionResult
import com.example.data.local.AppDatabase
import com.example.data.model.AiReportEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.DatasetEntity
import com.example.data.model.Db2ConfigEntity
import com.example.data.model.QualityIssueEntity
import com.example.data.model.QualityRunEntity
import com.example.data.model.QueryAuditEntity
import com.example.data.repository.DataSentryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppDestination(val title: String) {
    OVERVIEW("Overview"),
    DATASETS("Datasets"),
    UPLOAD("Upload"),
    DATA_QUALITY("Data Quality"),
    ROOT_CAUSE("Root Cause"),
    AI_COPILOT("AI Copilot"),
    REPORTS("Reports"),
    SQL_INSPECTOR("SQL Inspector"),
    SETTINGS("Settings")
}

class DataSentryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = DataSentryRepository(AppDatabase.getDatabase(application))

    val datasets: StateFlow<List<DatasetEntity>> = repository.getAllDatasetsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val issues: StateFlow<List<QualityIssueEntity>> = repository.getAllIssuesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val runs: StateFlow<List<QualityRunEntity>> = repository.getAllRunsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reports: StateFlow<List<AiReportEntity>> = repository.getAllReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.getChatMessagesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val queryAudits: StateFlow<List<QueryAuditEntity>> = repository.getRecentAuditsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val db2Config: StateFlow<Db2ConfigEntity?> = repository.getDb2ConfigFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _currentDestination = MutableStateFlow(AppDestination.OVERVIEW)
    val currentDestination: StateFlow<AppDestination> = _currentDestination.asStateFlow()

    private val _selectedDatasetId = MutableStateFlow<String?>(null)
    val selectedDatasetId: StateFlow<String?> = _selectedDatasetId.asStateFlow()

    private val _selectedIssue = MutableStateFlow<QualityIssueEntity?>(null)
    val selectedIssue: StateFlow<QualityIssueEntity?> = _selectedIssue.asStateFlow()

    private val _selectedReport = MutableStateFlow<AiReportEntity?>(null)
    val selectedReport: StateFlow<AiReportEntity?> = _selectedReport.asStateFlow()

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isSendingChat = MutableStateFlow(false)
    val isSendingChat: StateFlow<Boolean> = _isSendingChat.asStateFlow()

    private val _lastAgentResult = MutableStateFlow<AgentExecutionResult?>(null)
    val lastAgentResult: StateFlow<AgentExecutionResult?> = _lastAgentResult.asStateFlow()

    private val _previewRows = MutableStateFlow<List<Map<String, String>>>(emptyList())
    val previewRows: StateFlow<List<Map<String, String>>> = _previewRows.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isTestingConnection = MutableStateFlow(false)
    val isTestingConnection: StateFlow<Boolean> = _isTestingConnection.asStateFlow()

    init {
        viewModelScope.launch {
            repository.initializeIfNeeded()
        }
    }

    fun navigateTo(dest: AppDestination) {
        _currentDestination.value = dest
    }

    fun selectDataset(datasetId: String) {
        _selectedDatasetId.value = datasetId
        viewModelScope.launch {
            _previewRows.value = repository.getRowsForDataset(datasetId, 50)
        }
    }

    fun selectIssue(issue: QualityIssueEntity) {
        _selectedIssue.value = issue
        _currentDestination.value = AppDestination.ROOT_CAUSE
    }

    fun selectReport(report: AiReportEntity) {
        _selectedReport.value = report
        _currentDestination.value = AppDestination.REPORTS
    }

    fun setChatInput(text: String) {
        _chatInput.value = text
    }

    fun sendChatMessage(promptText: String? = null) {
        val messageToSend = promptText ?: _chatInput.value.trim()
        if (messageToSend.isBlank()) return

        _isSendingChat.value = true
        _chatInput.value = ""

        viewModelScope.launch {
            try {
                val result = repository.sendMessage(messageToSend, _selectedDatasetId.value)
                _lastAgentResult.value = result
            } catch (e: Exception) {
                _statusMessage.value = "AI Agent error: ${e.message}"
            } finally {
                _isSendingChat.value = false
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun uploadCustomCsv(name: String, description: String, csvContent: String) {
        viewModelScope.launch {
            try {
                val created = repository.uploadCustomCsv(name, description, csvContent)
                _selectedDatasetId.value = created.id
                _previewRows.value = repository.getRowsForDataset(created.id, 50)
                _statusMessage.value = "Dataset '${created.name}' imported into Db2 successfully."
                _currentDestination.value = AppDestination.DATASETS
            } catch (e: Exception) {
                _statusMessage.value = "CSV Upload failed: ${e.message}"
            }
        }
    }

    fun reRunProfiling(datasetId: String) {
        viewModelScope.launch {
            _statusMessage.value = "Running automated Db2 profiling..."
            val result = repository.runDataProfiling(datasetId)
            if (result != null) {
                _statusMessage.value = "Profiling completed! Overall Score: ${result.qualityRun.overallScore}/100"
            }
        }
    }

    fun deleteDataset(datasetId: String) {
        viewModelScope.launch {
            repository.deleteDataset(datasetId)
            if (_selectedDatasetId.value == datasetId) {
                _selectedDatasetId.value = null
                _previewRows.value = emptyList()
            }
            _statusMessage.value = "Dataset removed from Db2 storage."
        }
    }

    fun testDb2Connection(config: Db2ConfigEntity) {
        _isTestingConnection.value = true
        viewModelScope.launch {
            val success = repository.testDb2Connection(config)
            _isTestingConnection.value = false
            _statusMessage.value = if (success) {
                "Connected successfully to IBM Db2 LUW on host ${config.host}:${config.port}"
            } else {
                "Unable to reach Db2 server. Running in Demo Mode with synthetic data."
            }
        }
    }

    fun saveDb2Config(config: Db2ConfigEntity) {
        viewModelScope.launch {
            repository.saveDb2Config(config)
            _statusMessage.value = "Db2 Configuration updated."
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
