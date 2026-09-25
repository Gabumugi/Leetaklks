package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CallRecord
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>

    @Query("SELECT * FROM users WHERE userId = :id LIMIT 1")
    fun getUserById(id: String): Flow<User?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Update
    suspend fun updateUser(user: User)

    @Query("DELETE FROM users WHERE isCurrentUser = 1")
    suspend fun clearCurrentUser()
}

@Dao
interface ConversationDao {
    @Query("SELECT * FROM conversations ORDER BY isPinned DESC, lastMessageTime DESC")
    fun getAllConversations(): Flow<List<Conversation>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    fun getConversationById(id: String): Flow<Conversation?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conversation: Conversation)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversations(conversations: List<Conversation>)

    @Update
    suspend fun updateConversation(conversation: Conversation)

    @Query("DELETE FROM conversations WHERE id = :id")
    suspend fun deleteConversation(id: String)

    @Query("UPDATE conversations SET lastMessageSnippet = :snippet, lastMessageTime = :time WHERE id = :id")
    suspend fun updateLastMessage(id: String, snippet: String, time: Long)

    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    suspend fun markAsRead(id: String)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE conversationId = :conversationId ORDER BY createdAt ASC")
    fun getMessagesForConversation(conversationId: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE content LIKE '%' || :query || '%' AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun searchMessages(query: String): Flow<List<Message>>

    @Query("SELECT * FROM messages WHERE id = :id LIMIT 1")
    suspend fun getMessageById(id: String): Message?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessages(messages: List<Message>)

    @Update
    suspend fun updateMessage(message: Message)

    @Query("DELETE FROM messages WHERE id = :id")
    suspend fun deleteMessageById(id: String)

    @Query("UPDATE messages SET deletedAt = :deletedAt, content = 'This message was deleted' WHERE id = :id")
    suspend fun softDeleteMessage(id: String, deletedAt: Long)

    @Query("UPDATE messages SET status = :status WHERE id = :id")
    suspend fun updateMessageStatus(id: String, status: String)

    @Query("SELECT * FROM messages WHERE status = 'SENDING'")
    suspend fun getPendingMessages(): List<Message>
}

@Dao
interface ContactDao {
    @Query("SELECT * FROM contacts ORDER BY displayName ASC")
    fun getAllContacts(): Flow<List<Contact>>

    @Query("SELECT * FROM contacts WHERE relationship = :relationship ORDER BY displayName ASC")
    fun getContactsByRelationship(relationship: String): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<Contact>)

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("DELETE FROM contacts WHERE id = :id")
    suspend fun deleteContact(id: String)

    @Query("SELECT * FROM contacts WHERE displayName LIKE '%' || :query || '%' OR username LIKE '%' || :query || '%'")
    fun searchContacts(query: String): Flow<List<Contact>>
}

@Dao
interface CallDao {
    @Query("SELECT * FROM call_records ORDER BY timestamp DESC")
    fun getAllCalls(): Flow<List<CallRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCall(call: CallRecord)

    @Query("DELETE FROM call_records WHERE id = :id")
    suspend fun deleteCall(id: String)

    @Query("DELETE FROM call_records")
    suspend fun clearCallHistory()
}
