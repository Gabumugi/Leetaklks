package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.ui.components.AttachmentBottomSheet
import com.example.ui.components.ChatComposer
import com.example.ui.components.MessageBubble
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue
import com.example.ui.theme.LeeGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    conversation: Conversation,
    messages: List<Message>,
    currentUserId: String,
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
    onSendAttachment: (type: String, content: String, mediaName: String?, mediaSize: String?) -> Unit,
    onReply: (Message) -> Unit,
    onReaction: (Message, String) -> Unit,
    onDeleteMessage: (Message) -> Unit,
    onEditMessage: (Message) -> Unit,
    editingMessage: Message?,
    onSubmitEdit: (String) -> Unit,
    onCancelEdit: () -> Unit,
    onStartVoiceCall: () -> Unit,
    onStartVideoCall: () -> Unit,
    onBackClick: () -> Unit,
    typingStatus: String?,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var showAttachments by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    var previewImageDialogUrl by remember { mutableStateOf<String?>(null) }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("chat_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        UserAvatar(
                            name = conversation.title,
                            size = 40.dp,
                            isGroup = conversation.type == "GROUP",
                            isOnline = true
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = conversation.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            val subtitle = when {
                                typingStatus != null -> typingStatus
                                conversation.type == "GROUP" -> "Project Members"
                                else -> "Online"
                            }
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (typingStatus != null) LeeGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (typingStatus != null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onStartVoiceCall) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = "Voice Call",
                            tint = LeeBlue
                        )
                    }
                    IconButton(onClick = onStartVideoCall) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video Call",
                            tint = LeeBlue
                        )
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Search in chat") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Shared media & files") },
                            onClick = { showMenu = false }
                        )
                        DropdownMenuItem(
                            text = { Text("Clear chat") },
                            onClick = { showMenu = false }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            ChatComposer(
                draft = draft,
                onDraftChange = onDraftChange,
                replyingTo = replyingTo,
                onCancelReply = onCancelReply,
                isRecordingVoice = isRecordingVoice,
                recordingDuration = recordingDuration,
                onStartVoiceRecording = onStartVoiceRecording,
                onCancelVoiceRecording = onCancelVoiceRecording,
                onSendVoiceRecording = onSendVoiceRecording,
                onSendMessage = onSendMessage,
                onOpenAttachments = { showAttachments = true },
                modifier = Modifier.imePadding()
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
            ) {
                // Header date badge
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                        ) {
                            Text(
                                text = "Today",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                items(messages, key = { it.id }) { msg ->
                    val isCurrentUser = msg.senderId == currentUserId || msg.senderName.contains("Gabriel")
                    MessageBubble(
                        message = msg,
                        isCurrentUser = isCurrentUser,
                        onReply = onReply,
                        onReaction = { m, emoji -> onReaction(m, emoji) },
                        onDelete = onDeleteMessage,
                        onEdit = onEditMessage,
                        onOpenImage = { previewImageDialogUrl = it }
                    )
                }
            }
        }
    }

    // Attachment bottom sheet
    if (showAttachments) {
        AttachmentBottomSheet(
            onDismiss = { showAttachments = false },
            onSelectAttachment = { type, content, name, size ->
                onSendAttachment(type, content, name, size)
                showAttachments = false
            }
        )
    }

    // Edit message dialog
    if (editingMessage != null) {
        var editContent by remember(editingMessage.content) { mutableStateOf(editingMessage.content) }
        AlertDialog(
            onDismissRequest = onCancelEdit,
            title = { Text("Edit Message") },
            text = {
                OutlinedTextField(
                    value = editContent,
                    onValueChange = { editContent = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = { onSubmitEdit(editContent) },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelEdit) {
                    Text("Cancel")
                }
            }
        )
    }

    // Full screen image preview dialog
    if (previewImageDialogUrl != null) {
        AlertDialog(
            onDismissRequest = { previewImageDialogUrl = null },
            title = { Text("Photo Preview") },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .size(240.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🖼️ High Resolution Photo Viewer\n(LeeTalk Media Storage)")
                }
            },
            confirmButton = {
                TextButton(onClick = { previewImageDialogUrl = null }) {
                    Text("Close")
                }
            }
        )
    }
}
