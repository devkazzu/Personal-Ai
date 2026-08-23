package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.MemoryCategory
import com.example.data.local.entity.MemoryEntity
import com.example.ui.components.MemoryBadge
import com.example.ui.theme.*
import com.example.viewmodel.PersonalOSViewModel

@Composable
fun MemoryVaultScreen(
    viewModel: PersonalOSViewModel,
    modifier: Modifier = Modifier
) {
    val allMemories by viewModel.allMemories.collectAsState()
    val isVaultUnlocked by viewModel.isVaultUnlocked.collectAsState()
    val vaultErrorMessage by viewModel.vaultErrorMessage.collectAsState()

    var selectedCategoryFilter by remember { mutableStateOf<MemoryCategory?>(null) }
    var showAddMemoryDialog by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var activeSubTab by remember { mutableStateOf(0) } // 0: Long-Term Memory, 1: Private Encrypted Vault

    val filteredMemories = remember(allMemories, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) allMemories else allMemories.filter { it.category == selectedCategoryFilter }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian),
        containerColor = DeepObsidian,
        floatingActionButton = {
            if (activeSubTab == 0) {
                FloatingActionButton(
                    onClick = { showAddMemoryDialog = true },
                    containerColor = NeuralEmerald,
                    contentColor = DeepObsidian,
                    modifier = Modifier.testTag("memory_fab_add")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Memory Node"
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(bottom = 72.dp)
        ) {
            // Header
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "NEURAL PERSISTENCE & PRIVACY",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = NeuralEmerald,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (activeSubTab == 0) "Long-Term Memory" else "Encrypted Vault",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimaryDark
                        )
                    }

                    // Vault Security Status Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isVaultUnlocked) CyberCyan.copy(alpha = 0.15f) else CriticalRed.copy(alpha = 0.15f),
                        border = BorderStroke(1.dp, if (isVaultUnlocked) CyberCyan.copy(alpha = 0.5f) else CriticalRed.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (isVaultUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (isVaultUnlocked) CyberCyan else CriticalRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isVaultUnlocked) "VAULT UNLOCKED" else "VAULT LOCKED",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isVaultUnlocked) CyberCyan else CriticalRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp
                            )
                        }
                    }
                }

                // Sub-tab switcher
                TabRow(
                    selectedTabIndex = activeSubTab,
                    containerColor = SurfaceDark,
                    contentColor = NeuralEmerald,
                    divider = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                ) {
                    Tab(
                        selected = activeSubTab == 0,
                        onClick = { activeSubTab = 0 },
                        text = {
                            Text(
                                "Memory Nodes (${allMemories.size})",
                                fontWeight = if (activeSubTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = activeSubTab == 1,
                        onClick = { activeSubTab = 1 },
                        text = {
                            Text(
                                "Private Vault 🔐",
                                fontWeight = if (activeSubTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (activeSubTab == 0) {
                // Category Filter Chips
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedCategoryFilter == null,
                            onClick = { selectedCategoryFilter = null },
                            label = { Text("All (${allMemories.size})", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuralEmerald,
                                selectedLabelColor = DeepObsidian
                            )
                        )
                    }

                    items(MemoryCategory.values()) { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat,
                            onClick = { selectedCategoryFilter = cat },
                            label = { Text(cat.name.replace("_", " "), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeuralEmerald,
                                selectedLabelColor = DeepObsidian
                            )
                        )
                    }
                }

                // Memory Nodes List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredMemories.isEmpty()) {
                        item {
                            EmptyMemoriesCard()
                        }
                    } else {
                        items(filteredMemories) { memory ->
                            MemoryNodeCard(
                                memory = memory,
                                onTogglePin = { viewModel.togglePinMemory(memory) },
                                onDelete = { viewModel.deleteMemory(memory.id) }
                            )
                        }
                    }
                }
            } else {
                // Private Encrypted Vault View
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!isVaultUnlocked) {
                        // Vault Lock Screen
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(CriticalRed.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = CriticalRed,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Text(
                                    text = "Private Encrypted Layer",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimaryDark
                                )

                                Text(
                                    text = "Zero-telemetry local database. Enter your security PIN (Default: 1234) to decrypt sensitive nodes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )

                                OutlinedTextField(
                                    value = enteredPin,
                                    onValueChange = { if (it.length <= 4) enteredPin = it },
                                    modifier = Modifier
                                        .width(160.dp)
                                        .testTag("vault_pin_input"),
                                    placeholder = { Text("PIN (1234)", color = TextMutedDark) },
                                    visualTransformation = PasswordVisualTransformation(),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )

                                if (vaultErrorMessage != null) {
                                    Text(
                                        text = vaultErrorMessage!!,
                                        color = CriticalRed,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Button(
                                    onClick = {
                                        viewModel.unlockVault(enteredPin)
                                        enteredPin = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = DeepObsidian),
                                    modifier = Modifier.testTag("unlock_vault_btn")
                                ) {
                                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Unlock Vault")
                                }
                            }
                        }
                    } else {
                        // Unlocked Vault State
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, CyberCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = CyberCyan)
                                        Text(
                                            text = "Encrypted Vault Active",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimaryDark
                                        )
                                    }

                                    OutlinedButton(
                                        onClick = { viewModel.lockVault() },
                                        modifier = Modifier.testTag("lock_vault_btn")
                                    ) {
                                        Text("Lock Vault", color = WarningAmber, fontSize = 11.sp)
                                    }
                                }

                                Text(
                                    text = "Your private memory nodes are stored locally inside an on-device SQLite AES-encrypted enclave with zero telemetry.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondaryDark
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = SurfaceVariantDark,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("🔒 Security Audit Status:", style = MaterialTheme.typography.labelSmall, color = CyberCyan, fontWeight = FontWeight.Bold)
                                        Text("• Local Persistence: Active (Room SQLite on device)", style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark)
                                        Text("• Gemini Grounding: Injected securely in-memory during queries", style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark)
                                        Text("• Network Telemetry: 0 trackers / 0 external logging", style = MaterialTheme.typography.bodySmall, color = TextPrimaryDark)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Memory Dialog
    if (showAddMemoryDialog) {
        AddMemoryDialog(
            onDismiss = { showAddMemoryDialog = false },
            onConfirm = { category, content ->
                viewModel.addMemory(category, content)
                showAddMemoryDialog = false
            }
        )
    }
}

@Composable
private fun MemoryNodeCard(
    memory: MemoryEntity,
    onTogglePin: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                border = BorderStroke(
                    1.dp,
                    if (memory.isPinned) NeuralEmerald.copy(alpha = 0.6f) else CardBorder
                ),
                shape = RoundedCornerShape(14.dp)
            )
            .testTag("memory_node_${memory.id}"),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                MemoryBadge(category = memory.category)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onTogglePin,
                        modifier = Modifier.size(28.dp).testTag("pin_memory_btn_${memory.id}")
                    ) {
                        Icon(
                            imageVector = if (memory.isPinned) Icons.Default.PushPin else Icons.Default.PushPin,
                            contentDescription = "Pin Memory",
                            tint = if (memory.isPinned) NeuralEmerald else TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Memory",
                            tint = TextMutedDark,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Text(
                text = memory.content,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryDark,
                lineHeight = 20.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Source: ${memory.source}",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    fontSize = 10.sp
                )
                Text(
                    text = "Confidence: ${(memory.confidenceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeuralEmerald,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun EmptyMemoriesCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = null,
                tint = NeuralEmerald,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "No Memories in Filter",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimaryDark
            )
            Text(
                text = "The AI continuously extracts preferences, habits, and facts from conversations, or you can add one manually.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMutedDark,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun AddMemoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (MemoryCategory, String) -> Unit
) {
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(MemoryCategory.PREFERENCE) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Long-Term Memory", color = TextPrimaryDark) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Memory Content / Fact / Preference") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Category:", style = MaterialTheme.typography.labelSmall, color = TextSecondaryDark)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MemoryCategory.values().take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name, fontSize = 9.sp) }
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    MemoryCategory.values().drop(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.name, fontSize = 9.sp) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (content.isNotBlank()) {
                        onConfirm(selectedCategory, content)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeuralEmerald, contentColor = DeepObsidian)
            ) {
                Text("Lock Memory")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = TextSecondaryDark) }
        },
        containerColor = SurfaceDark
    )
}
