package com.example.localservice.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.localservice.data.local.entity.ChatMessageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(messages: List<ChatMessageEntity>)

    @Query("SELECT * FROM chat_messages WHERE bookingId = :bookingId ORDER BY createdAt ASC")
    fun getMessages(bookingId: String): Flow<List<ChatMessageEntity>>

    @Query("DELETE FROM chat_messages WHERE id = :messageId")
    suspend fun deleteById(messageId: String)
}
