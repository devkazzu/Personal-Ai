package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.data.local.entity.PlanStatus
import com.example.ui.components.AgentActionCard
import com.example.ui.theme.*
import com.example.viewmodel.PersonalOSViewModel

@Composable
fun AgentCenterScreen(
    viewModel: PersonalOSViewModel,
    modifier: Modifier = Modifier
) {
    val allPlans by viewModel.allPlans.collectAsState()
    val pendingPlans by viewModel.pendingPlans.collectAsState()
    val isAiThinking by viewModel.isAiThinking.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 84.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "GOD-LEVEL AGENT OS",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = "Autonomous Reasoning",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    // Run Autonomous Audit Trigger
                    Button(
                        onClick = { viewModel.triggerAutonomousAudit() },
                        enabled = !isAiThinking,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepObsidian),
                        modifier = Modifier.testTag("run_agent_audit_btn")
                    ) {
                        if (isAiThinking) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), color = DeepObsidian, strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Run Audit", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Autonomous Architecture Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        border = BorderStroke(1.5.dp, Brush.horizontalGradient(listOf(CyberCyan, NeonPurple))),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = null, tint = CyberCyan)
                        Text(
                            text = "V2 Action Execution Engine",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    Text(
                        text = "Instead of only giving conversational answers, the agent actively monitors upcoming events, unfinished topics, and conflicting tasks. With your approval, it executes database-level reschedule and task allocation actions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = SurfaceVariantDark,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null, tint = NeuralEmerald, modifier = Modifier.size(16.dp))
                            Text(
                                text = "Human-in-the-loop Guardrail: Action Execution requires 1-tap approval",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeuralEmerald,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        // Pending Approval Plans Section
        if (pendingPlans.isNotEmpty()) {
            item {
                Text(
                    text = "ACTION PLANS AWAITING APPROVAL (${pendingPlans.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = WarningAmber,
                    letterSpacing = 1.sp
                )
            }

            items(pendingPlans) { plan ->
                AgentActionCard(
                    plan = plan,
                    onApprove = viewModel::approveAgentPlan,
                    onDismiss = viewModel::dismissAgentPlan
                )
            }
        }

        // Historical / Executed Plans Section
        val executedPlans = allPlans.filter { it.status != PlanStatus.PENDING_APPROVAL }
        if (executedPlans.isNotEmpty()) {
            item {
                Text(
                    text = "AUDIT LOG & EXECUTED AGENT ACTIONS",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondaryDark,
                    letterSpacing = 1.sp
                )
            }

            items(executedPlans) { plan ->
                AgentActionCard(
                    plan = plan,
                    onApprove = { },
                    onDismiss = { }
                )
            }
        }
    }
}
