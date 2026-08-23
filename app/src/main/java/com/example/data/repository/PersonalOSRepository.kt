package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

data class UnifiedSearchResult(
    val query: String,
    val tasks: List<TaskEntity>,
    val notes: List<NoteEntity>,
    val memories: List<MemoryEntity>,
    val events: List<CalendarEventEntity>,
    val docs: List<DocEntity>,
    val messages: List<ChatMessageEntity>
) {
    val totalCount: Int
        get() = tasks.size + notes.size + memories.size + events.size + docs.size + messages.size
}

class PersonalOSRepository(private val db: AppDatabase) {
    // DAOs
    val taskDao = db.taskDao()
    val noteDao = db.noteDao()
    val memoryDao = db.memoryDao()
    val calendarDao = db.calendarDao()
    val docDao = db.docDao()
    val chatDao = db.chatDao()
    val agentPlanDao = db.agentPlanDao()

    // Flows
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasks()
    val allNotes: Flow<List<NoteEntity>> = noteDao.getAllNotes()
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()
    val allEvents: Flow<List<CalendarEventEntity>> = calendarDao.getAllEvents()
    val allDocs: Flow<List<DocEntity>> = docDao.getAllDocs()
    val allMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessages()
    val pendingPlans: Flow<List<AgentPlanEntity>> = agentPlanDao.getPendingPlans()
    val allPlans: Flow<List<AgentPlanEntity>> = agentPlanDao.getAllPlans()

    // --- Task Operations ---
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun toggleTaskComplete(task: TaskEntity) {
        val newStatus = if (task.status == TaskStatus.COMPLETED) TaskStatus.TODO else TaskStatus.COMPLETED
        taskDao.updateTaskStatus(task.id, newStatus)
    }
    suspend fun deleteTask(id: Long) = taskDao.deleteTaskById(id)
    suspend fun rescheduleTask(id: Long, newDueDate: Long, reasoning: String) =
        taskDao.rescheduleTask(id, newDueDate, reasoning)

    // --- Note Operations ---
    suspend fun insertNote(note: NoteEntity): Long = noteDao.insertNote(note)
    suspend fun updateNote(note: NoteEntity) = noteDao.updateNote(note)
    suspend fun deleteNote(id: Long) = noteDao.deleteNoteById(id)

    // --- Memory Operations ---
    suspend fun insertMemory(memory: MemoryEntity): Long = memoryDao.insertMemory(memory)
    suspend fun updateMemory(memory: MemoryEntity) = memoryDao.updateMemory(memory)
    suspend fun deleteMemory(id: Long) = memoryDao.deleteMemoryById(id)
    suspend fun togglePinMemory(memory: MemoryEntity) =
        memoryDao.updateMemory(memory.copy(isPinned = !memory.isPinned))

    // --- Calendar Operations ---
    suspend fun insertEvent(event: CalendarEventEntity): Long = calendarDao.insertEvent(event)
    suspend fun updateEvent(event: CalendarEventEntity) = calendarDao.updateEvent(event)
    suspend fun deleteEvent(id: Long) = calendarDao.deleteEventById(id)

    // --- Doc Operations ---
    suspend fun insertDoc(doc: DocEntity): Long = docDao.insertDoc(doc)
    suspend fun updateDoc(doc: DocEntity) = docDao.updateDoc(doc)
    suspend fun deleteDoc(id: Long) = docDao.deleteDocById(id)

    // --- Chat Operations ---
    suspend fun insertMessage(message: ChatMessageEntity): Long = chatDao.insertMessage(message)
    suspend fun clearChatHistory() = chatDao.clearHistory()

    // --- Agent Plan Execution ---
    suspend fun insertPlan(plan: AgentPlanEntity): Long = agentPlanDao.insertPlan(plan)
    suspend fun executePlan(plan: AgentPlanEntity) = withContext(Dispatchers.IO) {
        // 1. Mark plan as executed
        agentPlanDao.updatePlanStatus(plan.id, PlanStatus.EXECUTED)

        // 2. If proposedTaskName exists, create the task
        if (plan.proposedTaskName.isNotBlank()) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.HOUR_OF_DAY, 2)
            taskDao.insertTask(
                TaskEntity(
                    title = plan.proposedTaskName,
                    description = "Autonomously staged by AI Personal OS Agent: ${plan.reasoning}",
                    priority = TaskPriority.HIGH,
                    status = TaskStatus.TODO,
                    dueDate = cal.timeInMillis,
                    estimatedMinutes = plan.proposedTaskDurationMinutes,
                    aiReasoning = plan.reasoning,
                    category = "Agent Scheduled"
                )
            )
        }

        // 3. If rescheduledTaskName matches an active task, reschedule it
        if (plan.rescheduledTaskName.isNotBlank()) {
            val matchingTasks = taskDao.searchTasks(plan.rescheduledTaskName)
            val target = matchingTasks.firstOrNull { it.status != TaskStatus.COMPLETED }
            if (target != null) {
                val cal = Calendar.getInstance()
                cal.add(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 18)
                cal.set(Calendar.MINUTE, 0)
                taskDao.rescheduleTask(
                    id = target.id,
                    newDueDate = cal.timeInMillis,
                    reasoning = "Rescheduled to accommodate higher-priority focus session."
                )
            }
        }

        // 4. Log in Chat history
        chatDao.insertMessage(
            ChatMessageEntity(
                role = MessageRole.SYSTEM,
                content = "⚡ Agent Action Executed: ${plan.proposedActionTitle}. Updated tasks and calendar schedule."
            )
        )
    }

    suspend fun dismissPlan(planId: Long) = agentPlanDao.updatePlanStatus(planId, PlanStatus.DISMISSED)

    // --- Semantic Unified Search ---
    suspend fun searchAll(query: String): UnifiedSearchResult = withContext(Dispatchers.IO) {
        if (query.isBlank()) {
            return@withContext UnifiedSearchResult(query, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), emptyList())
        }
        val clean = query.trim()
        val tasks = taskDao.searchTasks(clean)
        val notes = noteDao.searchNotes(clean)
        val memories = memoryDao.searchMemories(clean)
        val events = calendarDao.searchEvents(clean)
        val docs = docDao.searchDocs(clean)
        val messages = chatDao.searchMessages(clean)

        UnifiedSearchResult(
            query = clean,
            tasks = tasks,
            notes = notes,
            memories = memories,
            events = events,
            docs = docs,
            messages = messages
        )
    }

    // --- Build User Context String for AI Grounding ---
    suspend fun getUserContextSummary(): String = withContext(Dispatchers.IO) {
        val memories = memoryDao.getRecentMemoriesSync()
        val pinnedMemories = memoryDao.getPinnedMemories()
        val cal = Calendar.getInstance()
        val upcomingEvents = calendarDao.getUpcomingEventsSync(cal.timeInMillis - 3600000)

        val sb = StringBuilder()
        sb.append("User Memories & Preferences:\n")
        val combinedMem = (pinnedMemories + memories).distinctBy { it.id }.take(10)
        for (m in combinedMem) {
            sb.append("- [${m.category}] ${m.content}\n")
        }

        sb.append("\nUpcoming Schedule:\n")
        for (e in upcomingEvents.take(5)) {
            val timeStr = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.getDefault()).format(java.util.Date(e.startTime))
            sb.append("- $timeStr: ${e.title} (${e.location})\n")
        }

        sb.toString()
    }

    // --- Initialize Sample Seed Data on First Launch ---
    suspend fun seedDatabaseIfEmpty() = withContext(Dispatchers.IO) {
        val existingMemories = memoryDao.getRecentMemoriesSync()
        if (existingMemories.isNotEmpty()) return@withContext

        // 1. Long-term Memories
        memoryDao.insertMemory(
            MemoryEntity(
                category = MemoryCategory.PREFERENCE,
                content = "Prefers deep focus blocks in early mornings before 11:00 AM with black coffee.",
                confidenceScore = 0.98f,
                source = "Extracted from chat",
                isPinned = true
            )
        )
        memoryDao.insertMemory(
            MemoryEntity(
                category = MemoryCategory.WORK_STUDY,
                content = "Has Advanced Computer Systems & Neural Architectures Exam tomorrow at 10:00 AM.",
                confidenceScore = 0.99f,
                source = "Calendar sync & chat",
                isPinned = true
            )
        )
        memoryDao.insertMemory(
            MemoryEntity(
                category = MemoryCategory.GOAL,
                content = "Publish AI OS open-source mobile client and reach 10,000 active neural memory nodes.",
                confidenceScore = 0.95f,
                source = "User manual entry",
                isPinned = false
            )
        )
        memoryDao.insertMemory(
            MemoryEntity(
                category = MemoryCategory.HABIT,
                content = "Takes 15-minute screen-free walks every afternoon at 3:30 PM.",
                confidenceScore = 0.91f,
                source = "Agent behavioral analysis",
                isPinned = false
            )
        )

        // 2. Calendar Events
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()

        // Today 4:00 PM Meeting
        cal.set(Calendar.HOUR_OF_DAY, 16)
        cal.set(Calendar.MINUTE, 0)
        val meetStart = cal.timeInMillis
        cal.set(Calendar.HOUR_OF_DAY, 17)
        val meetEnd = cal.timeInMillis
        calendarDao.insertEvent(
            CalendarEventEntity(
                title = "AI Core Architecture Sync",
                description = "Weekly review of local RAG vector indexing and offline agent tools.",
                startTime = meetStart,
                endTime = meetEnd,
                location = "Room 4B / Google Meet",
                colorHex = "#00E5FF"
            )
        )

        // Tomorrow 10:00 AM Exam
        val calTomorrow = Calendar.getInstance()
        calTomorrow.add(Calendar.DAY_OF_YEAR, 1)
        calTomorrow.set(Calendar.HOUR_OF_DAY, 10)
        calTomorrow.set(Calendar.MINUTE, 0)
        val examStart = calTomorrow.timeInMillis
        calTomorrow.set(Calendar.HOUR_OF_DAY, 12)
        val examEnd = calTomorrow.timeInMillis
        calendarDao.insertEvent(
            CalendarEventEntity(
                title = "Final Exam: Neural Architectures",
                description = "Comprehensive examination covering Transformer Attention, RAG, and Agent Systems.",
                startTime = examStart,
                endTime = examEnd,
                location = "Engineering Hall 101",
                colorHex = "#EF4444"
            )
        )

        // 3. Tasks
        val calTask = Calendar.getInstance()
        calTask.set(Calendar.HOUR_OF_DAY, 20)
        taskDao.insertTask(
            TaskEntity(
                title = "Review Neural Architectures Chapter 4 & 5",
                description = "Critical review before tomorrow's 10 AM exam.",
                priority = TaskPriority.CRITICAL,
                status = TaskStatus.TODO,
                dueDate = calTask.timeInMillis,
                estimatedMinutes = 90,
                category = "Study",
                aiReasoning = "Calculated high urgency due to exam scheduled tomorrow at 10 AM."
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Refactor Local Room Migration Script",
                description = "Update SQL schemas and verify KSP compiler output.",
                priority = TaskPriority.MEDIUM,
                status = TaskStatus.TODO,
                dueDate = now + 86400000,
                estimatedMinutes = 45,
                category = "Development"
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Sort Monthly Receipts & Subscriptions",
                description = "Non-urgent admin backlog task.",
                priority = TaskPriority.LOW,
                status = TaskStatus.TODO,
                dueDate = now + 172800000,
                estimatedMinutes = 30,
                category = "Admin"
            )
        )

        // 4. Notes
        noteDao.insertNote(
            NoteEntity(
                title = "Neural Memory & Long-Term RAG Specs",
                content = "Key principles of private local memory: 1. Zero-leakage encryption, 2. Dynamic decay and confidence scoring, 3. Contextual graph association between facts, habits, and schedules.",
                summary = "Architectural blueprint for on-device private memory and contextual graph recall.",
                tags = "AI,Memory,Privacy,Architecture",
                aiKeyTakeaways = "• Zero-leakage encryption for private nodes\n• Dynamic confidence scoring\n• Contextual graph recall",
                isPinned = true
            )
        )
        noteDao.insertNote(
            NoteEntity(
                title = "Weekly Focus & Health Manifesto",
                content = "Prioritize 8 hours of sleep before high-cognitive load days. Morning sunlight and structured deep work cycles improve retention by 40%.",
                summary = "Health and high-performance focus protocol.",
                tags = "Health,Habits,Focus",
                aiKeyTakeaways = "• Maintain 8-hour sleep baseline\n• Schedule deep work in peak morning windows",
                isPinned = false
            )
        )

        // 5. Documents (Document Brain)
        docDao.insertDoc(
            DocEntity(
                title = "Distributed Systems & Neural OS Whitepaper.pdf",
                fileType = "PDF",
                content = "Document Brain Knowledge Base: High availability distributed consensus, vector database caching strategies, on-device SLM reasoning, and autonomous multi-agent tool execution protocols.",
                summary = "Core technical whitepaper covering autonomous agent tool execution and vector caching.",
                topics = "Neural OS,Agents,RAG,Distributed Systems",
                chunkCount = 4,
                isSecure = true
            )
        )

        // 6. Agent Plan (Pre-staged God-level Reasoning demo)
        agentPlanDao.insertPlan(
            AgentPlanEntity(
                triggerContext = "Calendar event detected: Final Exam tomorrow at 10:00 AM.",
                reasoning = "Your Neural Architectures exam is tomorrow morning. You have 2 unfinished review topics, so I've staged a 90-minute revision task for tonight and shifted non-urgent tasks to tomorrow evening.",
                proposedActionTitle = "Exam Prep & Schedule Optimization",
                proposedTaskName = "90-Min Final Exam Topic Review",
                proposedTaskDurationMinutes = 90,
                rescheduledTaskName = "Sort Monthly Receipts",
                rescheduledToDate = "Tomorrow 6:00 PM",
                status = PlanStatus.PENDING_APPROVAL
            )
        )

        // 7. Initial Chat Welcome
        chatDao.insertMessage(
            ChatMessageEntity(
                role = MessageRole.ASSISTANT,
                content = "Welcome to your AI Personal Operating System. I am initialized and actively monitoring your schedule, long-term memory, and tasks. Type 'I have an exam tomorrow' or use voice mode to see my proactive agent reasoning in action."
            )
        )
    }
}
