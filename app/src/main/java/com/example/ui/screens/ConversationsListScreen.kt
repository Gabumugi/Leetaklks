package com.example.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.ui.components.OfflineBanner
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationsListScreen(
    conversations: List<Conversation>,
    contacts: List<Contact>,
    isOffline: Boolean,
    onConversationClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onToggleOffline: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onCreateGroup: (title: String, desc: String, members: List<String>) -> Unit,
    onStartDirectChat: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    var filterTab by remember { mutableStateOf("ALL") } // ALL, UNREAD, GROUPS
    var showMenu by remember { mutableStateOf(false) }
    var showNewChatDialog by remember { mutableStateOf(false) }
    var showNewGroupDialog by remember { mutableStateOf(false) }

    val filteredConversations = remember(conversations, filterTab) {
        when (filterTab) {
            "UNREAD" -> conversations.filter { it.unreadCount > 0 }
            "GROUPS" -> conversations.filter { it.type == "GROUP" }
            else -> conversations
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "LeeTalk",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LeeBlue.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PWA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LeeBlue
                                )
                            }
                        }
                        Text(
                            text = "Talk. Share. Connect.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onSearchClick,
                        modifier = Modifier.testTag("search_icon_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search conversations")
                    }

                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New Group") },
                            leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                showNewGroupDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(if (isOffline) "Switch to Online" else "Simulate Offline Mode")
                            },
                            leadingIcon = {
                                Icon(
                                    if (isOffline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                    contentDescription = null
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleOffline()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewChatDialog = true },
                containerColor = LeeBlue,
                contentColor = Color.White,
                modifier = Modifier.testTag("new_chat_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Conversation")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Offline banner
            OfflineBanner(isOffline = isOffline, onReconnectClick = onToggleOffline)

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filterTab == "ALL",
                    onClick = { filterTab = "ALL" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filterTab == "UNREAD",
                    onClick = { filterTab = "UNREAD" },
                    label = {
                        val unreadTotal = conversations.sumOf { it.unreadCount }
                        Text(if (unreadTotal > 0) "Unread ($unreadTotal)" else "Unread")
                    }
                )
                FilterChip(
                    selected = filterTab == "GROUPS",
                    onClick = { filterTab = "GROUPS" },
                    label = { Text("Groups") }
                )
            }

            if (filteredConversations.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No conversations yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Start a conversation with someone on LeeTalk",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { showNewChatDialog = true },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("New Chat")
                        }
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredConversations, key = { it.id }) { conv ->
                        ConversationItem(
                            conversation = conv,
                            onClick = { onConversationClick(conv.id) },
                            onTogglePin = { onTogglePin(conv.id) },
                            onToggleMute = { onToggleMute(conv.id) }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 76.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }
    }

    // New Chat Contact Picker Dialog
    if (showNewChatDialog) {
        AlertDialog(
            onDismissRequest = { showNewChatDialog = false },
            title = { Text("Start a Conversation", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Select a contact:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    contacts.filter { it.relationship == "CONTACT" }.forEach { contact ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    showNewChatDialog = false
                                    onStartDirectChat(contact)
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = contact.displayName, size = 40.dp, isOnline = contact.isOnline)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(text = contact.displayName, fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = contact.username,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showNewChatDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // New Group Dialog
    if (showNewGroupDialog) {
        var groupTitle by remember { mutableStateOf("") }
        var groupDesc by remember { mutableStateOf("") }
        val selectedMemberIds = remember { mutableStateOf(mutableListOf<String>()) }

        AlertDialog(
            onDismissRequest = { showNewGroupDialog = false },
            title = { Text("Create New Group", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = groupTitle,
                        onValueChange = { groupTitle = it },
                        label = { Text("Group Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = groupDesc,
                        onValueChange = { groupDesc = it },
                        label = { Text("Description (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Add Members:", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    contacts.filter { it.relationship == "CONTACT" }.forEach { contact ->
                        val isSelected = selectedMemberIds.value.contains(contact.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .clickable {
                                    val list = selectedMemberIds.value.toMutableList()
                                    if (isSelected) list.remove(contact.id) else list.add(contact.id)
                                    selectedMemberIds.value = list
                                }
                                .background(if (isSelected) LeeBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(name = contact.displayName, size = 32.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(contact.displayName, modifier = Modifier.weight(1f))
                            if (isSelected) {
                                Text("✓ Added", color = LeeBlue, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (groupTitle.isNotBlank()) {
                            onCreateGroup(groupTitle, groupDesc, selectedMemberIds.value)
                            showNewGroupDialog = false
                        }
                    },
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewGroupDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ConversationItem(
    conversation: Conversation,
    onClick: () -> Unit,
    onTogglePin: () -> Unit,
    onToggleMute: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showContextMenu by remember { mutableStateOf(false) }

    val formattedTime = remember(conversation.lastMessageTime) {
        val diff = System.currentTimeMillis() - conversation.lastMessageTime
        if (diff < 86400000) {
            SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(conversation.lastMessageTime))
        } else {
            SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(conversation.lastMessageTime))
        }
    }

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = { showContextMenu = true }
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserAvatar(
                name = conversation.title,
                size = 52.dp,
                isGroup = conversation.type == "GROUP",
                isOnline = conversation.type != "GROUP"
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (conversation.unreadCount > 0) LeeBlue else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.lastMessageSnippet.ifBlank { "No messages yet" },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (conversation.unreadCount > 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = if (conversation.unreadCount > 0) FontWeight.Medium else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (conversation.isMuted) {
                            Icon(
                                imageVector = Icons.Default.NotificationsOff,
                                contentDescription = "Muted",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = "Pinned",
                                tint = LeeBlue,
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LeeBlue)
                                    .padding(horizontal = 7.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        DropdownMenu(
            expanded = showContextMenu,
            onDismissRequest = { showContextMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text(if (conversation.isPinned) "Unpin chat" else "Pin chat") },
                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                onClick = {
                    showContextMenu = false
                    onTogglePin()
                }
            )
            DropdownMenuItem(
                text = { Text(if (conversation.isMuted) "Unmute notifications" else "Mute notifications") },
                leadingIcon = { Icon(Icons.Default.NotificationsOff, contentDescription = null) },
                onClick = {
                    showContextMenu = false
                    onToggleMute()
                }
            )
        }
    }
}
