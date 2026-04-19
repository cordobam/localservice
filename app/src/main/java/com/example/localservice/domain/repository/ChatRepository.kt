package com.example.localservice.domain.repository

import com.example.localservice.domain.model.ChatMessage
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow

interface ChatRepository {
    fun getMessages(bookingId: String): Flow<Result<List<ChatMessage>>>
    suspend fun sendMessage(message: ChatMessage): Result<Unit>
}
