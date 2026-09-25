package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val userId: String,
    val displayName: String,
    val username: String,
    val email: String,
    val avatarUrl: String = "",
    val bio: String = "Talk. Share. Connect.",
    val status: String = "Available",
    val isCurrentUser: Boolean = false,
    val isOnline: Boolean = true,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class Conversation(
    @PrimaryKey val id: String,
    val title: String,
    val type: String = "DIRECT", // "DIRECT" or "GROUP"
    val avatarUrl: String = "",
    val lastMessageSnippet: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val recipientUserId: String? = null,
    val memberIds: String = "",
    val groupDescription: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val senderName: String,
    val senderAvatar: String = "",
    val type: String = "TEXT", // TEXT, IMAGE, VIDEO, AUDIO, VOICE, DOCUMENT, LOCATION, CONTACT, CALL
    val content: String = "",
    val mediaUrl: String? = null,
    val mediaName: String? = null,
    val mediaSize: String? = null,
    val mediaDuration: String? = null,
    val replyToId: String? = null,
    val replySenderName: String? = null,
    val replySnippet: String? = null,
    val status: String = "SENT", // SENDING, SENT, DELIVERED, READ, FAILED
    val clientMessageId: String = "",
    val reactions: String = "", // formatted like "❤️:1,👍:2"
    val isStarred: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val deletedAt: Long? = null
)

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val id: String,
    val displayName: String,
    val username: String,
    val email: String,
    val avatarUrl: String = "",
    val bio: String = "",
    val relationship: String = "CONTACT", // CONTACT, REQUEST_RECEIVED, REQUEST_SENT, BLOCKED
    val isOnline: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis()
)

@Entity(tableName = "call_records")
data class CallRecord(
    @PrimaryKey val id: String,
    val contactId: String,
    val contactName: String,
    val contactAvatar: String = "",
    val callType: String = "VOICE", // VOICE, VIDEO
    val direction: String = "OUTGOING", // INCOMING, OUTGOING, MISSED
    val durationSeconds: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class DeviceSession(
    val id: String,
    val deviceName: String,
    val browser: String,
    val location: String,
    val lastActive: String,
    val isCurrent: Boolean
)
