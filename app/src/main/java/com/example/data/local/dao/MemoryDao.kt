package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.MemoryCategory
import com.example.data.local.entity.MemoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY isPinned DESC, createdAt DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE category = :category ORDER BY createdAt DESC")
    fun getMemoriesByCategory(category: MemoryCategory): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM memories WHERE isPinned = 1")
    suspend fun getPinnedMemories(): List<MemoryEntity>

    @Query("SELECT * FROM memories ORDER BY lastAccessedAt DESC LIMIT 20")
    suspend fun getRecentMemoriesSync(): List<MemoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    suspend fun updateMemory(memory: MemoryEntity)

    @Delete
    suspend fun deleteMemory(memory: MemoryEntity)

    @Query("DELETE FROM memories WHERE id = :id")
    suspend fun deleteMemoryById(id: Long)

    @Query("SELECT * FROM memories WHERE content LIKE '%' || :query || '%'")
    suspend fun searchMemories(query: String): List<MemoryEntity>
}
