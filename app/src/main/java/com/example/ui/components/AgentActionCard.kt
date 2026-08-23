package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.data.local.entity.PlanStatus
import com.example.ui.theme.*

@Composable
fun AgentActionCard(
    plan: AgentPlanEntity,
    onApprove: (AgentPlanEntity) -> Unit,
    onDismiss: (AgentPlanEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val isPending = plan.status == PlanStatus.PENDING_APPROVAL
    val isExecuted = plan.status == PlanStatus.EXECUTED

    val borderBrush = if (isPending) {
        Brush.horizontalGradient(listOf(CyberCyan, NeonPurple))
    } else {
        Brush.horizontalGradient(listOf(SurfaceVariantDark, SurfaceVariantDark))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                border = BorderStroke(1.5.dp, borderBrush),
                shape = RoundedCornerShape(16.dp)
            )
            .testTag("agent_action_card_${plan.id}"),
        colors = CardDefaults.cardColors(
            containerColor = SurfaceDark
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header: Agent Badge & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Agent Intelligence",
                            tint = CyberCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "AGENT REASONING",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = when (plan.status) {
                        PlanStatus.PENDING_APPROVAL -> WarningAmber.copy(alpha = 0.2f)
                        PlanStatus.EXECUTED -> NeuralEmerald.copy(alpha = 0.2f)
                        PlanStatus.APPROVED -> CyberCyan.copy(alpha = 0.2f)
                        PlanStatus.DISMISSED -> TextMutedDark.copy(alpha = 0.2f)
                    }
                ) {
                    Text(
                        text = when (plan.status) {
                            PlanStatus.PENDING_APPROVAL -> "APPROVAL NEEDED"
                            PlanStatus.EXECUTED -> "EXECUTED"
                            PlanStatus.APPROVED -> "APPROVED"
                            PlanStatus.DISMISSED -> "DISMISSED"
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = when (plan.status) {
                            PlanStatus.PENDING_APPROVAL -> WarningAmber
                            PlanStatus.EXECUTED -> NeuralEmerald
                            PlanStatus.APPROVED -> CyberCyan
                            PlanStatus.DISMISSED -> TextMutedDark
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Plan Title
            Text(
                text = plan.proposedActionTitle,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )

            // Agent Reasoning Text
            Text(
                text = plan.reasoning,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                lineHeight = 20.sp
            )

            // Proposed Sub-Actions
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceVariantDark)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (plan.proposedTaskName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircleOutline,
                            contentDescription = "New Task",
                            tint = NeuralEmerald,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Create Task: ${plan.proposedTaskName} (${plan.proposedTaskDurationMinutes}m)",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (plan.rescheduledTaskName.isNotBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Update,
                            contentDescription = "Reschedule Task",
                            tint = WarningAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Reschedule: '${plan.rescheduledTaskName}' → ${plan.rescheduledToDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryDark,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Action Buttons
            if (isPending) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDismiss(plan) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dismiss_plan_btn_${plan.id}"),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, CardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = TextSecondaryDark
                        )
                    ) {
                        Text("Dismiss")
                    }

                    Button(
                        onClick = { onApprove(plan) },
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("approve_plan_btn_${plan.id}"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = DeepObsidian
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Approve & Execute",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
