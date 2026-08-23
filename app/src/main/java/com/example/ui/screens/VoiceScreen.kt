package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.VoiceWaveformVisualizer
import com.example.ui.theme.*
import com.example.viewmodel.PersonalOSViewModel

@Composable
fun VoiceScreen(
    viewModel: PersonalOSViewModel,
    onDismiss: () -> Unit
) {
    val isAiThinking by viewModel.isAiThinking.collectAsState()
    val isSpeaking by viewModel.isSpeaking.collectAsState()
    val messages by viewModel.allMessages.collectAsState()
    val lastAssistantMessage = messages.lastOrNull { it.role.name == "ASSISTANT" }?.content

    var isSimulatedListening by remember { mutableStateOf(false) }
    var spokenTranscript by remember { mutableStateOf("") }

    val sampleVoiceQueries = listOf(
        "I have an exam tomorrow at 10 AM",
        "What does my schedule look like today?",
        "Remember that I love working in morning deep focus",
        "Summarize my urgent tasks"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("voice_screen_overlay"),
        color = DeepObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar
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
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking || isSimulatedListening) CyberCyan else NeuralEmerald)
                    )
                    Text(
                        text = if (isSpeaking) "AI SPEAKING" else if (isAiThinking) "REASONING..." else if (isSimulatedListening) "LISTENING..." else "VOICE READY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isSpeaking || isSimulatedListening) CyberCyan else NeuralEmerald,
                        letterSpacing = 1.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_voice_screen_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Voice Mode",
                        tint = TextSecondaryDark
                    )
                }
            }

            // Center Content: Soundwave Visualizer & Live Transcript
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "AI Voice Engine",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimaryDark
                )

                // Voice Waveform Visualizer
                VoiceWaveformVisualizer(
                    isActive = isSpeaking || isSimulatedListening || isAiThinking,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                // Spoken AI Response Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 120.dp, max = 220.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isAiThinking) {
                            CircularProgressIndicator(
                                color = CyberCyan,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                "Synthesizing voice response...",
                                color = CyberCyan,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else if (!lastAssistantMessage.isNullOrBlank()) {
                            Text(
                                text = lastAssistantMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryDark,
                                textAlign = TextAlign.Center,
                                lineHeight = 22.sp
                            )
                        } else {
                            Text(
                                text = "Tap the mic or select a voice prompt below to start speaking with your AI OS.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMutedDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Bottom Area: Quick Voice Prompts & Mic Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "TAP A VOICE PROMPT TO SPEAK",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMutedDark,
                    letterSpacing = 1.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sampleVoiceQueries.forEach { prompt ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            color = SurfaceDark,
                            border = BorderStroke(1.dp, CardBorder)
                        ) {
                            TextButton(
                                onClick = {
                                    spokenTranscript = prompt
                                    viewModel.sendChatMessage(prompt, isVoice = true)
                                },
                                modifier = Modifier.testTag("voice_prompt_${prompt.take(10)}")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "“$prompt”",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondaryDark
                                    )
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = CyberCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Main Microphone Orb
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(if (isSpeaking || isSimulatedListening) CyberCyan else SurfaceVariantDark)
                        .border(2.dp, CyberCyan, CircleShape)
                        .testTag("voice_mic_main_btn"),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = {
                            if (isSpeaking) {
                                viewModel.stopSpeaking()
                            } else {
                                isSimulatedListening = !isSimulatedListening
                                if (isSimulatedListening) {
                                    viewModel.sendChatMessage("What are my highest priority tasks right now?", isVoice = true)
                                }
                            }
                        },
                        modifier = Modifier.size(72.dp)
                    ) {
                        Icon(
                            imageVector = if (isSpeaking) Icons.Default.VolumeOff else Icons.Default.Mic,
                            contentDescription = "Voice Interaction",
                            tint = if (isSpeaking || isSimulatedListening) DeepObsidian else CyberCyan,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
