package com.example.data.repository

import com.example.data.local.LeeTalkDatabase
import com.example.data.model.CallRecord
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.DeviceSession
import com.example.data.model.Message
import com.example.data.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.UUID

class LeeTalkRepository(
    private val db: LeeTalkDatabase,
    private val scope: CoroutineScope
) {
    private val userDao = db.userDao()
    private val conversationDao = db.conversationDao()
    private val messageDao = db.messageDao()
    private val contactDao = db.contactDao()
    private val callDao = db.callDao()

    // Real-time network and typing presence simulation
    private val _isOffline = MutableStateFlow(false)
    val isOffline: StateFlow<Boolean> = _isOffline.asStateFlow()

    private val _typingStatus = MutableStateFlow<Map<String, String>>(emptyMap()) // conversationId -> "Mary is typing..."
    val typingStatus: StateFlow<Map<String, String>> = _typingStatus.asStateFlow()

    private val _deviceSessions = MutableStateFlow(
        listOf(
            DeviceSession(
                id = "sess-1",
                deviceName = "Android Mobile (This Device)",
                browser = "LeeTalk Android Native / PWA",
                location = "Current Location",
                lastActive = "Active now",
                isCurrent = true
            ),
            DeviceSession(
                id = "sess-2",
                deviceName = "Chrome — Windows 11",
                browser = "Chrome 122.0",
                location = "Nairobi, Kenya",
                lastActive = "2 hours ago",
                isCurrent = false
            ),
            DeviceSession(
                id = "sess-3",
                deviceName = "MacBook Pro — Safari",
                browser = "Safari 17.3",
                location = "London, UK",
                lastActive = "3 days ago",
                isCurrent = false
            )
        )
    )
    val deviceSessions: StateFlow<List<DeviceSession>> = _deviceSessions.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            seedInitialDataIfNeeded()
        }
    }

    // Flows
    val currentUser: Flow<User?> = userDao.getCurrentUser()
    val allConversations: Flow<List<Conversation>> = conversationDao.getAllConversations()
    val allContacts: Flow<List<Contact>> = contactDao.getAllContacts()
    val allCalls: Flow<List<CallRecord>> = callDao.getAllCalls()

    fun getMessagesForConversation(conversationId: String): Flow<List<Message>> {
        return messageDao.getMessagesForConversation(conversationId)
    }

    fun getConversationById(id: String): Flow<Conversation?> {
        return conversationDao.getConversationById(id)
    }

    fun searchContacts(query: String): Flow<List<Contact>> = contactDao.searchContacts(query)
    fun searchMessages(query: String): Flow<List<Message>> = messageDao.searchMessages(query)

    suspend fun markConversationAsRead(id: String) {
        conversationDao.markAsRead(id)
    }

    // Toggle simulated offline mode
    fun setOffline(offline: Boolean) {
        _isOffline.value = offline
        if (!offline) {
            // Reconnected! Flush pending offline messages
            scope.launch(Dispatchers.IO) {
                val pending = messageDao.getPendingMessages()
                for (msg in pending) {
                    messageDao.updateMessageStatus(msg.id, "SENT")
                    delay(300)
                    messageDao.updateMessageStatus(msg.id, "DELIVERED")
                    delay(500)
                    messageDao.updateMessageStatus(msg.id, "READ")
                }
            }
        }
    }

    // Send Message pipeline (with optimistic update, status lifecycles, and auto-reply)
    suspend fun sendMessage(
        conversationId: String,
        type: String = "TEXT",
        content: String = "",
        mediaUrl: String? = null,
        mediaName: String? = null,
        mediaSize: String? = null,
        mediaDuration: String? = null,
        replyToId: String? = null,
        replySenderName: String? = null,
        replySnippet: String? = null
    ): Message {
        val user = currentUser.firstOrNull()
        val senderName = user?.displayName ?: "Gabriel"
        val senderId = user?.userId ?: "user-gabriel"

        val messageId = "msg-${UUID.randomUUID()}"
        val initialStatus = if (_isOffline.value) "SENDING" else "SENT"

        val snippet = when (type) {
            "IMAGE" -> "📷 Photo: $content"
            "VOICE", "AUDIO" -> "🎙️ Voice message (${mediaDuration ?: "0:12"})"
            "DOCUMENT" -> "📄 ${mediaName ?: "Document"}"
            "LOCATION" -> "📍 Location: $content"
            "CONTACT" -> "👤 Contact: $content"
            "VIDEO" -> "🎥 Video: $content"
            else -> content
        }

        val message = Message(
            id = messageId,
            conversationId = conversationId,
            senderId = senderId,
            senderName = senderName,
            type = type,
            content = content,
            mediaUrl = mediaUrl,
            mediaName = mediaName,
            mediaSize = mediaSize,
            mediaDuration = mediaDuration,
            replyToId = replyToId,
            replySenderName = replySenderName,
            replySnippet = replySnippet,
            status = initialStatus,
            clientMessageId = UUID.randomUUID().toString(),
            createdAt = System.currentTimeMillis()
        )

        messageDao.insertMessage(message)
        conversationDao.updateLastMessage(conversationId, snippet, message.createdAt)

        // Real-time delivery simulation if online
        if (!_isOffline.value) {
            scope.launch(Dispatchers.IO) {
                delay(600)
                messageDao.updateMessageStatus(messageId, "DELIVERED")
                delay(800)
                messageDao.updateMessageStatus(messageId, "READ")

                // Auto reply from contacts in 1-on-1 chats for vibrant interactive experience
                handleAutoReply(conversationId, content)
            }
        }

        return message
    }

    private suspend fun handleAutoReply(conversationId: String, userMessage: String) {
        val conversation = conversationDao.getConversationById(conversationId).firstOrNull() ?: return
        if (conversation.type == "GROUP") return

        // Set typing indicator
        val currentTyping = _typingStatus.value.toMutableMap()
        currentTyping[conversationId] = "${conversation.title} is typing..."
        _typingStatus.value = currentTyping

        delay(1800)

        // Clear typing indicator
        val clearedTyping = _typingStatus.value.toMutableMap()
        clearedTyping.remove(conversationId)
        _typingStatus.value = clearedTyping

        val replies = when {
            userMessage.contains("hello", ignoreCase = true) || userMessage.contains("hi", ignoreCase = true) ->
                listOf("Hey there! Good to hear from you 😊", "Hello! How is your day going?")
            userMessage.contains("meeting", ignoreCase = true) || userMessage.contains("time", ignoreCase = true) ->
                listOf("Sounds good! Looking forward to it.", "I've added it to my calendar.")
            userMessage.contains("document", ignoreCase = true) || userMessage.contains("file", ignoreCase = true) ->
                listOf("Got it! Reviewing the files now.", "Thanks for sending this over!")
            else -> listOf(
                "Got your message! Everything looks great.",
                "Understood! Let's talk more soon 👍",
                "Awesome, thanks for the update!",
                "Great! LeeTalk is super fast and smooth."
            )
        }
        val replyContent = replies.random()
        val replyMsgId = "msg-${UUID.randomUUID()}"
        val replyMsg = Message(
            id = replyMsgId,
            conversationId = conversationId,
            senderId = conversation.recipientUserId ?: "contact-other",
            senderName = conversation.title,
            type = "TEXT",
            content = replyContent,
            status = "READ",
            createdAt = System.currentTimeMillis()
        )
        messageDao.insertMessage(replyMsg)
        conversationDao.updateLastMessage(conversationId, replyContent, replyMsg.createdAt)
    }

    // Toggle reaction
    suspend fun toggleReaction(messageId: String, emoji: String) {
        val message = messageDao.getMessageById(messageId) ?: return
        val currentReactions = message.reactions
        val updatedReactions = if (currentReactions.contains(emoji)) {
            // Remove
            currentReactions.split(",")
                .filter { !it.startsWith(emoji) }
                .joinToString(",")
        } else {
            // Add
            if (currentReactions.isEmpty()) "$emoji:1" else "$currentReactions,$emoji:1"
        }
        messageDao.updateMessage(message.copy(reactions = updatedReactions))
    }

    // Soft delete message
    suspend fun deleteMessage(messageId: String) {
        messageDao.softDeleteMessage(messageId, System.currentTimeMillis())
    }

    // Edit message
    suspend fun editMessage(messageId: String, newContent: String) {
        val message = messageDao.getMessageById(messageId) ?: return
        messageDao.updateMessage(message.copy(content = newContent, editedAt = System.currentTimeMillis()))
    }

    // Toggle pin conversation
    suspend fun togglePinConversation(conversationId: String) {
        val conversation = conversationDao.getConversationById(conversationId).firstOrNull() ?: return
        conversationDao.updateConversation(conversation.copy(isPinned = !conversation.isPinned))
    }

    // Toggle mute conversation
    suspend fun toggleMuteConversation(conversationId: String) {
        val conversation = conversationDao.getConversationById(conversationId).firstOrNull() ?: return
        conversationDao.updateConversation(conversation.copy(isMuted = !conversation.isMuted))
    }

    // Create Direct Conversation
    suspend fun getOrCreateDirectConversation(contact: Contact): String {
        val existing = conversationDao.getAllConversations().firstOrNull()?.find {
            it.type == "DIRECT" && it.recipientUserId == contact.id
        }
        if (existing != null) return existing.id

        val newId = "conv-${UUID.randomUUID()}"
        val newConv = Conversation(
            id = newId,
            title = contact.displayName,
            type = "DIRECT",
            avatarUrl = contact.avatarUrl,
            lastMessageSnippet = "Conversation started",
            lastMessageTime = System.currentTimeMillis(),
            recipientUserId = contact.id,
            memberIds = contact.id
        )
        conversationDao.insertConversation(newConv)
        return newId
    }

    // Create Group Conversation
    suspend fun createGroupConversation(title: String, description: String, memberIds: List<String>): String {
        val newId = "group-${UUID.randomUUID()}"
        val newConv = Conversation(
            id = newId,
            title = title,
            type = "GROUP",
            groupDescription = description,
            lastMessageSnippet = "Group created: $title",
            lastMessageTime = System.currentTimeMillis(),
            memberIds = memberIds.joinToString(",")
        )
        conversationDao.insertConversation(newConv)
        return newId
    }

    // Contacts management
    suspend fun addContact(username: String, displayName: String, email: String, bio: String): Contact {
        val id = "contact-${UUID.randomUUID()}"
        val cleanUsername = if (username.startsWith("@")) username else "@$username"
        val contact = Contact(
            id = id,
            displayName = displayName,
            username = cleanUsername,
            email = email,
            bio = bio,
            relationship = "CONTACT",
            isOnline = true
        )
        contactDao.insertContact(contact)
        return contact
    }

    suspend fun updateContactRelationship(contactId: String, newRelationship: String) {
        val contacts = contactDao.getAllContacts().firstOrNull() ?: return
        val contact = contacts.find { it.id == contactId } ?: return
        contactDao.updateContact(contact.copy(relationship = newRelationship))
    }

    // Call logging
    suspend fun logCall(
        contactId: String,
        contactName: String,
        contactAvatar: String,
        callType: String,
        direction: String,
        durationSeconds: Int
    ) {
        val call = CallRecord(
            id = "call-${UUID.randomUUID()}",
            contactId = contactId,
            contactName = contactName,
            contactAvatar = contactAvatar,
            callType = callType,
            direction = direction,
            durationSeconds = durationSeconds,
            timestamp = System.currentTimeMillis()
        )
        callDao.insertCall(call)
    }

    suspend fun clearCalls() {
        callDao.clearCallHistory()
    }

    // Sessions management
    fun revokeSession(sessionId: String) {
        _deviceSessions.value = _deviceSessions.value.filter { it.id != sessionId }
    }

    // Passwordless authentication simulation
    suspend fun loginWithMagicLink(email: String, displayName: String, username: String): User {
        val cleanUsername = if (username.startsWith("@")) username else "@$username"
        val user = User(
            userId = "user-${UUID.randomUUID()}",
            displayName = displayName.ifBlank { "Gabriel Mugi" },
            username = cleanUsername.ifBlank { "@gabriel" },
            email = email,
            bio = "Talk. Share. Connect.",
            isCurrentUser = true,
            isOnline = true
        )
        userDao.clearCurrentUser()
        userDao.insertUser(user)
        return user
    }

    suspend fun updateProfile(displayName: String, username: String, bio: String, status: String) {
        val user = currentUser.firstOrNull() ?: return
        val cleanUsername = if (username.startsWith("@")) username else "@$username"
        userDao.updateUser(
            user.copy(
                displayName = displayName,
                username = cleanUsername,
                bio = bio,
                status = status
            )
        )
    }

    suspend fun logout() {
        userDao.clearCurrentUser()
    }

    // Initial seed data
    private suspend fun seedInitialDataIfNeeded() {
        val existingUser = userDao.getCurrentUser().firstOrNull()
        if (existingUser == null) {
            val defaultUser = User(
                userId = "user-gabriel",
                displayName = "Gabriel Mugi",
                username = "@gabriel",
                email = "gabriel.mugi66@gmail.com",
                bio = "Talk. Share. Connect. | Software Architect",
                status = "Available",
                isCurrentUser = true,
                isOnline = true
            )
            userDao.insertUser(defaultUser)
        }

        val existingContacts = contactDao.getAllContacts().firstOrNull()
        if (existingContacts.isNullOrEmpty()) {
            val contacts = listOf(
                Contact(
                    id = "contact-mary",
                    displayName = "Mary Wanjiku",
                    username = "@mary",
                    email = "mary@example.com",
                    bio = "Product Designer & Innovator",
                    relationship = "CONTACT",
                    isOnline = true
                ),
                Contact(
                    id = "contact-john",
                    displayName = "John Kamau",
                    username = "@john254",
                    email = "john@example.com",
                    bio = "Backend Architect | Coffee lover ☕",
                    relationship = "CONTACT",
                    isOnline = false,
                    lastSeen = System.currentTimeMillis() - 7200000
                ),
                Contact(
                    id = "contact-sarah",
                    displayName = "Sarah Jenkins",
                    username = "@sarah_j",
                    email = "sarah@example.com",
                    bio = "Living life to the fullest 🌟",
                    relationship = "CONTACT",
                    isOnline = true
                ),
                Contact(
                    id = "contact-peter",
                    displayName = "Peter Mwangi",
                    username = "@peter_m",
                    email = "peter@example.com",
                    bio = "Mobile Developer & Tech Enthusiast",
                    relationship = "CONTACT",
                    isOnline = true
                ),
                Contact(
                    id = "contact-discover-1",
                    displayName = "Alex O'Connor",
                    username = "@alex_o",
                    email = "alex@example.com",
                    bio = "Security Researcher",
                    relationship = "REQUEST_RECEIVED",
                    isOnline = false
                )
            )
            contactDao.insertContacts(contacts)
        }

        val existingConvs = conversationDao.getAllConversations().firstOrNull()
        if (existingConvs.isNullOrEmpty()) {
            val convMary = Conversation(
                id = "conv-mary",
                title = "Mary Wanjiku",
                type = "DIRECT",
                lastMessageSnippet = "Perfect, see you there! 👍",
                lastMessageTime = System.currentTimeMillis() - 120000,
                unreadCount = 0,
                isPinned = true,
                recipientUserId = "contact-mary",
                memberIds = "contact-mary"
            )

            val convJohn = Conversation(
                id = "conv-john",
                title = "John Kamau",
                type = "DIRECT",
                lastMessageSnippet = "📄 LeeTalk_Architecture_v1.pdf",
                lastMessageTime = System.currentTimeMillis() - 3600000,
                unreadCount = 1,
                recipientUserId = "contact-john",
                memberIds = "contact-john"
            )

            val convTeam = Conversation(
                id = "conv-team",
                title = "Project Team",
                type = "GROUP",
                groupDescription = "Core product engineering & sprint syncs",
                lastMessageSnippet = "Gabriel: Meeting starts at 2pm",
                lastMessageTime = System.currentTimeMillis() - 1800000,
                unreadCount = 2,
                memberIds = "user-gabriel,contact-mary,contact-john,contact-peter"
            )

            conversationDao.insertConversations(listOf(convMary, convJohn, convTeam))

            // Seed messages for Mary
            val now = System.currentTimeMillis()
            val msgsMary = listOf(
                Message(
                    id = "msg-m1",
                    conversationId = "conv-mary",
                    senderId = "user-gabriel",
                    senderName = "Gabriel Mugi",
                    type = "TEXT",
                    content = "Hi Mary 👋",
                    status = "READ",
                    createdAt = now - 600000
                ),
                Message(
                    id = "msg-m2",
                    conversationId = "conv-mary",
                    senderId = "contact-mary",
                    senderName = "Mary Wanjiku",
                    type = "TEXT",
                    content = "Hey Gabriel! How is the new build going?",
                    status = "READ",
                    createdAt = now - 540000
                ),
                Message(
                    id = "msg-m3",
                    conversationId = "conv-mary",
                    senderId = "user-gabriel",
                    senderName = "Gabriel Mugi",
                    type = "TEXT",
                    content = "It's running super smoothly! Check out this voice preview:",
                    status = "READ",
                    createdAt = now - 480000
                ),
                Message(
                    id = "msg-m4",
                    conversationId = "conv-mary",
                    senderId = "contact-mary",
                    senderName = "Mary Wanjiku",
                    type = "VOICE",
                    content = "Voice note update",
                    mediaDuration = "0:18",
                    reactions = "❤️:1",
                    status = "READ",
                    createdAt = now - 360000
                ),
                Message(
                    id = "msg-m5",
                    conversationId = "conv-mary",
                    senderId = "user-gabriel",
                    senderName = "Gabriel Mugi",
                    type = "LOCATION",
                    content = "Nairobi Tech Hub, Floor 4",
                    mediaName = "-1.286389, 36.817223",
                    status = "READ",
                    createdAt = now - 240000
                ),
                Message(
                    id = "msg-m6",
                    conversationId = "conv-mary",
                    senderId = "contact-mary",
                    senderName = "Mary Wanjiku",
                    type = "TEXT",
                    content = "Perfect, see you there! 👍",
                    reactions = "👍:2",
                    status = "READ",
                    createdAt = now - 120000
                )
            )
            messageDao.insertMessages(msgsMary)

            // Seed messages for John
            val msgsJohn = listOf(
                Message(
                    id = "msg-j1",
                    conversationId = "conv-john",
                    senderId = "contact-john",
                    senderName = "John Kamau",
                    type = "TEXT",
                    content = "Hey Gabriel, here is the architecture specification we discussed:",
                    status = "READ",
                    createdAt = now - 7200000
                ),
                Message(
                    id = "msg-j2",
                    conversationId = "conv-john",
                    senderId = "contact-john",
                    senderName = "John Kamau",
                    type = "DOCUMENT",
                    content = "SRS Technical Specification v1.0",
                    mediaName = "LeeTalk_Architecture_v1.pdf",
                    mediaSize = "2.4 MB",
                    status = "READ",
                    createdAt = now - 7100000
                )
            )
            messageDao.insertMessages(msgsJohn)

            // Seed messages for Project Team
            val msgsTeam = listOf(
                Message(
                    id = "msg-t1",
                    conversationId = "conv-team",
                    senderId = "contact-mary",
                    senderName = "Mary Wanjiku",
                    type = "TEXT",
                    content = "Hey team, sprint 1 is looking fantastic!",
                    status = "READ",
                    createdAt = now - 3600000
                ),
                Message(
                    id = "msg-t2",
                    conversationId = "conv-team",
                    senderId = "contact-john",
                    senderName = "John Kamau",
                    type = "TEXT",
                    content = "All REST endpoints and WebSocket protocols tested.",
                    status = "READ",
                    createdAt = now - 2700000
                ),
                Message(
                    id = "msg-t3",
                    conversationId = "conv-team",
                    senderId = "user-gabriel",
                    senderName = "Gabriel Mugi",
                    type = "TEXT",
                    content = "Meeting starts at 2pm",
                    reactions = "🙌:3",
                    status = "READ",
                    createdAt = now - 1800000
                )
            )
            messageDao.insertMessages(msgsTeam)

            // Seed calls
            val sampleCalls = listOf(
                CallRecord(
                    id = "call-1",
                    contactId = "contact-mary",
                    contactName = "Mary Wanjiku",
                    callType = "VIDEO",
                    direction = "INCOMING",
                    durationSeconds = 860,
                    timestamp = now - 10800000
                ),
                CallRecord(
                    id = "call-2",
                    contactId = "contact-john",
                    contactName = "John Kamau",
                    callType = "VOICE",
                    direction = "MISSED",
                    durationSeconds = 0,
                    timestamp = now - 18000000
                ),
                CallRecord(
                    id = "call-3",
                    contactId = "contact-peter",
                    contactName = "Peter Mwangi",
                    callType = "VOICE",
                    direction = "OUTGOING",
                    durationSeconds = 312,
                    timestamp = now - 86400000
                )
            )
            for (c in sampleCalls) callDao.insertCall(c)
        }
    }
}
