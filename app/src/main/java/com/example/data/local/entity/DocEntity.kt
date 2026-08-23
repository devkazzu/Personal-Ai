package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val fileType: String = "DOC", // "PDF", "MARKDOWN", "TXT", "CODE"
    val content: String,
    val summary: String = "",
    val topics: String = "", // Comma-separated topics
    val chunkCount: Int = 1,
    val isSecure: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
