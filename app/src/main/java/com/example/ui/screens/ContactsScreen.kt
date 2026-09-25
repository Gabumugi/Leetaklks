package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.components.UserAvatar
import com.example.ui.theme.LeeBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(
    contacts: List<Contact>,
    onStartChat: (Contact) -> Unit,
    onStartCall: (Contact) -> Unit,
    onAddContact: (username: String, name: String, email: String, bio: String) -> Unit,
    onBlockContact: (String) -> Unit,
    onUnblockContact: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, REQUESTS, BLOCKED
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }

    val filtered = remember(contacts, selectedTab, searchQuery) {
        contacts.filter { c ->
            val matchesTab = when (selectedTab) {
                "REQUESTS" -> c.relationship == "REQUEST_RECEIVED"
                "BLOCKED" -> c.relationship == "BLOCKED"
                else -> c.relationship == "CONTACT"
            }
            val matchesSearch = searchQuery.isBlank() ||
                    c.displayName.contains(searchQuery, ignoreCase = true) ||
                    c.username.contains(searchQuery, ignoreCase = true)
            matchesTab && matchesSearch
        }
    }

    val requestsCount = remember(contacts) {
        contacts.count { it.relationship == "REQUEST_RECEIVED" }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Contacts",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = LeeBlue,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Add Contact")
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or @username...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == "ALL",
                    onClick = { selectedTab = "ALL" },
                    label = { Text("My Contacts") }
                )
                FilterChip(
                    selected = selectedTab == "REQUESTS",
                    onClick = { selectedTab = "REQUESTS" },
                    label = {
                        Text(if (requestsCount > 0) "Requests ($requestsCount)" else "Requests")
                    }
                )
                FilterChip(
                    selected = selectedTab == "BLOCKED",
                    onClick = { selectedTab = "BLOCKED" },
                    label = { Text("Blocked") }
                )
            }

            if (filtered.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (selectedTab == "REQUESTS") "No pending requests"
                        else if (selectedTab == "BLOCKED") "No blocked users"
                        else "No contacts found",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filtered, key = { it.id }) { contact ->
                        ContactRowItem(
                            contact = contact,
                            tab = selectedTab,
                            onChat = { onStartChat(contact) },
                            onCall = { onStartCall(contact) },
                            onBlock = { onBlockContact(contact.id) },
                            onUnblock = { onUnblockContact(contact.id) },
                            onAccept = { onAcceptRequest(contact.id) }
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

    // Add Contact Dialog
    if (showAddDialog) {
        var newUsername by remember { mutableStateOf("") }
        var newDisplayName by remember { mutableStateOf("") }
        var newEmail by remember { mutableStateOf("") }
        var newBio by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Add Contact", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = newUsername,
                        onValueChange = { newUsername = it },
                        label = { Text("Username") },
                        prefix = { Text("@") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newDisplayName,
                        onValueChange = { newDisplayName = it },
                        label = { Text("Display Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newEmail,
                        onValueChange = { newEmail = it },
                        label = { Text("Email (Optional)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newBio,
                        onValueChange = { newBio = it },
                        label = { Text("Bio / Note (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newDisplayName.isNotBlank() && newUsername.isNotBlank()) {
                            onAddContact(newUsername, newDisplayName, newEmail, newBio)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ContactRowItem(
    contact: Contact,
    tab: String,
    onChat: () -> Unit,
    onCall: () -> Unit,
    onBlock: () -> Unit,
    onUnblock: () -> Unit,
    onAccept: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (tab == "ALL") onChat()
            }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        UserAvatar(
            name = contact.displayName,
            size = 48.dp,
            isOnline = contact.isOnline
        )

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = contact.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = contact.username,
                style = MaterialTheme.typography.bodySmall,
                color = LeeBlue
            )
            if (contact.bio.isNotBlank()) {
                Text(
                    text = contact.bio,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        when (tab) {
            "REQUESTS" -> {
                Button(
                    onClick = onAccept,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LeeBlue)
                ) {
                    Text("Accept", fontSize = 12.sp)
                }
            }
            "BLOCKED" -> {
                OutlinedButton(
                    onClick = onUnblock,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Unblock", fontSize = 12.sp)
                }
            }
            else -> {
                IconButton(onClick = onChat) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Message",
                        tint = LeeBlue
                    )
                }
                IconButton(onClick = onCall) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Block contact", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onBlock()
                            }
                        )
                    }
                }
            }
        }
    }
}
