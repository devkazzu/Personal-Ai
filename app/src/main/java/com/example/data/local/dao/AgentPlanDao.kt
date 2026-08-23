package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.AgentPlanEntity
import com.example.data.local.entity.PlanStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentPlanDao {
    @Query("SELECT * FROM agent_plans ORDER BY createdAt DESC")
    fun getAllPlans(): Flow<List<AgentPlanEntity>>

    @Query("SELECT * FROM agent_plans WHERE status = 'PENDING_APPROVAL' ORDER BY createdAt DESC")
    fun getPendingPlans(): Flow<List<AgentPlanEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlan(plan: AgentPlanEntity): Long

    @Update
    suspend fun updatePlan(plan: AgentPlanEntity)

    @Query("UPDATE agent_plans SET status = :status WHERE id = :id")
    suspend fun updatePlanStatus(id: Long, status: PlanStatus)

    @Delete
    suspend fun deletePlan(plan: AgentPlanEntity)
}
