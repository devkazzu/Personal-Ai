package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.OSTab
import com.example.viewmodel.PersonalOSViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: PersonalOSViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PersonalOSApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun PersonalOSApp(viewModel: PersonalOSViewModel) {
    val currentTab by viewModel.currentTab.collectAsState()
    val isVoiceOverlayOpen by viewModel.isVoiceOverlayOpen.collectAsState()
    val pendingPlans by viewModel.pendingPlans.collectAsState()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceDark,
                contentColor = CyberCyan,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = CardBorder
                    )
            ) {
                // Hub / Dashboard
                NavigationBarItem(
                    selected = currentTab == OSTab.DASHBOARD,
                    onClick = { viewModel.selectTab(OSTab.DASHBOARD) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.SpaceDashboard,
                            contentDescription = "Hub"
                        )
                    },
                    label = { Text("Hub", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_hub")
                )

                // AI Chat
                NavigationBarItem(
                    selected = currentTab == OSTab.CHAT,
                    onClick = { viewModel.selectTab(OSTab.CHAT) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "AI Chat"
                        )
                    },
                    label = { Text("AI Chat", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.CHAT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_chat")
                )

                // Tasks & Calendar
                NavigationBarItem(
                    selected = currentTab == OSTab.TASKS_CALENDAR,
                    onClick = { viewModel.selectTab(OSTab.TASKS_CALENDAR) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Agenda"
                        )
                    },
                    label = { Text("Agenda", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.TASKS_CALENDAR) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = CyberCyan,
                        indicatorColor = CyberCyan,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_agenda")
                )

                // Notes & Docs Brain
                NavigationBarItem(
                    selected = currentTab == OSTab.NOTES_DOCS,
                    onClick = { viewModel.selectTab(OSTab.NOTES_DOCS) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = "Knowledge"
                        )
                    },
                    label = { Text("Brain", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.NOTES_DOCS) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = NeonPurple,
                        indicatorColor = NeonPurple,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_knowledge")
                )

                // Memory Vault
                NavigationBarItem(
                    selected = currentTab == OSTab.MEMORY_VAULT,
                    onClick = { viewModel.selectTab(OSTab.MEMORY_VAULT) },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Psychology,
                            contentDescription = "Memory"
                        )
                    },
                    label = { Text("Memory", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.MEMORY_VAULT) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = NeuralEmerald,
                        indicatorColor = NeuralEmerald,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_memory")
                )

                // Agent OS Center
                NavigationBarItem(
                    selected = currentTab == OSTab.AGENT_CENTER,
                    onClick = { viewModel.selectTab(OSTab.AGENT_CENTER) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (pendingPlans.isNotEmpty()) {
                                    Badge(
                                        containerColor = WarningAmber,
                                        contentColor = DeepObsidian
                                    ) {
                                        Text("${pendingPlans.size}", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Agent OS"
                            )
                        }
                    },
                    label = { Text("Agent", fontSize = 10.sp, fontWeight = if (currentTab == OSTab.AGENT_CENTER) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DeepObsidian,
                        selectedTextColor = WarningAmber,
                        indicatorColor = WarningAmber,
                        unselectedIconColor = TextMutedDark,
                        unselectedTextColor = TextMutedDark
                    ),
                    modifier = Modifier.testTag("nav_agent")
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                OSTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                OSTab.CHAT -> ChatScreen(viewModel = viewModel)
                OSTab.TASKS_CALENDAR -> TasksCalendarScreen(viewModel = viewModel)
                OSTab.NOTES_DOCS -> NotesDocsScreen(viewModel = viewModel)
                OSTab.MEMORY_VAULT -> MemoryVaultScreen(viewModel = viewModel)
                OSTab.AGENT_CENTER -> AgentCenterScreen(viewModel = viewModel)
            }

            // Full-screen Animated Voice Overlay
            AnimatedVisibility(
                visible = isVoiceOverlayOpen,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
            ) {
                VoiceScreen(
                    viewModel = viewModel,
                    onDismiss = { viewModel.closeVoiceOverlay() }
                )
            }
        }
    }
}
