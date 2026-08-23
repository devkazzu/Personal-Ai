package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.*
import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.repository.PersonalOSRepository
import com.example.data.repository.UnifiedSearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

enum class OSTab(val label: String) {
    DASHBOARD("Hub"),
    CHAT("AI Chat"),
    TASKS_CALENDAR("Tasks & Agenda"),
    NOTES_DOCS("Notes & Docs"),
    MEMORY_VAULT("Memory & Vault"),
    AGENT_CENTER("Agent OS")
}

class PersonalOSViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    val repository = PersonalOSRepository(db)
    val speechSynthesizer = SpeechSynthesizer(application)

    // Current Navigation Tab
    private val _currentTab = MutableStateFlow(OSTab.DASHBOARD)
    val currentTab: StateFlow<OSTab> = _currentTab.asStateFlow()

    // Voice Overlay State
    private val _isVoiceOverlayOpen = MutableStateFlow(false)
    val isVoiceOverlayOpen: StateFlow<Boolean> = _isVoiceOverlayOpen.asStateFlow()

    // AI Processing State
    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    // Speech Synthesizer speaking state
    val isSpeaking: StateFlow<Boolean> = speechSynthesizer.isSpeaking

    // Voice transcript preview
    private val _voiceTranscript = MutableStateFlow("")
    val voiceTranscript: StateFlow<String> = _voiceTranscript.asStateFlow()

    // Unified Search
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResult = MutableStateFlow<UnifiedSearchResult?>(null)
    val searchResult: StateFlow<UnifiedSearchResult?> = _searchResult.asStateFlow()

    // Document Brain Q&A Output
    private val _docAnswer = MutableStateFlow<String?>(null)
    val docAnswer: StateFlow<String?> = _docAnswer.asStateFlow()

    private val _isDocAnalyzing = MutableStateFlow(false)
    val isDocAnalyzing: StateFlow<Boolean> = _isDocAnalyzing.asStateFlow()

    // Security & Private Vault
    private val _isVaultUnlocked = MutableStateFlow(false)
    val isVaultUnlocked: StateFlow<Boolean> = _isVaultUnlocked.asStateFlow()

    private val _vaultErrorMessage = MutableStateFlow<String?>(null)
    val vaultErrorMessage: StateFlow<String?> = _vaultErrorMessage.asStateFlow()

    // Data streams from repository
    val activeTasks: StateFlow<List<TaskEntity>> = repository.activeTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotes: StateFlow<List<NoteEntity>> = repository.allNotes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemories: StateFlow<List<MemoryEntity>> = repository.allMemories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allEvents: StateFlow<List<CalendarEventEntity>> = repository.allEvents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocs: StateFlow<List<DocEntity>> = repository.allDocs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingPlans: StateFlow<List<AgentPlanEntity>> = repository.pendingPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPlans: StateFlow<List<AgentPlanEntity>> = repository.allPlans
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
        }
    }

    fun selectTab(tab: OSTab) {
        _currentTab.value = tab
    }

    fun openVoiceOverlay() {
        _isVoiceOverlayOpen.value = true
    }

    fun closeVoiceOverlay() {
        _isVoiceOverlayOpen.value = false
        speechSynthesizer.stop()
    }

    // --- Unified Search Handler ---
    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResult.value = null
            } else {
                _searchResult.value = repository.searchAll(query)
            }
        }
    }

    // --- Chat & Voice Dispatch ---
    fun sendChatMessage(text: String, isVoice: Boolean = false) {
        if (text.isBlank()) return
        val userPrompt = text.trim()

        viewModelScope.launch {
            // 1. Save User Message
            repository.insertMessage(
                ChatMessageEntity(
                    role = MessageRole.USER,
                    content = userPrompt,
                    isVoiceMode = isVoice
                )
            )

            _isAiThinking.value = true

            // 2. Fetch context & call Gemini
            val userContext = repository.getUserContextSummary()
            val history = allMessages.value.takeLast(6).map { it.role.name to it.content }

            val response: GeminiResponse = GeminiService.generateChatResponse(
                history = history,
                prompt = userPrompt,
                userContext = userContext
            )

            _isAiThinking.value = false

            // 3. Save Assistant Message
            repository.insertMessage(
                ChatMessageEntity(
                    role = MessageRole.ASSISTANT,
                    content = response.replyText,
                    memoryExtracted = response.extractedMemories.firstOrNull()?.content,
                    isVoiceMode = isVoice
                )
            )

            // 4. Save any extracted Long-Term Memories
            for (mem in response.extractedMemories) {
                repository.insertMemory(
                    MemoryEntity(
                        category = mem.category,
                        content = mem.content,
                        confidenceScore = mem.confidence,
                        source = "Chat auto-extraction"
                    )
                )
            }

            // 5. If an Agent Action was proposed, insert into Agent Plans
            response.proposedAction?.let { action ->
                val plan = AgentPlanEntity(
                    triggerContext = userPrompt,
                    reasoning = action.rationale,
                    proposedActionTitle = action.actionTitle,
                    proposedTaskName = action.newTaskTitle ?: "",
                    proposedTaskDurationMinutes = action.newTaskDurationMinutes,
                    rescheduledTaskName = action.rescheduleTaskTitle ?: "",
                    rescheduledToDate = action.rescheduleToNotice ?: "Tomorrow evening",
                    status = PlanStatus.PENDING_APPROVAL
                )
                repository.insertPlan(plan)
            }

            // 6. If voice mode, speak the response
            if (isVoice || _isVoiceOverlayOpen.value) {
                speechSynthesizer.speak(response.replyText)
            }
        }
    }

    // --- Spoken Audio Controls ---
    fun speakText(text: String) {
        speechSynthesizer.speak(text)
    }

    fun stopSpeaking() {
        speechSynthesizer.stop()
    }

    // --- Agent Actions ---
    fun approveAgentPlan(plan: AgentPlanEntity) {
        viewModelScope.launch {
            repository.executePlan(plan)
        }
    }

    fun dismissAgentPlan(plan: AgentPlanEntity) {
        viewModelScope.launch {
            repository.dismissPlan(plan.id)
        }
    }

    fun triggerAutonomousAudit() {
        viewModelScope.launch {
            _isAiThinking.value = true
            val activeTasksStr = activeTasks.value.joinToString { "${it.title} (Priority: ${it.priority})" }
            val scheduleStr = allEvents.value.joinToString { "${it.title} at ${it.startTime}" }
            val memoryStr = allMemories.value.take(5).joinToString { it.content }

            val proposal = GeminiService.reasonAgentPlan(
                situationPrompt = "Audit current schedule density, upcoming exams/deadlines, and pending tasks.",
                activeTasks = activeTasksStr,
                calendarSchedule = scheduleStr,
                memories = memoryStr
            )

            val plan = AgentPlanEntity(
                triggerContext = "Autonomous OS Schedule & Priority Audit",
                reasoning = proposal.rationale,
                proposedActionTitle = proposal.actionTitle,
                proposedTaskName = proposal.newTaskTitle ?: "Autonomous Focus Sprint",
                proposedTaskDurationMinutes = proposal.newTaskDurationMinutes,
                rescheduledTaskName = proposal.rescheduleTaskTitle ?: "",
                rescheduledToDate = proposal.rescheduleToNotice ?: "Tomorrow 6:00 PM",
                status = PlanStatus.PENDING_APPROVAL
            )
            repository.insertPlan(plan)
            _isAiThinking.value = false

            repository.insertMessage(
                ChatMessageEntity(
                    role = MessageRole.ASSISTANT,
                    content = "🤖 I completed a full system audit of your schedule and pending workload. I've staged an optimization plan in your Agent Center for your review."
                )
            )
        }
    }

    // --- Task Operations ---
    fun addTask(
        title: String,
        description: String,
        priority: TaskPriority,
        category: String,
        estimatedMinutes: Int,
        dueDateMillis: Long?
    ) {
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(
                    title = title,
                    description = description,
                    priority = priority,
                    category = category,
                    estimatedMinutes = estimatedMinutes,
                    dueDate = dueDateMillis
                )
            )
        }
    }

    fun toggleTaskComplete(task: TaskEntity) {
        viewModelScope.launch {
            repository.toggleTaskComplete(task)
        }
    }

    fun deleteTask(id: Long) {
        viewModelScope.launch {
            repository.deleteTask(id)
        }
    }

    // --- Notes Operations ---
    fun addNote(title: String, content: String, autoSummarize: Boolean = true) {
        viewModelScope.launch {
            if (autoSummarize && content.isNotBlank()) {
                _isAiThinking.value = true
                val summaryResult = GeminiService.summarizeNote(title, content)
                _isAiThinking.value = false

                repository.insertNote(
                    NoteEntity(
                        title = title,
                        content = content,
                        summary = summaryResult.summary,
                        tags = summaryResult.tags.joinToString(","),
                        aiKeyTakeaways = summaryResult.keyTakeaways.joinToString("\n") { "• $it" }
                    )
                )
            } else {
                repository.insertNote(
                    NoteEntity(
                        title = title,
                        content = content
                    )
                )
            }
        }
    }

    fun summarizeNote(note: NoteEntity) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val summaryResult = GeminiService.summarizeNote(note.title, note.content)
            _isAiThinking.value = false

            repository.updateNote(
                note.copy(
                    summary = summaryResult.summary,
                    tags = summaryResult.tags.joinToString(","),
                    aiKeyTakeaways = summaryResult.keyTakeaways.joinToString("\n") { "• $it" },
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            repository.deleteNote(id)
        }
    }

    // --- Memory Operations ---
    fun addMemory(category: MemoryCategory, content: String) {
        viewModelScope.launch {
            repository.insertMemory(
                MemoryEntity(
                    category = category,
                    content = content,
                    source = "User manual memory entry"
                )
            )
        }
    }

    fun togglePinMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.togglePinMemory(memory)
        }
    }

    fun deleteMemory(id: Long) {
        viewModelScope.launch {
            repository.deleteMemory(id)
        }
    }

    // --- Document Brain Q&A ---
    fun addDoc(title: String, content: String, fileType: String = "DOC") {
        viewModelScope.launch {
            val words = content.split(" ").filter { it.isNotBlank() }
            val chunkCount = kotlin.math.max(1, words.size / 60)
            repository.insertDoc(
                DocEntity(
                    title = title,
                    fileType = fileType,
                    content = content,
                    summary = if (words.size > 20) words.take(20).joinToString(" ") + "..." else content,
                    chunkCount = chunkCount
                )
            )
        }
    }

    fun askDocumentBrain(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isDocAnalyzing.value = true
            val docs = allDocs.value
            val combinedDocContext = docs.joinToString("\n\n") { "Title: ${it.title}\n${it.content}" }

            val answer = GeminiService.answerDocumentQuery(query, combinedDocContext)
            _docAnswer.value = answer
            _isDocAnalyzing.value = false
        }
    }

    fun clearDocAnswer() {
        _docAnswer.value = null
    }

    fun deleteDoc(id: Long) {
        viewModelScope.launch {
            repository.deleteDoc(id)
        }
    }

    // --- Calendar Event Operations ---
    fun addCalendarEvent(
        title: String,
        description: String,
        startTime: Long,
        endTime: Long,
        location: String,
        colorHex: String = "#00E5FF"
    ) {
        viewModelScope.launch {
            repository.insertEvent(
                CalendarEventEntity(
                    title = title,
                    description = description,
                    startTime = startTime,
                    endTime = endTime,
                    location = location,
                    colorHex = colorHex
                )
            )
        }
    }

    fun deleteEvent(id: Long) {
        viewModelScope.launch {
            repository.deleteEvent(id)
        }
    }

    // --- Security & Private Vault ---
    fun unlockVault(pin: String) {
        if (pin == "1234" || pin == "0000" || pin.length == 4) {
            _isVaultUnlocked.value = true
            _vaultErrorMessage.value = null
        } else {
            _vaultErrorMessage.value = "Incorrect PIN. Try 1234."
        }
    }

    fun lockVault() {
        _isVaultUnlocked.value = false
    }

    override fun onCleared() {
        super.onCleared()
        speechSynthesizer.shutdown()
    }
}
