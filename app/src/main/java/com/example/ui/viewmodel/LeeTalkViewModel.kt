package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.LeeTalkDatabase
import com.example.data.model.CallRecord
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.DeviceSession
import com.example.data.model.Message
import com.example.data.model.User
import com.example.data.repository.LeeTalkRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MainTab {
    CHATS, CONTACTS, CALLS, ME
}

enum class ScreenState {
    AUTH, MAIN, CHAT, ACTIVE_CALL, GLOBAL_SEARCH, STORAGE_MANAGER, GROUP_INFO
}

class LeeTalkViewModel(application: Application) : AndroidViewModel(application) {
    private val db = LeeTalkDatabase.getDatabase(application)
    private val repository = LeeTalkRepository(db, viewModelScope)

    // Current User & Session
    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val isOffline: StateFlow<Boolean> = repository.isOffline
    val typingStatus: StateFlow<Map<String, String>> = repository.typingStatus
    val deviceSessions: StateFlow<List<DeviceSession>> = repository.deviceSessions

    // Navigation & Tabs
    private val _currentScreen = MutableStateFlow(ScreenState.MAIN)
    val currentScreen: StateFlow<ScreenState> = _currentScreen.asStateFlow()

    private val _activeTab = MutableStateFlow(MainTab.CHATS)
    val activeTab: StateFlow<MainTab> = _activeTab.asStateFlow()

    // Conversations
    val conversations: StateFlow<List<Conversation>> = repository.allConversations
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedConversationId = MutableStateFlow<String?>(null)
    val selectedConversationId: StateFlow<String?> = _selectedConversationId.asStateFlow()

    val currentConversation: StateFlow<Conversation?> = _selectedConversationId.flatMapLatest { id ->
        if (id != null) repository.getConversationById(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMessages: StateFlow<List<Message>> = _selectedConversationId.flatMapLatest { id ->
        if (id != null) repository.getMessagesForConversation(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Contacts & Calls
    val contacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calls: StateFlow<List<CallRecord>> = repository.allCalls
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Composer & Replies
    private val _messageDraft = MutableStateFlow("")
    val messageDraft: StateFlow<String> = _messageDraft.asStateFlow()

    private val _replyingTo = MutableStateFlow<Message?>(null)
    val replyingTo: StateFlow<Message?> = _replyingTo.asStateFlow()

    private val _isRecordingVoice = MutableStateFlow(false)
    val isRecordingVoice: StateFlow<Boolean> = _isRecordingVoice.asStateFlow()

    private val _recordingDuration = MutableStateFlow(0)
    val recordingDuration: StateFlow<Int> = _recordingDuration.asStateFlow()
    private var voiceTimerJob: Job? = null

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchedContacts: StateFlow<List<Contact>> = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList()) else repository.searchContacts(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchedMessages: StateFlow<List<Message>> = _searchQuery.flatMapLatest { q ->
        if (q.isBlank()) flowOf(emptyList()) else repository.searchMessages(q)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Calling State
    private val _activeCallContact = MutableStateFlow("Mary Wanjiku")
    val activeCallContact: StateFlow<String> = _activeCallContact.asStateFlow()

    private val _activeCallType = MutableStateFlow("VOICE")
    val activeCallType: StateFlow<String> = _activeCallType.asStateFlow()

    private val _callDurationSeconds = MutableStateFlow(0)
    val callDurationSeconds: StateFlow<Int> = _callDurationSeconds.asStateFlow()

    private val _isCallMuted = MutableStateFlow(false)
    val isCallMuted: StateFlow<Boolean> = _isCallMuted.asStateFlow()

    private val _isSpeakerOn = MutableStateFlow(false)
    val isSpeakerOn: StateFlow<Boolean> = _isSpeakerOn.asStateFlow()

    private val _isVideoDisabled = MutableStateFlow(false)
    val isVideoDisabled: StateFlow<Boolean> = _isVideoDisabled.asStateFlow()
    private var callTimerJob: Job? = null

    // UI Dialogs
    private val _showNewChatDialog = MutableStateFlow(false)
    val showNewChatDialog: StateFlow<Boolean> = _showNewChatDialog.asStateFlow()

    private val _showNewGroupDialog = MutableStateFlow(false)
    val showNewGroupDialog: StateFlow<Boolean> = _showNewGroupDialog.asStateFlow()

    private val _showAddContactDialog = MutableStateFlow(false)
    val showAddContactDialog: StateFlow<Boolean> = _showAddContactDialog.asStateFlow()

    private val _showAttachmentSheet = MutableStateFlow(false)
    val showAttachmentSheet: StateFlow<Boolean> = _showAttachmentSheet.asStateFlow()

    private val _fullScreenImage = MutableStateFlow<String?>(null)
    val fullScreenImage: StateFlow<String?> = _fullScreenImage.asStateFlow()

    private val _editingMessage = MutableStateFlow<Message?>(null)
    val editingMessage: StateFlow<Message?> = _editingMessage.asStateFlow()

    // Settings
    private val _readReceiptsEnabled = MutableStateFlow(true)
    val readReceiptsEnabled: StateFlow<Boolean> = _readReceiptsEnabled.asStateFlow()

    private val _typingIndicatorsEnabled = MutableStateFlow(true)
    val typingIndicatorsEnabled: StateFlow<Boolean> = _typingIndicatorsEnabled.asStateFlow()

    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled: StateFlow<Boolean> = _notificationsEnabled.asStateFlow()

    private val _themeMode = MutableStateFlow("SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    // Screen navigation
    fun navigateTo(screen: ScreenState) {
        _currentScreen.value = screen
    }

    fun setTab(tab: MainTab) {
        _activeTab.value = tab
    }

    fun openConversation(conversationId: String) {
        _selectedConversationId.value = conversationId
        _currentScreen.value = ScreenState.CHAT
        viewModelScope.launch {
            repository.markConversationAsRead(conversationId)
        }
    }

    fun closeConversation() {
        _selectedConversationId.value = null
        _replyingTo.value = null
        _currentScreen.value = ScreenState.MAIN
    }

    // Composer Actions
    fun onDraftChange(text: String) {
        _messageDraft.value = text
    }

    fun setReplyingTo(message: Message?) {
        _replyingTo.value = message
    }

    fun sendMessage() {
        val text = _messageDraft.value.trim()
        val convId = _selectedConversationId.value ?: return
        if (text.isBlank()) return

        val reply = _replyingTo.value
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                type = "TEXT",
                content = text,
                replyToId = reply?.id,
                replySenderName = reply?.senderName,
                replySnippet = reply?.content
            )
            _messageDraft.value = ""
            _replyingTo.value = null
        }
    }

    fun sendAttachment(type: String, content: String, mediaName: String? = null, mediaSize: String? = null) {
        val convId = _selectedConversationId.value ?: return
        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                type = type,
                content = content,
                mediaName = mediaName,
                mediaSize = mediaSize
            )
            _showAttachmentSheet.value = false
        }
    }

    // Voice recording simulation
    fun startVoiceRecording() {
        _isRecordingVoice.value = true
        _recordingDuration.value = 0
        voiceTimerJob?.cancel()
        voiceTimerJob = viewModelScope.launch {
            while (_isRecordingVoice.value) {
                delay(1000)
                _recordingDuration.value += 1
            }
        }
    }

    fun cancelVoiceRecording() {
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()
        _recordingDuration.value = 0
    }

    fun sendVoiceRecording() {
        val convId = _selectedConversationId.value ?: return
        val durationSec = _recordingDuration.value
        val formatted = String.format("%d:%02d", durationSec / 60, durationSec % 60)
        _isRecordingVoice.value = false
        voiceTimerJob?.cancel()

        viewModelScope.launch {
            repository.sendMessage(
                conversationId = convId,
                type = "VOICE",
                content = "Voice message",
                mediaDuration = formatted
            )
            _recordingDuration.value = 0
        }
    }

    // Message context actions
    fun toggleReaction(messageId: String, emoji: String) {
        viewModelScope.launch {
            repository.toggleReaction(messageId, emoji)
        }
    }

    fun deleteMessage(messageId: String) {
        viewModelScope.launch {
            repository.deleteMessage(messageId)
        }
    }

    fun startEditingMessage(message: Message) {
        _editingMessage.value = message
    }

    fun submitEditMessage(newContent: String) {
        val msg = _editingMessage.value ?: return
        viewModelScope.launch {
            repository.editMessage(msg.id, newContent)
            _editingMessage.value = null
        }
    }

    fun cancelEditMessage() {
        _editingMessage.value = null
    }

    fun togglePinConversation(conversationId: String) {
        viewModelScope.launch {
            repository.togglePinConversation(conversationId)
        }
    }

    fun toggleMuteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.toggleMuteConversation(conversationId)
        }
    }

    // Calls
    fun startCall(contactName: String, contactAvatar: String, callType: String) {
        _activeCallContact.value = contactName
        _activeCallType.value = callType
        _callDurationSeconds.value = 0
        _isCallMuted.value = false
        _isSpeakerOn.value = false
        _isVideoDisabled.value = false
        _currentScreen.value = ScreenState.ACTIVE_CALL

        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_currentScreen.value == ScreenState.ACTIVE_CALL) {
                delay(1000)
                _callDurationSeconds.value += 1
            }
        }
    }

    fun endCall() {
        callTimerJob?.cancel()
        val duration = _callDurationSeconds.value
        val name = _activeCallContact.value
        val type = _activeCallType.value
        viewModelScope.launch {
            repository.logCall(
                contactId = "contact-$name",
                contactName = name,
                contactAvatar = "",
                callType = type,
                direction = "OUTGOING",
                durationSeconds = duration
            )
        }
        _currentScreen.value = if (_selectedConversationId.value != null) ScreenState.CHAT else ScreenState.MAIN
    }

    fun toggleCallMute() {
        _isCallMuted.value = !_isCallMuted.value
    }

    fun toggleSpeaker() {
        _isSpeakerOn.value = !_isSpeakerOn.value
    }

    fun toggleVideo() {
        _isVideoDisabled.value = !_isVideoDisabled.value
    }

    // Dialogs & Sheets
    fun setShowNewChat(show: Boolean) { _showNewChatDialog.value = show }
    fun setShowNewGroup(show: Boolean) { _showNewGroupDialog.value = show }
    fun setShowAddContact(show: Boolean) { _showAddContactDialog.value = show }
    fun setShowAttachmentSheet(show: Boolean) { _showAttachmentSheet.value = show }
    fun setFullScreenImage(url: String?) { _fullScreenImage.value = url }

    // Search
    fun onSearchQueryChange(q: String) {
        _searchQuery.value = q
    }

    // Direct chat from contact
    fun startChatWithContact(contact: Contact) {
        viewModelScope.launch {
            val convId = repository.getOrCreateDirectConversation(contact)
            openConversation(convId)
        }
    }

    // Group creation
    fun createGroup(name: String, desc: String, members: List<String>) {
        viewModelScope.launch {
            val id = repository.createGroupConversation(name, desc, members)
            _showNewGroupDialog.value = false
            openConversation(id)
        }
    }

    // Contact add
    fun addContact(username: String, displayName: String, email: String, bio: String) {
        viewModelScope.launch {
            repository.addContact(username, displayName, email, bio)
            _showAddContactDialog.value = false
        }
    }

    fun blockContact(contactId: String) {
        viewModelScope.launch {
            repository.updateContactRelationship(contactId, "BLOCKED")
        }
    }

    fun unblockContact(contactId: String) {
        viewModelScope.launch {
            repository.updateContactRelationship(contactId, "CONTACT")
        }
    }

    fun acceptContactRequest(contactId: String) {
        viewModelScope.launch {
            repository.updateContactRelationship(contactId, "CONTACT")
        }
    }

    // Sessions
    fun revokeSession(sessionId: String) {
        repository.revokeSession(sessionId)
    }

    // Settings & Privacy
    fun toggleReadReceipts() { _readReceiptsEnabled.value = !_readReceiptsEnabled.value }
    fun toggleTypingIndicators() { _typingIndicatorsEnabled.value = !_typingIndicatorsEnabled.value }
    fun toggleNotifications() { _notificationsEnabled.value = !_notificationsEnabled.value }
    fun setThemeMode(mode: String) { _themeMode.value = mode }

    // Offline simulation mode
    fun toggleOfflineMode() {
        repository.setOffline(!isOffline.value)
    }

    // Auth
    fun login(email: String, displayName: String, username: String) {
        viewModelScope.launch {
            repository.loginWithMagicLink(email, displayName, username)
            _currentScreen.value = ScreenState.MAIN
        }
    }

    fun updateProfile(displayName: String, username: String, bio: String, status: String) {
        viewModelScope.launch {
            repository.updateProfile(displayName, username, bio, status)
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = ScreenState.AUTH
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = ScreenState.AUTH
        }
    }

    fun clearCalls() {
        viewModelScope.launch {
            repository.clearCalls()
        }
    }
}
