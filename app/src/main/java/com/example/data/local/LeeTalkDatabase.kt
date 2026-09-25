package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.CallRecord
import com.example.data.model.Contact
import com.example.data.model.Conversation
import com.example.data.model.Message
import com.example.data.model.User

@Database(
    entities = [
        User::class,
        Conversation::class,
        Message::class,
        Contact::class,
        CallRecord::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LeeTalkDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun conversationDao(): ConversationDao
    abstract fun messageDao(): MessageDao
    abstract fun contactDao(): ContactDao
    abstract fun callDao(): CallDao

    companion object {
        @Volatile
        private var INSTANCE: LeeTalkDatabase? = null

        fun getDatabase(context: Context): LeeTalkDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LeeTalkDatabase::class.java,
                    "leetalk_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
