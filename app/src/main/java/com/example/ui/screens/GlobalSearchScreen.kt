package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlobalSearchScreen(
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    conversations: List<Conversation>,
    searchedContacts: List<Contact>,
    searchedMessages: List<Message>,
    onSelectConversation: (String) -> Unit,
    onSelectContact: (Contact) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }

    var searchCategory by remember { mutableStateOf("ALL") } // ALL, CHATS, PEOPLE, MESSAGES, FILES

    val matchingConversations = remember(conversations, searchQuery) {
        if (searchQuery.isBlank()) emptyList()
        else conversations.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
            it.lastMessageSnippet.contains(searchQuery, ignoreCase = true)
        }
    }

    val matchingFiles = remember(searchedMessages) {
        searchedMessages.filter { it.type == "DOCUMENT" || it.type == "IMAGE" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onQueryChange,
                        placeholder = { Text("Search LeeTalk...") },
                        singleLine = true,
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { onQueryChange("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = searchCategory == "ALL",
                    onClick = { searchCategory = "ALL" },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = searchCategory == "CHATS",
                    onClick = { searchCategory = "CHATS" },
                    label = { Text("Chats") }
                )
                FilterChip(
                    selected = searchCategory == "PEOPLE",
                    onClick = { searchCategory = "PEOPLE" },
                    label = { Text("People") }
                )
                FilterChip(
                    selected = searchCategory == "MESSAGES",
                    onClick = { searchCategory = "MESSAGES" },
                    label = { Text("Messages") }
                )
            }

            if (searchQuery.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Search people, messages, and files",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    // Matching Conversations
                    if ((searchCategory == "ALL" || searchCategory == "CHATS") && matchingConversations.isNotEmpty()) {
                        item {
                            SearchSectionHeader("Chats")
                        }
                        items(matchingConversations) { conv ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectConversation(conv.id) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = conv.title, size = 40.dp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(conv.title, fontWeight = FontWeight.Bold)
                                    Text(
                                        conv.lastMessageSnippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp)
                        }
                    }

                    // Matching People
                    if ((searchCategory == "ALL" || searchCategory == "PEOPLE") && searchedContacts.isNotEmpty()) {
                        item {
                            SearchSectionHeader("People")
                        }
                        items(searchedContacts) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectContact(contact) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(name = contact.displayName, size = 40.dp, isOnline = contact.isOnline)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(contact.displayName, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        contact.username,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = LeeBlue
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(start = 68.dp), thickness = 0.5.dp)
                        }
                    }

                    // Matching Messages
                    if ((searchCategory == "ALL" || searchCategory == "MESSAGES") && searchedMessages.isNotEmpty()) {
                        item {
                            SearchSectionHeader("Messages")
                        }
                        items(searchedMessages) { msg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectConversation(msg.conversationId) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Message,
                                    contentDescription = null,
                                    tint = LeeBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = msg.senderName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = msg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 2
                                    )
                                }
                            }
                            HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)
                        }
                    }

                    // Files
                    if (matchingFiles.isNotEmpty()) {
                        item {
                            SearchSectionHeader("Files & Media")
                        }
                        items(matchingFiles) { fileMsg ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectConversation(fileMsg.conversationId) }
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = LeeBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = fileMsg.mediaName ?: fileMsg.content,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "In conversation with ${fileMsg.senderName}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
private fun SearchSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = LeeBlue,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
