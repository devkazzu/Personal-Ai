package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MemoryCategory {
    PREFERENCE,
    FACT,
    GOAL,
    HABIT,
    WORK_STUDY,
    RELATIONSHIP
}

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: MemoryCategory = MemoryCategory.FACT,
    val content: String,
    val confidenceScore: Float = 0.95f,
    val source: String = "AI Conversation", // "Chat extraction", "User manual", "Agent inferred"
    val isPinned: Boolean = false,
    val isEncrypted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastAccessedAt: Long = System.currentTimeMillis()
)
