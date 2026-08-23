package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority {
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    RESCHEDULED
}

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val status: TaskStatus = TaskStatus.TODO,
    val dueDate: Long? = null,
    val estimatedMinutes: Int = 30,
    val aiReasoning: String? = null,
    val category: String = "General",
    val createdAt: Long = System.currentTimeMillis()
)
