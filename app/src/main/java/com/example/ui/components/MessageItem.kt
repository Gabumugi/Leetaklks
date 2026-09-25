package com.example.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Message
import com.example.ui.theme.LeeBlue
import com.example.ui.theme.LeeTickRead
import com.example.ui.theme.LeeTickSent
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class, ExperimentalLayoutApi::class)
@Composable
fun MessageBubble(
    message: Message,
    isCurrentUser: Boolean,
    onReply: (Message) -> Unit,
    onReaction: (Message, String) -> Unit,
    onDelete: (Message) -> Unit,
    onEdit: (Message) -> Unit,
    onOpenImage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    val bubbleShape = if (isCurrentUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp)
    }

    val bubbleColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = if (isCurrentUser) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val timeString = remember(message.createdAt) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(message.createdAt))
    }

    val isDeleted = message.deletedAt != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 3.dp),
        contentAlignment = if (isCurrentUser) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = if (isCurrentUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                shape = bubbleShape,
                color = bubbleColor,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .widthIn(min = 80.dp, max = 310.dp)
                    .combinedClickable(
                        onClick = {
                            if (message.type == "IMAGE") {
                                onOpenImage(message.mediaUrl ?: "")
                            } else if (message.type == "VOICE" || message.type == "AUDIO") {
                                isAudioPlaying = !isAudioPlaying
                            }
                        },
                        onLongClick = {
                            if (!isDeleted) showMenu = true
                        }
                    )
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 8.dp,
                        bottom = 6.dp
                    )
                ) {
                    // Sender name in group conversations if not current user
                    if (!isCurrentUser && message.senderName.isNotBlank()) {
                        Text(
                            text = message.senderName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    // Quoted reply banner
                    if (message.replyToId != null && !message.replySnippet.isNullOrBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isCurrentUser) Color.Black.copy(alpha = 0.15f)
                                    else Color.White.copy(alpha = 0.5f)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(28.dp)
                                    .background(if (isCurrentUser) Color.White else LeeBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = message.replySenderName ?: "Reply",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isCurrentUser) textColor.copy(alpha = 0.9f) else LeeBlue
                                )
                                Text(
                                    text = message.replySnippet,
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    color = textColor.copy(alpha = 0.75f)
                                )
                            }
                        }
                    }

                    // Content rendering based on message type
                    if (isDeleted) {
                        Text(
                            text = "🚫 This message was deleted",
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic,
                            color = textColor.copy(alpha = 0.6f)
                        )
                    } else {
                        when (message.type) {
                            "TEXT" -> {
                                Text(
                                    text = message.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = textColor
                                )
                            }
                            "IMAGE" -> {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(160.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF1E293B)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Image,
                                            contentDescription = "Photo",
                                            tint = Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Text(
                                            text = "Tap to preview",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White.copy(alpha = 0.8f),
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 8.dp)
                                        )
                                    }
                                    if (message.content.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = message.content,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textColor
                                        )
                                    }
                                }
                            }
                            "VOICE", "AUDIO" -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    IconButton(
                                        onClick = { isAudioPlaying = !isAudioPlaying },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isCurrentUser) Color.White.copy(alpha = 0.25f)
                                                else MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                            )
                                    ) {
                                        Icon(
                                            imageVector = if (isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isAudioPlaying) "Pause" else "Play",
                                            tint = if (isCurrentUser) Color.White else LeeBlue,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Simulated audio waveform bars
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val barHeights = listOf(10, 18, 14, 24, 16, 28, 20, 12, 22, 16, 8, 20, 14, 18)
                                        barHeights.forEachIndexed { idx, h ->
                                            Box(
                                                modifier = Modifier
                                                    .width(3.dp)
                                                    .height(h.dp)
                                                    .clip(RoundedCornerShape(2.dp))
                                                    .background(
                                                        if (isAudioPlaying && idx < 7) {
                                                            if (isCurrentUser) Color.White else LeeBlue
                                                        } else {
                                                            textColor.copy(alpha = 0.4f)
                                                        }
                                                    )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = message.mediaDuration ?: "0:18",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            "DOCUMENT" -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentUser) Color.White.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.InsertDriveFile,
                                            contentDescription = "Document",
                                            tint = if (isCurrentUser) Color.White else LeeBlue,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = message.mediaName ?: "Document.pdf",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textColor,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = message.mediaSize ?: "2.4 MB",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                            "LOCATION" -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentUser) Color.White.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.LocationOn,
                                            contentDescription = "Location",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = message.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textColor
                                            )
                                            Text(
                                                text = message.mediaName ?: "GPS Coordinates",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                            "CONTACT" -> {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isCurrentUser) Color.White.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = "Contact",
                                            tint = if (isCurrentUser) Color.White else LeeBlue,
                                            modifier = Modifier.size(32.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = message.content,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = textColor
                                            )
                                            Text(
                                                text = "LeeTalk Contact",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = textColor.copy(alpha = 0.7f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Metadata row: timestamp, edited status, delivery ticks
                    Row(
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (message.editedAt != null && !isDeleted) {
                            Text(
                                text = "edited ",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = textColor.copy(alpha = 0.6f)
                            )
                        }
                        Text(
                            text = timeString,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            color = textColor.copy(alpha = 0.7f)
                        )

                        if (isCurrentUser && !isDeleted) {
                            Spacer(modifier = Modifier.width(4.dp))
                            when (message.status) {
                                "SENDING" -> {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = "Sending",
                                        tint = textColor.copy(alpha = 0.6f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                "SENT" -> {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Sent",
                                        tint = textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                "DELIVERED" -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Delivered",
                                        tint = LeeTickSent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                "READ" -> {
                                    Icon(
                                        imageVector = Icons.Default.DoneAll,
                                        contentDescription = "Read",
                                        tint = LeeTickRead,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                "FAILED" -> {
                                    Icon(
                                        imageVector = Icons.Default.ErrorOutline,
                                        contentDescription = "Failed",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Reactions row below message
            if (message.reactions.isNotBlank() && !isDeleted) {
                FlowRow(
                    modifier = Modifier.padding(top = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val reactionList = message.reactions.split(",").filter { it.isNotBlank() }
                    reactionList.forEach { reactionStr ->
                        val parts = reactionStr.split(":")
                        val emoji = parts.getOrNull(0) ?: ""
                        val count = parts.getOrNull(1) ?: "1"

                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 1.dp,
                            modifier = Modifier
                                .clip(CircleShape)
                                .combinedClickable(
                                    onClick = { onReaction(message, emoji) }
                                )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 12.sp)
                                if (count != "1") {
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = count,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Context dropdown actions menu
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            // Quick reaction row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val emojiList = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")
                emojiList.forEach { emoji ->
                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clip(CircleShape)
                            .combinedClickable {
                                onReaction(message, emoji)
                                showMenu = false
                            }
                            .padding(4.dp)
                    )
                }
            }

            DropdownMenuItem(
                text = { Text("Reply") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null) },
                onClick = {
                    onReply(message)
                    showMenu = false
                }
            )

            DropdownMenuItem(
                text = { Text("Copy text") },
                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                onClick = { showMenu = false }
            )

            DropdownMenuItem(
                text = { Text("Star message") },
                leadingIcon = { Icon(Icons.Default.Star, contentDescription = null) },
                onClick = { showMenu = false }
            )

            if (isCurrentUser) {
                DropdownMenuItem(
                    text = { Text("Edit") },
                    leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    onClick = {
                        onEdit(message)
                        showMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    onClick = {
                        onDelete(message)
                        showMenu = false
                    }
                )
            }
        }
    }
}
