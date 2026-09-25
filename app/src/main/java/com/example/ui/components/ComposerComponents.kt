package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.ui.theme.LeeBlue

@Composable
fun ChatComposer(
    draft: String,
    onDraftChange: (String) -> Unit,
    replyingTo: Message?,
    onCancelReply: () -> Unit,
    isRecordingVoice: Boolean,
    recordingDuration: Int,
    onStartVoiceRecording: () -> Unit,
    onCancelVoiceRecording: () -> Unit,
    onSendVoiceRecording: () -> Unit,
    onSendMessage: () -> Unit,
    onOpenAttachments: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showQuickEmoji by remember { mutableStateOf(false) }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Reply context banner
            if (replyingTo != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(30.dp)
                            .background(LeeBlue)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Replying to ${replyingTo.senderName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = LeeBlue,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = replyingTo.content,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onCancelReply,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel reply",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Quick emoji row
            if (showQuickEmoji) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val emojis = listOf("👋", "😊", "❤️", "👍", "🔥", "🚀", "🎉", "🙏")
                    emojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 22.sp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable {
                                    onDraftChange(draft + emoji)
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Active voice recording bar vs standard composer
            if (isRecordingVoice) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Recording",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    val min = recordingDuration / 60
                    val sec = recordingDuration % 60
                    Text(
                        text = String.format("Recording 0%d:%02d", min, sec),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFEF4444),
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clickable { onCancelVoiceRecording() }
                            .padding(8.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onSendVoiceRecording,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(LeeBlue)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send voice note",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Attachment + button
                    IconButton(
                        onClick = onOpenAttachments,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Attach media",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Emoji toggle
                    IconButton(
                        onClick = { showQuickEmoji = !showQuickEmoji },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SentimentSatisfiedAlt,
                            contentDescription = "Emojis",
                            tint = if (showQuickEmoji) LeeBlue else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Text Field
                    OutlinedTextField(
                        value = draft,
                        onValueChange = onDraftChange,
                        placeholder = { Text("Type a message...", fontSize = 14.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp),
                        maxLines = 4,
                        shape = RoundedCornerShape(22.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Dynamic action button: Mic if empty, Send if typed
                    if (draft.isNotBlank()) {
                        IconButton(
                            onClick = onSendMessage,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(LeeBlue)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send message",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    } else {
                        IconButton(
                            onClick = onStartVoiceRecording,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Hold to record voice message",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentBottomSheet(
    onDismiss: () -> Unit,
    onSelectAttachment: (type: String, content: String, mediaName: String?, mediaSize: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Share Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOption(
                    icon = Icons.Default.CameraAlt,
                    label = "Camera",
                    color = Color(0xFFE11D48),
                    onClick = {
                        onSelectAttachment("IMAGE", "Photo from Camera", "Camera_Capture_01.jpg", "1.8 MB")
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Image,
                    label = "Gallery",
                    color = Color(0xFF7C3AED),
                    onClick = {
                        onSelectAttachment("IMAGE", "Shared image from Gallery", "Design_Screenshot.png", "2.1 MB")
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Description,
                    label = "Document",
                    color = Color(0xFF2563EB),
                    onClick = {
                        onSelectAttachment("DOCUMENT", "Quarterly Report", "LeeTalk_Roadmap_2026.pdf", "3.4 MB")
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                AttachmentOption(
                    icon = Icons.Default.Audiotrack,
                    label = "Audio",
                    color = Color(0xFFEA580C),
                    onClick = {
                        onSelectAttachment("AUDIO", "Voice Recording", "Voice_Note_01.m4a", "850 KB")
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.LocationOn,
                    label = "Location",
                    color = Color(0xFF0D9488),
                    onClick = {
                        onSelectAttachment("LOCATION", "Nairobi Innovation Hub", "-1.2921, 36.8219", null)
                    }
                )
                AttachmentOption(
                    icon = Icons.Default.Person,
                    label = "Contact",
                    color = Color(0xFF0284C7),
                    onClick = {
                        onSelectAttachment("CONTACT", "Mary Wanjiku (@mary)", null, null)
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
    }
}
