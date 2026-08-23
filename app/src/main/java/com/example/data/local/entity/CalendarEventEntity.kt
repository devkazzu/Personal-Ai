package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_events")
data class CalendarEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val startTime: Long, // Epoch timestamp millis
    val endTime: Long,   // Epoch timestamp millis
    val location: String = "",
    val isAllDay: Boolean = false,
    val colorHex: String = "#00E5FF",
    val relatedTaskId: Long? = null
)
