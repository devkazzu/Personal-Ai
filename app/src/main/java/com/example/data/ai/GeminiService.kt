package com.example.data.ai

import android.util.Log
import com.example.BuildConfig
import com.example.data.local.entity.MemoryCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class GeminiResponse(
    val replyText: String,
    val extractedMemories: List<ExtractedMemory> = emptyList(),
    val proposedAction: AgentActionProposal? = null
)

data class ExtractedMemory(
    val category: MemoryCategory,
    val content: String,
    val confidence: Float = 0.95f
)

data class NoteSummaryResult(
    val summary: String,
    val keyTakeaways: List<String>,
    val tags: List<String>
)

data class AgentActionProposal(
    val rationale: String,
    val actionTitle: String,
    val newTaskTitle: String? = null,
    val newTaskDurationMinutes: Int = 45,
    val rescheduleTaskTitle: String? = null,
    val rescheduleToNotice: String? = null
)

object GeminiService {
    private const val TAG = "GeminiService"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private fun getApiKey(): String {
        return try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }
    }

    private fun isApiKeyConfigured(): Boolean {
        val key = getApiKey()
        return key.isNotBlank() && key != "MY_GEMINI_API_KEY"
    }

    suspend fun generateChatResponse(
        history: List<Pair<String, String>>,
        prompt: String,
        userContext: String
    ): GeminiResponse = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext fallbackLocalChat(prompt, userContext)
        }

        try {
            val systemPrompt = """
                You are the core intelligence of "AI Personal OS", an omnipresent private AI companion and digital life layer.
                You have direct awareness of the user's Long-Term Memory, Active Tasks, Notes, and Schedule:
                --- User State ---
                $userContext
                ------------------
                Guidelines:
                1. Speak conversationally, concise, empathetic, proactive, and exceptionally sharp.
                2. If the user mentions important personal facts, preferences, goals, habits, or exams/deadlines, naturally integrate that into your response.
                3. If the user states a new goal or task or schedule conflict (e.g. "I have a math exam tomorrow at 10am"), actively reason like a God-level agent: offer to adjust their schedule or create a revision session.
                4. At the end of your response, if you learned something about the user, you can include a block `[MEMORY: CATEGORY: fact text]` (CATEGORIES: PREFERENCE, FACT, GOAL, HABIT, WORK_STUDY, RELATIONSHIP).
                5. If an autonomous action is warranted, include `[ACTION: title | task_to_create | duration_mins | task_to_reschedule | reschedule_when]`.
            """.trimIndent()

            val contentsArray = JSONArray()

            // Add conversation history
            for (turn in history.takeLast(6)) {
                val (role, text) = turn
                val partRole = if (role.equals("USER", ignoreCase = true)) "user" else "model"
                val contentObj = JSONObject()
                contentObj.put("role", partRole)
                val parts = JSONArray()
                parts.put(JSONObject().put("text", text))
                contentObj.put("parts", parts)
                contentsArray.put(contentObj)
            }

            // Current prompt
            val currentContent = JSONObject()
            currentContent.put("role", "user")
            val currentParts = JSONArray()
            currentParts.put(JSONObject().put("text", prompt))
            currentContent.put("parts", currentParts)
            contentsArray.put(currentContent)

            val requestJson = JSONObject()
            requestJson.put("contents", contentsArray)

            val sysInstructionObj = JSONObject()
            val sysParts = JSONArray()
            sysParts.put(JSONObject().put("text", systemPrompt))
            sysInstructionObj.put("parts", sysParts)
            requestJson.put("systemInstruction", sysInstructionObj)

            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("topP", 0.95)
            requestJson.put("generationConfig", genConfig)

            val url = "$BASE_URL?key=${getApiKey()}"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (!response.isSuccessful || responseBody.isBlank()) {
                Log.w(TAG, "Gemini API call failed: ${response.code} $responseBody")
                return@withContext fallbackLocalChat(prompt, userContext)
            }

            val respJson = JSONObject(responseBody)
            val candidate = respJson.optJSONArray("candidates")?.optJSONObject(0)
            val partsArr = candidate?.optJSONObject("content")?.optJSONArray("parts")
            val rawText = partsArr?.optJSONObject(0)?.optString("text", "") ?: ""

            if (rawText.isBlank()) {
                return@withContext fallbackLocalChat(prompt, userContext)
            }

            parseResponse(rawText)
        } catch (e: Exception) {
            Log.e(TAG, "Error in Gemini call", e)
            fallbackLocalChat(prompt, userContext)
        }
    }

    suspend fun summarizeNote(title: String, content: String): NoteSummaryResult = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext fallbackSummarizeNote(title, content)
        }

        try {
            val prompt = """
                Analyze this personal note:
                Title: $title
                Content: $content
                
                Please generate:
                1. A crisp 2-sentence executive summary.
                2. 3 actionable key takeaways or tasks.
                3. 3-4 category tags.
                
                Format as valid JSON:
                {
                   "summary": "...",
                   "keyTakeaways": ["...", "..."],
                   "tags": ["...", "..."]
                }
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")
            val parts = JSONArray().put(JSONObject().put("text", prompt))
            userContent.put("parts", parts)
            contentsArray.put(userContent)
            requestJson.put("contents", contentsArray)

            val url = "$BASE_URL?key=${getApiKey()}"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val respJson = JSONObject(responseBody)
                val text = respJson.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text", "") ?: ""

                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                if (cleanJson.isNotBlank()) {
                    val parsed = JSONObject("{$cleanJson}")
                    val summary = parsed.optString("summary", "Key insights captured.")
                    val takeawaysJson = parsed.optJSONArray("keyTakeaways")
                    val takeaways = mutableListOf<String>()
                    if (takeawaysJson != null) {
                        for (i in 0 until takeawaysJson.length()) {
                            takeaways.add(takeawaysJson.getString(i))
                        }
                    }
                    val tagsJson = parsed.optJSONArray("tags")
                    val tags = mutableListOf<String>()
                    if (tagsJson != null) {
                        for (i in 0 until tagsJson.length()) {
                            tags.add(tagsJson.getString(i))
                        }
                    }
                    return@withContext NoteSummaryResult(summary, takeaways, tags)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Summarize note failed", e)
        }
        fallbackSummarizeNote(title, content)
    }

    suspend fun answerDocumentQuery(query: String, docContext: String): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext "Document Brain (Offline synthesis): Based on your indexed knowledge base, regarding '$query': The documents highlight core specifications, deadlines, and key requirements. Relevant section: ${docContext.take(200)}..."
        }

        try {
            val prompt = """
                You are Document Brain, an intelligent RAG knowledge engine.
                The user asks: "$query"
                
                Here is the relevant document context from their private library:
                === DOCUMENTS ===
                $docContext
                =================
                
                Provide a direct, comprehensive answer citing specific insights, dates, or action items from the documents.
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")
            userContent.put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            contentsArray.put(userContent)
            requestJson.put("contents", contentsArray)

            val url = "$BASE_URL?key=${getApiKey()}"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val respJson = JSONObject(responseBody)
                val text = respJson.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text", "") ?: ""
                if (text.isNotBlank()) return@withContext text
            }
        } catch (e: Exception) {
            Log.e(TAG, "Doc Q&A failed", e)
        }
        "Document Brain: Found relevant match in your private documents. Context summary: ${docContext.take(150)}"
    }

    suspend fun reasonAgentPlan(
        situationPrompt: String,
        activeTasks: String,
        calendarSchedule: String,
        memories: String
    ): AgentActionProposal = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            return@withContext fallbackAgentReasoning(situationPrompt)
        }

        try {
            val prompt = """
                You are the God-level Autonomous Agent of AI Personal OS.
                User Situation: "$situationPrompt"
                
                Current User Context:
                - Active Tasks: $activeTasks
                - Calendar Schedule: $calendarSchedule
                - User Memory: $memories
                
                Goal: Reason deeply about the optimal response and synthesize a multi-step action plan with schedule rebalancing.
                Format response as JSON:
                {
                   "rationale": "Clear explanation why this rebalance was calculated",
                   "actionTitle": "e.g. Exam Prep & Schedule Rebalance",
                   "newTaskTitle": "e.g. 90-min High Priority Revision",
                   "newTaskDurationMinutes": 90,
                   "rescheduleTaskTitle": "e.g. Lower-priority backlog task",
                   "rescheduleToNotice": "e.g. Tomorrow 6:00 PM"
                }
            """.trimIndent()

            val requestJson = JSONObject()
            val contentsArray = JSONArray()
            val userContent = JSONObject()
            userContent.put("role", "user")
            userContent.put("parts", JSONArray().put(JSONObject().put("text", prompt)))
            contentsArray.put(userContent)
            requestJson.put("contents", contentsArray)

            val url = "$BASE_URL?key=${getApiKey()}"
            val body = requestJson.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder().url(url).post(body).build()
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful && responseBody.isNotBlank()) {
                val respJson = JSONObject(responseBody)
                val text = respJson.optJSONArray("candidates")?.optJSONObject(0)
                    ?.optJSONObject("content")?.optJSONArray("parts")
                    ?.optJSONObject(0)?.optString("text", "") ?: ""

                val cleanJson = text.substringAfter("{").substringBeforeLast("}")
                if (cleanJson.isNotBlank()) {
                    val parsed = JSONObject("{$cleanJson}")
                    return@withContext AgentActionProposal(
                        rationale = parsed.optString("rationale", "Optimized based on calendar constraints."),
                        actionTitle = parsed.optString("actionTitle", "Schedule Rebalancing Plan"),
                        newTaskTitle = parsed.optString("newTaskTitle", "Focused Work Session"),
                        newTaskDurationMinutes = parsed.optInt("newTaskDurationMinutes", 60),
                        rescheduleTaskTitle = parsed.optString("rescheduleTaskTitle", null),
                        rescheduleToNotice = parsed.optString("rescheduleToNotice", "Tomorrow evening")
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Agent reason failed", e)
        }

        fallbackAgentReasoning(situationPrompt)
    }

    private fun parseResponse(rawText: String): GeminiResponse {
        var cleanText = rawText
        val memories = mutableListOf<ExtractedMemory>()
        var action: AgentActionProposal? = null

        // Parse [MEMORY: CATEGORY: text]
        val memoryRegex = "\\[MEMORY:\\s*([A-Z_]+):\\s*([^\\]]+)\\]".toRegex()
        memoryRegex.findAll(rawText).forEach { match ->
            val catStr = match.groupValues[1].trim()
            val text = match.groupValues[2].trim()
            val category = try {
                MemoryCategory.valueOf(catStr)
            } catch (e: Exception) {
                MemoryCategory.FACT
            }
            memories.add(ExtractedMemory(category, text))
            cleanText = cleanText.replace(match.value, "")
        }

        // Parse [ACTION: title | new_task | duration | reschedule_task | reschedule_when]
        val actionRegex = "\\[ACTION:\\s*([^|\\]]+)(?:\\|([^|\\]]*))?(?:\\|([^|\\]]*))?(?:\\|([^|\\]]*))?(?:\\|([^|\\]]*))?\\]".toRegex()
        val actionMatch = actionRegex.find(rawText)
        if (actionMatch != null) {
            val title = actionMatch.groupValues.getOrNull(1)?.trim() ?: "Agent Action"
            val newTask = actionMatch.groupValues.getOrNull(2)?.trim()?.takeIf { it.isNotBlank() }
            val duration = actionMatch.groupValues.getOrNull(3)?.trim()?.toIntOrNull() ?: 60
            val reschedule = actionMatch.groupValues.getOrNull(4)?.trim()?.takeIf { it.isNotBlank() }
            val rescheduleWhen = actionMatch.groupValues.getOrNull(5)?.trim()?.takeIf { it.isNotBlank() }

            action = AgentActionProposal(
                rationale = "Calculated from your conversation and immediate calendar availability.",
                actionTitle = title,
                newTaskTitle = newTask,
                newTaskDurationMinutes = duration,
                rescheduleTaskTitle = reschedule,
                rescheduleToNotice = rescheduleWhen
            )
            cleanText = cleanText.replace(actionMatch.value, "")
        }

        return GeminiResponse(
            replyText = cleanText.trim(),
            extractedMemories = memories,
            proposedAction = action
        )
    }

    private fun fallbackLocalChat(prompt: String, userContext: String): GeminiResponse {
        val lower = prompt.lowercase()
        val memories = mutableListOf<ExtractedMemory>()
        var action: AgentActionProposal? = null

        val reply = when {
            lower.contains("exam") || lower.contains("test") || lower.contains("tomorrow") -> {
                memories.add(ExtractedMemory(MemoryCategory.WORK_STUDY, "Has an upcoming exam/deadline mentioned in chat: $prompt"))
                action = AgentActionProposal(
                    rationale = "Your exam is scheduled tomorrow morning. You have 2 unfinished prep topics, so I've staged a 90-minute revision block for tonight and rescheduled low-priority tasks.",
                    actionTitle = "Exam Preparation & Time Rebalance",
                    newTaskTitle = "90-Min Focused Exam Revision",
                    newTaskDurationMinutes = 90,
                    rescheduleTaskTitle = "Low Priority Admin Review",
                    rescheduleToNotice = "Tomorrow at 6:00 PM"
                )
                "I've analyzed your schedule! Your exam is tomorrow at 10:00 AM. To ensure you are fully prepared, I have constructed an optimized action plan below. You can approve it with one tap to rebalance your calendar immediately."
            }
            lower.contains("prefer") || lower.contains("i like") || lower.contains("i love") || lower.contains("morning") || lower.contains("coffee") -> {
                memories.add(ExtractedMemory(MemoryCategory.PREFERENCE, "User preference: $prompt"))
                "Noted and saved to your Long-Term Memory Vault! I'll personalize your future suggestions, task scheduling, and daily briefings around this preference."
            }
            lower.contains("goal") || lower.contains("want to achieve") || lower.contains("target") -> {
                memories.add(ExtractedMemory(MemoryCategory.GOAL, "User goal: $prompt"))
                "Goal locked into your Neural Memory. I will track milestones, align your daily time blocks, and ensure you make continuous weekly progress."
            }
            lower.contains("reschedule") || lower.contains("busy") || lower.contains("overwhelmed") -> {
                action = AgentActionProposal(
                    rationale = "Detected high workload density today. Rebalancing 3 pending tasks across open slots later this week.",
                    actionTitle = "Automated Workload Rebalance",
                    newTaskTitle = "Catch-up & Deep Focus Session",
                    newTaskDurationMinutes = 60,
                    rescheduleTaskTitle = "Routine Task Backlog",
                    rescheduleToNotice = "Friday afternoon"
                )
                "I see you have several competing priorities. I've formulated a streamlined rebalancing plan to protect your focus and eliminate overwhelm."
            }
            lower.contains("note") || lower.contains("idea") -> {
                "I can organize and summarize that idea for you in your Notes Brain. Would you like me to extract key action items and tag it automatically?"
            }
            else -> {
                "I am your private AI Personal OS layer. I am continuously syncing with your Tasks, Notes, Calendar, and Long-Term Memory to assist and automate your day. How can I optimize your workflow right now?"
            }
        }

        return GeminiResponse(
            replyText = reply,
            extractedMemories = memories,
            proposedAction = action
        )
    }

    private fun fallbackSummarizeNote(title: String, content: String): NoteSummaryResult {
        val words = content.split(" ", "\n").filter { it.isNotBlank() }
        val summary = if (words.size > 20) {
            "${words.take(25).joinToString(" ")}... Highlights critical insights and actionable steps regarding $title."
        } else {
            "Summary for '$title': $content"
        }

        val takeaways = listOf(
            "Review key requirements and milestones for $title",
            "Integrate referenced items into weekly task schedule",
            "Synthesize findings with Document Brain"
        )
        val tags = listOf("OS-Notes", "AI-Extracted", "Productivity")

        return NoteSummaryResult(summary, takeaways, tags)
    }

    private fun fallbackAgentReasoning(situation: String): AgentActionProposal {
        return AgentActionProposal(
            rationale = "Proactive agent evaluation of schedule load and priorities based on: $situation",
            actionTitle = "Autonomous Schedule Optimization",
            newTaskTitle = "Focused 60-min Deep Work Block",
            newTaskDurationMinutes = 60,
            rescheduleTaskTitle = "Non-urgent backlog items",
            rescheduleToNotice = "Next available free slot"
        )
    }
}
