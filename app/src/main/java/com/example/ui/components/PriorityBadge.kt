package com.example.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.TaskPriority
import com.example.ui.theme.*

@Composable
fun PriorityBadge(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (bg, textColor, label) = when (priority) {
        TaskPriority.CRITICAL -> Triple(CriticalRed.copy(alpha = 0.2f), CriticalRed, "CRITICAL")
        TaskPriority.HIGH -> Triple(WarningAmber.copy(alpha = 0.2f), WarningAmber, "HIGH")
        TaskPriority.MEDIUM -> Triple(CyberCyan.copy(alpha = 0.2f), CyberCyan, "MEDIUM")
        TaskPriority.LOW -> Triple(NeuralEmerald.copy(alpha = 0.2f), NeuralEmerald, "LOW")
    }

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = bg,
        modifier = modifier
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}
