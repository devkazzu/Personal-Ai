package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entity.DocEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocDao {
    @Query("SELECT * FROM documents ORDER BY createdAt DESC")
    fun getAllDocs(): Flow<List<DocEntity>>

    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocById(id: Long): DocEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDoc(doc: DocEntity): Long

    @Update
    suspend fun updateDoc(doc: DocEntity)

    @Delete
    suspend fun deleteDoc(doc: DocEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocById(id: Long)

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%' OR summary LIKE '%' || :query || '%' OR topics LIKE '%' || :query || '%'")
    suspend fun searchDocs(query: String): List<DocEntity>
}
