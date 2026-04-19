package com.example.localservice.data.repository

import com.example.localservice.data.remote.firebase.ChatFirestoreSource
import com.example.localservice.domain.model.ChatMessage
import com.example.localservice.domain.repository.ChatRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val source: ChatFirestoreSource
) : ChatRepository {
    override fun getMessages(bookingId: String) = source.getMessages(bookingId)
    override suspend fun sendMessage(message: ChatMessage) = source.sendMessage(message)
}
