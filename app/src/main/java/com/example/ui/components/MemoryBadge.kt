package com.example.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.MemoryCategory
import com.example.ui.theme.*

@Composable
fun MemoryBadge(category: MemoryCategory, modifier: Modifier = Modifier) {
    val (color, icon, label) = when (category) {
        MemoryCategory.PREFERENCE -> Triple(CyberCyan, Icons.Default.Favorite, "PREFERENCE")
        MemoryCategory.WORK_STUDY -> Triple(NeuralViolet, Icons.Default.School, "WORK & STUDY")
        MemoryCategory.GOAL -> Triple(WarningAmber, Icons.Default.Flag, "GOAL")
        MemoryCategory.HABIT -> Triple(NeuralEmerald, Icons.Default.Loop, "HABIT")
        MemoryCategory.FACT -> Triple(ElectricBlue, Icons.Default.Lightbulb, "FACT")
        MemoryCategory.RELATIONSHIP -> Triple(NeonPurple, Icons.Default.People, "RELATIONSHIP")
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = " $label",
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
