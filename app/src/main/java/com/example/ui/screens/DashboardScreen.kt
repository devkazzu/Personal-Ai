package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.AgentPlanEntity
import com.example.data.local.entity.CalendarEventEntity
import com.example.data.local.entity.TaskEntity
import com.example.ui.components.AgentActionCard
import com.example.ui.components.PriorityBadge
import com.example.ui.components.SemanticSearchBar
import com.example.ui.theme.*
import com.example.viewmodel.OSTab
import com.example.viewmodel.PersonalOSViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: PersonalOSViewModel,
    modifier: Modifier = Modifier
) {
    val activeTasks by viewModel.activeTasks.collectAsState()
    val allMemories by viewModel.allMemories.collectAsState()
    val allDocs by viewModel.allDocs.collectAsState()
    val allEvents by viewModel.allEvents.collectAsState()
    val pendingPlans by viewModel.pendingPlans.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResult by viewModel.searchResult.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Bar & Status
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AI PERSONAL OS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Neural Operating Layer",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    // Online AI Chip
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = NeuralEmerald.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, NeuralEmerald.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isAiThinking) WarningAmber else NeuralEmerald)
                            )
                            Text(
                                text = if (isAiThinking) "REASONING..." else "AI ONLINE",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isAiThinking) WarningAmber else NeuralEmerald,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Semantic Search Bar
        item {
            SemanticSearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                searchResult = searchResult,
                onResultSelected = { /* handled in search results */ }
            )
        }

        // Quick OS Action Cards
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickLauncherCard(
                    title = "Voice Mode",
                    subtitle = "Talk to AI",
                    icon = Icons.Default.Mic,
                    color = CyberCyan,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.openVoiceOverlay() },
                    testTag = "quick_voice_btn"
                )
                QuickLauncherCard(
                    title = "Agent Audit",
                    subtitle = "Optimize Day",
                    icon = Icons.Default.AutoAwesome,
                    color = NeonPurple,
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.triggerAutonomousAudit() },
                    testTag = "quick_audit_btn"
                )
            }
        }

        // Neural Stats Row
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatNode(count = activeTasks.size, label = "Active Tasks", color = CyberCyan)
                    StatNode(count = allMemories.size, label = "Memories", color = NeuralEmerald)
                    StatNode(count = allDocs.size, label = "Brain Docs", color = NeonPurple)
                    StatNode(count = allEvents.size, label = "Events", color = WarningAmber)
                }
            }
        }

        // Pending Agent Actions (God-level reasoning card)
        if (pendingPlans.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ PROACTIVE AGENT REASONING",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = WarningAmber,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "${pendingPlans.size} Action Pending",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondaryDark
                        )
                    }

                    for (plan in pendingPlans) {
                        AgentActionCard(
                            plan = plan,
                            onApprove = viewModel::approveAgentPlan,
                            onDismiss = viewModel::dismissAgentPlan
                        )
                    }
                }
            }
        }

        // Today's Agenda & Critical Timeline
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TODAY'S SCHEDULE & AGENDA",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = { viewModel.selectTab(OSTab.TASKS_CALENDAR) },
                        modifier = Modifier.testTag("view_calendar_btn")
                    ) {
                        Text("View Calendar", color = CyberCyan, style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (allEvents.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark
                    ) {
                        Text(
                            "No upcoming events on today's calendar.",
                            modifier = Modifier.padding(16.dp),
                            color = TextMutedDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allEvents.take(3).forEach { event ->
                            EventAgendaRow(event = event)
                        }
                    }
                }
            }
        }

        // Critical & High Priority Tasks
        item {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "PRIORITY TASKS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = NeuralEmerald,
                        letterSpacing = 1.sp
                    )
                    TextButton(
                        onClick = { viewModel.selectTab(OSTab.TASKS_CALENDAR) },
                        modifier = Modifier.testTag("view_all_tasks_btn")
                    ) {
                        Text("All Tasks (${activeTasks.size})", color = NeuralEmerald, style = MaterialTheme.typography.labelSmall)
                    }
                }

                if (activeTasks.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = SurfaceDark
                    ) {
                        Text(
                            "All tasks complete! Your schedule is clear.",
                            modifier = Modifier.padding(16.dp),
                            color = TextMutedDark,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeTasks.take(4).forEach { task ->
                            TaskRow(
                                task = task,
                                onToggle = { viewModel.toggleTaskComplete(task) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickLauncherCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .testTag(testTag),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondaryDark,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun StatNode(count: Int, label: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondaryDark,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun EventAgendaRow(event: CalendarEventEntity) {
    val timeFormatter = SimpleDateFormat("h:mm a", Locale.getDefault())
    val startStr = timeFormatter.format(Date(event.startTime))
    val endStr = timeFormatter.format(Date(event.endTime))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CyberCyan.copy(alpha = 0.15f)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = startStr,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontSize = 11.sp
                    )
                    Text(
                        text = endStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMutedDark,
                        fontSize = 9.sp
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark
                )
                if (event.location.isNotBlank()) {
                    Text(
                        text = "📍 ${event.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: TaskEntity,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            IconButton(
                onClick = onToggle,
                modifier = Modifier.size(28.dp).testTag("task_toggle_${task.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.RadioButtonUnchecked,
                    contentDescription = "Toggle Complete",
                    tint = TextSecondaryDark
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimaryDark
                )
                if (task.aiReasoning != null) {
                    Text(
                        text = "⚡ ${task.aiReasoning}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberCyan,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            PriorityBadge(priority = task.priority)
        }
    }
}
