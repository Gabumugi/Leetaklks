package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.QRScannerOverlay
import com.example.ui.screens.ActiveCallScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.MainScaffold
import com.example.ui.theme.LeeTalkTheme
import com.example.ui.viewmodel.LeeTalkViewModel
import com.example.ui.viewmodel.ScreenState

class MainActivity : ComponentActivity() {
    private val viewModel: LeeTalkViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDark = when (themeMode) {
                "LIGHT" -> false
                "DARK" -> true
                else -> isSystemInDarkTheme()
            }

            LeeTalkTheme(darkTheme = isDark) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    LeeTalkApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun LeeTalkApp(viewModel: LeeTalkViewModel) {
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val activeTab by viewModel.activeTab.collectAsStateWithLifecycle()
    val conversations by viewModel.conversations.collectAsStateWithLifecycle()
    val contacts by viewModel.contacts.collectAsStateWithLifecycle()
    val calls by viewModel.calls.collectAsStateWithLifecycle()
    val isOffline by viewModel.isOffline.collectAsStateWithLifecycle()
    val typingMap by viewModel.typingStatus.collectAsStateWithLifecycle()
    val deviceSessions by viewModel.deviceSessions.collectAsStateWithLifecycle()

    val currentConversation by viewModel.currentConversation.collectAsStateWithLifecycle()
    val currentMessages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val messageDraft by viewModel.messageDraft.collectAsStateWithLifecycle()
    val replyingTo by viewModel.replyingTo.collectAsStateWithLifecycle()
    val isRecordingVoice by viewModel.isRecordingVoice.collectAsStateWithLifecycle()
    val recordingDuration by viewModel.recordingDuration.collectAsStateWithLifecycle()
    val editingMessage by viewModel.editingMessage.collectAsStateWithLifecycle()

    val activeCallContact by viewModel.activeCallContact.collectAsStateWithLifecycle()
    val activeCallType by viewModel.activeCallType.collectAsStateWithLifecycle()
    val callDurationSeconds by viewModel.callDurationSeconds.collectAsStateWithLifecycle()
    val isCallMuted by viewModel.isCallMuted.collectAsStateWithLifecycle()
    val isSpeakerOn by viewModel.isSpeakerOn.collectAsStateWithLifecycle()
    val isVideoDisabled by viewModel.isVideoDisabled.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchedContacts by viewModel.searchedContacts.collectAsStateWithLifecycle()
    val searchedMessages by viewModel.searchedMessages.collectAsStateWithLifecycle()

    val readReceipts by viewModel.readReceiptsEnabled.collectAsStateWithLifecycle()
    val typingIndicators by viewModel.typingIndicatorsEnabled.collectAsStateWithLifecycle()
    val notifications by viewModel.notificationsEnabled.collectAsStateWithLifecycle()
    val showQRScanner by viewModel.showQRScanner.collectAsStateWithLifecycle()
    val pairedSuccessEvent by viewModel.pairedSuccessEvent.collectAsStateWithLifecycle()

    if (currentUser == null) {
        AuthScreen(
            onLoginSuccess = { email, name, username ->
                viewModel.login(email, name, username)
            }
        )
    } else {
        when (currentScreen) {
            ScreenState.AUTH -> {
                AuthScreen(
                    onLoginSuccess = { email, name, username ->
                        viewModel.login(email, name, username)
                    }
                )
            }

            ScreenState.CHAT -> {
                val conv = currentConversation
                if (conv != null) {
                    val typingForConv = typingMap[conv.id]
                    ChatScreen(
                        conversation = conv,
                        messages = currentMessages,
                        currentUserId = currentUser?.userId ?: "user-gabriel",
                        draft = messageDraft,
                        onDraftChange = { viewModel.onDraftChange(it) },
                        replyingTo = replyingTo,
                        onCancelReply = { viewModel.setReplyingTo(null) },
                        isRecordingVoice = isRecordingVoice,
                        recordingDuration = recordingDuration,
                        onStartVoiceRecording = { viewModel.startVoiceRecording() },
                        onCancelVoiceRecording = { viewModel.cancelVoiceRecording() },
                        onSendVoiceRecording = { viewModel.sendVoiceRecording() },
                        onSendMessage = { viewModel.sendMessage() },
                        onSendAttachment = { type, content, name, size ->
                            viewModel.sendAttachment(type, content, name, size)
                        },
                        onReply = { msg -> viewModel.setReplyingTo(msg) },
                        onReaction = { msg, emoji -> viewModel.toggleReaction(msg.id, emoji) },
                        onDeleteMessage = { msg -> viewModel.deleteMessage(msg.id) },
                        onEditMessage = { msg -> viewModel.startEditingMessage(msg) },
                        editingMessage = editingMessage,
                        onSubmitEdit = { newText -> viewModel.submitEditMessage(newText) },
                        onCancelEdit = { viewModel.cancelEditMessage() },
                        onStartVoiceCall = {
                            viewModel.startCall(conv.title, conv.avatarUrl, "VOICE")
                        },
                        onStartVideoCall = {
                            viewModel.startCall(conv.title, conv.avatarUrl, "VIDEO")
                        },
                        onBackClick = { viewModel.closeConversation() },
                        typingStatus = typingForConv
                    )
                } else {
                    viewModel.navigateTo(ScreenState.MAIN)
                }
            }

            ScreenState.ACTIVE_CALL -> {
                ActiveCallScreen(
                    contactName = activeCallContact,
                    callType = activeCallType,
                    durationSeconds = callDurationSeconds,
                    isMuted = isCallMuted,
                    isSpeakerOn = isSpeakerOn,
                    isVideoDisabled = isVideoDisabled,
                    onToggleMute = { viewModel.toggleCallMute() },
                    onToggleSpeaker = { viewModel.toggleSpeaker() },
                    onToggleVideo = { viewModel.toggleVideo() },
                    onEndCall = { viewModel.endCall() }
                )
            }

            ScreenState.GLOBAL_SEARCH -> {
                GlobalSearchScreen(
                    searchQuery = searchQuery,
                    onQueryChange = { viewModel.onSearchQueryChange(it) },
                    conversations = conversations,
                    searchedContacts = searchedContacts,
                    searchedMessages = searchedMessages,
                    onSelectConversation = { convId ->
                        viewModel.openConversation(convId)
                    },
                    onSelectContact = { contact ->
                        viewModel.startChatWithContact(contact)
                    },
                    onBackClick = { viewModel.navigateTo(ScreenState.MAIN) }
                )
            }

            else -> {
                MainScaffold(
                    activeTab = activeTab,
                    onTabSelected = { viewModel.setTab(it) },
                    conversations = conversations,
                    contacts = contacts,
                    calls = calls,
                    currentUser = currentUser,
                    deviceSessions = deviceSessions,
                    isOffline = isOffline,
                    readReceipts = readReceipts,
                    typingIndicators = typingIndicators,
                    notifications = notifications,
                    onConversationClick = { id -> viewModel.openConversation(id) },
                    onSearchClick = { viewModel.navigateTo(ScreenState.GLOBAL_SEARCH) },
                    onToggleOffline = { viewModel.toggleOfflineMode() },
                    onTogglePin = { id -> viewModel.togglePinConversation(id) },
                    onToggleMute = { id -> viewModel.toggleMuteConversation(id) },
                    onCreateGroup = { title, desc, members ->
                        viewModel.createGroup(title, desc, members)
                    },
                    onStartDirectChat = { contact ->
                        viewModel.startChatWithContact(contact)
                    },
                    onStartCall = { name, type ->
                        viewModel.startCall(name, "", type)
                    },
                    onAddContact = { username, name, email, bio ->
                        viewModel.addContact(username, name, email, bio)
                    },
                    onBlockContact = { id -> viewModel.blockContact(id) },
                    onUnblockContact = { id -> viewModel.unblockContact(id) },
                    onAcceptRequest = { id -> viewModel.acceptContactRequest(id) },
                    onClearCalls = { viewModel.clearCalls() },
                    onToggleReadReceipts = { viewModel.toggleReadReceipts() },
                    onToggleTypingIndicators = { viewModel.toggleTypingIndicators() },
                    onToggleNotifications = { viewModel.toggleNotifications() },
                    onOpenQRScanner = { viewModel.setShowQRScanner(true) },
                    onRevokeSession = { id -> viewModel.revokeSession(id) },
                    onUpdateProfile = { name, username, bio, status ->
                        viewModel.updateProfile(name, username, bio, status)
                    },
                    onLogout = { viewModel.logout() },
                    onDeleteAccount = { viewModel.deleteAccount() }
                )
            }
        }

        // QR Code Scanner Overlay for pairing desktop/web
        if (showQRScanner) {
            QRScannerOverlay(
                onDismiss = { viewModel.setShowQRScanner(false) },
                onDevicePaired = { deviceName, browser, location ->
                    viewModel.linkNewDeviceSession(deviceName, browser, location)
                }
            )
        }

        // Pairing Success confirmation dialog
        if (pairedSuccessEvent != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearPairedSuccessEvent() },
                title = { Text("Device Linked Successfully", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold) },
                text = {
                    Text(pairedSuccessEvent ?: "Your LeeTalk desktop client is now connected and synced.")
                },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearPairedSuccessEvent() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.LeeBlue)
                    ) {
                        Text("Done")
                    }
                }
            )
        }
    }
}
