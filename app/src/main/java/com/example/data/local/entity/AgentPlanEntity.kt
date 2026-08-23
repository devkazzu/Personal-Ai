package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class PlanStatus {
    PENDING_APPROVAL,
    APPROVED,
    EXECUTED,
    DISMISSED
}

@Entity(tableName = "agent_plans")
data class AgentPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val triggerContext: String,
    val reasoning: String,
    val proposedActionTitle: String,
    val proposedTaskName: String = "",
    val proposedTaskDurationMinutes: Int = 60,
    val rescheduledTaskName: String = "",
    val rescheduledToDate: String = "",
    val status: PlanStatus = PlanStatus.PENDING_APPROVAL,
    val createdAt: Long = System.currentTimeMillis()
)
