package com.example.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.data.model.CallRecord
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.DeviceSession
import com.example.data.model.User
import com.example.ui.theme.LeeBlue
import com.example.ui.viewmodel.MainTab

@Composable
fun MainScaffold(
    activeTab: MainTab,
    onTabSelected: (MainTab) -> Unit,
    conversations: List<Conversation>,
    contacts: List<Contact>,
    calls: List<CallRecord>,
    currentUser: User?,
    deviceSessions: List<DeviceSession>,
    isOffline: Boolean,
    readReceipts: Boolean,
    typingIndicators: Boolean,
    notifications: Boolean,
    onConversationClick: (String) -> Unit,
    onSearchClick: () -> Unit,
    onToggleOffline: () -> Unit,
    onTogglePin: (String) -> Unit,
    onToggleMute: (String) -> Unit,
    onCreateGroup: (String, String, List<String>) -> Unit,
    onStartDirectChat: (Contact) -> Unit,
    onStartCall: (String, String) -> Unit,
    onAddContact: (String, String, String, String) -> Unit,
    onBlockContact: (String) -> Unit,
    onUnblockContact: (String) -> Unit,
    onAcceptRequest: (String) -> Unit,
    onClearCalls: () -> Unit,
    onToggleReadReceipts: () -> Unit,
    onToggleTypingIndicators: () -> Unit,
    onToggleNotifications: () -> Unit,
    onRevokeSession: (String) -> Unit,
    onUpdateProfile: (String, String, String, String) -> Unit,
    onLogout: () -> Unit,
    onDeleteAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalUnread = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                NavigationBarItem(
                    selected = activeTab == MainTab.CHATS,
                    onClick = { onTabSelected(MainTab.CHATS) },
                    icon = {
                        if (totalUnread > 0) {
                            BadgedBox(badge = { Badge { Text(totalUnread.toString()) } }) {
                                Icon(Icons.Default.ChatBubble, contentDescription = "Chats")
                            }
                        } else {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Chats")
                        }
                    },
                    label = { Text("Chats") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeeBlue,
                        selectedTextColor = LeeBlue,
                        indicatorColor = LeeBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_chats")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.CONTACTS,
                    onClick = { onTabSelected(MainTab.CONTACTS) },
                    icon = { Icon(Icons.Default.People, contentDescription = "Contacts") },
                    label = { Text("Contacts") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeeBlue,
                        selectedTextColor = LeeBlue,
                        indicatorColor = LeeBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_contacts")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.CALLS,
                    onClick = { onTabSelected(MainTab.CALLS) },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Calls") },
                    label = { Text("Calls") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeeBlue,
                        selectedTextColor = LeeBlue,
                        indicatorColor = LeeBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_calls")
                )

                NavigationBarItem(
                    selected = activeTab == MainTab.ME,
                    onClick = { onTabSelected(MainTab.ME) },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Me") },
                    label = { Text("Me") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LeeBlue,
                        selectedTextColor = LeeBlue,
                        indicatorColor = LeeBlue.copy(alpha = 0.15f)
                    ),
                    modifier = Modifier.testTag("tab_me")
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                MainTab.CHATS -> {
                    ConversationsListScreen(
                        conversations = conversations,
                        contacts = contacts,
                        isOffline = isOffline,
                        onConversationClick = onConversationClick,
                        onSearchClick = onSearchClick,
                        onToggleOffline = onToggleOffline,
                        onTogglePin = onTogglePin,
                        onToggleMute = onToggleMute,
                        onCreateGroup = onCreateGroup,
                        onStartDirectChat = onStartDirectChat
                    )
                }

                MainTab.CONTACTS -> {
                    ContactsScreen(
                        contacts = contacts,
                        onStartChat = onStartDirectChat,
                        onStartCall = { contact -> onStartCall(contact.displayName, "VOICE") },
                        onAddContact = onAddContact,
                        onBlockContact = onBlockContact,
                        onUnblockContact = onUnblockContact,
                        onAcceptRequest = onAcceptRequest
                    )
                }

                MainTab.CALLS -> {
                    CallsScreen(
                        calls = calls,
                        onStartCall = onStartCall,
                        onClearCalls = onClearCalls
                    )
                }

                MainTab.ME -> {
                    ProfileSettingsScreen(
                        user = currentUser,
                        deviceSessions = deviceSessions,
                        isOffline = isOffline,
                        readReceipts = readReceipts,
                        typingIndicators = typingIndicators,
                        notifications = notifications,
                        onToggleOffline = onToggleOffline,
                        onToggleReadReceipts = onToggleReadReceipts,
                        onToggleTypingIndicators = onToggleTypingIndicators,
                        onToggleNotifications = onToggleNotifications,
                        onRevokeSession = onRevokeSession,
                        onUpdateProfile = onUpdateProfile,
                        onLogout = onLogout,
                        onDeleteAccount = onDeleteAccount
                    )
                }
            }
        }
    }
}
