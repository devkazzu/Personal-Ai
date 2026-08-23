package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM,
    AGENT_PROPOSAL
}

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val role: MessageRole,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val memoryExtracted: String? = null,
    val agentActionJson: String? = null, // JSON payload if action proposed/executed
    val isVoiceMode: Boolean = false
)
