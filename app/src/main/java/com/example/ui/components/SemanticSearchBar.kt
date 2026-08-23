package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.UnifiedSearchResult
import com.example.ui.theme.*

@Composable
fun SemanticSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    searchResult: UnifiedSearchResult?,
    onResultSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Input Box
        OutlinedTextField(
            value = query,
            onValueChange = {
                onQueryChange(it)
                isExpanded = it.isNotBlank()
            },
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(SurfaceDark)
                .testTag("semantic_search_input"),
            placeholder = {
                Text(
                    "Search across memory, tasks, notes, docs...",
                    color = TextMutedDark,
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = CyberCyan
                )
            },
            trailingIcon = {
                if (query.isNotBlank()) {
                    IconButton(
                        onClick = {
                            onQueryChange("")
                            isExpanded = false
                            focusManager.clearFocus()
                        },
                        modifier = Modifier.testTag("clear_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear",
                            tint = TextSecondaryDark
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = CyberCyan,
                unfocusedBorderColor = CardBorder,
                focusedTextColor = TextPrimaryDark,
                unfocusedTextColor = TextPrimaryDark,
                cursorColor = CyberCyan
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = {
                focusManager.clearFocus()
            })
        )

        // Dropdown Search Results Overlay
        AnimatedVisibility(visible = isExpanded && query.isNotBlank() && searchResult != null) {
            searchResult?.let { results ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                        .heightIn(max = 340.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    if (results.totalCount == 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No matches found in your AI OS brain for '$query'",
                                color = TextMutedDark,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Tasks results
                            if (results.tasks.isNotEmpty()) {
                                item {
                                    Text(
                                        "TASKS (${results.tasks.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                items(results.tasks) { task ->
                                    SearchResultRow(
                                        icon = Icons.Default.CheckCircleOutline,
                                        title = task.title,
                                        subtitle = task.category,
                                        tag = task.priority.name,
                                        tagColor = CyberCyan,
                                        onClick = {
                                            onResultSelected("Task: ${task.title}")
                                            isExpanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            }

                            // Notes results
                            if (results.notes.isNotEmpty()) {
                                item {
                                    Text(
                                        "NOTES (${results.notes.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonPurple,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                items(results.notes) { note ->
                                    SearchResultRow(
                                        icon = Icons.Default.Description,
                                        title = note.title,
                                        subtitle = note.summary.ifBlank { note.content.take(60) },
                                        tag = "Note",
                                        tagColor = NeonPurple,
                                        onClick = {
                                            onResultSelected("Note: ${note.title}")
                                            isExpanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            }

                            // Memories results
                            if (results.memories.isNotEmpty()) {
                                item {
                                    Text(
                                        "LONG-TERM MEMORY (${results.memories.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeuralEmerald,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                items(results.memories) { mem ->
                                    SearchResultRow(
                                        icon = Icons.Default.Psychology,
                                        title = mem.content,
                                        subtitle = mem.source,
                                        tag = mem.category.name,
                                        tagColor = NeuralEmerald,
                                        onClick = {
                                            onResultSelected("Memory: ${mem.content}")
                                            isExpanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            }

                            // Documents results
                            if (results.docs.isNotEmpty()) {
                                item {
                                    Text(
                                        "DOCUMENT BRAIN (${results.docs.size})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarningAmber,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                items(results.docs) { doc ->
                                    SearchResultRow(
                                        icon = Icons.Default.Folder,
                                        title = doc.title,
                                        subtitle = doc.summary.ifBlank { doc.content.take(60) },
                                        tag = doc.fileType,
                                        tagColor = WarningAmber,
                                        onClick = {
                                            onResultSelected("Doc: ${doc.title}")
                                            isExpanded = false
                                            focusManager.clearFocus()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    tag: String,
    tagColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        color = SurfaceVariantDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tagColor,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimaryDark,
                    maxLines = 1
                )
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondaryDark,
                        maxLines = 1
                    )
                }
            }
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = tagColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = tag,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = tagColor,
                    fontSize = 9.sp
                )
            }
        }
    }
}
