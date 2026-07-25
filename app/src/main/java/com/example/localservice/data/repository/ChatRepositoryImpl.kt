package com.example.localservice.data.repository

import com.example.localservice.data.local.dao.ChatMessageDao
import com.example.localservice.data.local.entity.ChatMessageEntity
import com.example.localservice.data.remote.firebase.ChatFirestoreSource
import com.example.localservice.domain.model.ChatMessage
import com.example.localservice.domain.repository.ChatRepository
import com.example.localservice.util.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepositoryImpl @Inject constructor(
    private val source: ChatFirestoreSource,
    private val chatMessageDao: ChatMessageDao
) : ChatRepository {

    override fun getMessages(bookingId: String): Flow<Result<List<ChatMessage>>> = flow {
        val roomFlow = chatMessageDao.getMessages(bookingId).map { entities ->
            Result.Success(entities.map { it.toDomain() }) as Result<List<ChatMessage>>
        }
        coroutineScope {
            launch {
                source.getMessages(bookingId).collect { result ->
                    if (result is Result.Success) {
                        withContext(Dispatchers.IO) {
                            chatMessageDao.upsertAll(result.data.map { it.toEntity() })
                        }
                    }
                }
            }
            emitAll(roomFlow)
        }
    }

    override suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        val result = source.sendMessage(message)
        if (result is Result.Success) {
            withContext(Dispatchers.IO) { chatMessageDao.upsert(message.toEntity()) }
        }
        return result
    }

    private fun ChatMessage.toEntity() = ChatMessageEntity(
        id = id,
        bookingId = bookingId,
        senderUid = senderUid,
        senderName = senderName,
        text = text,
        createdAt = createdAt
    )

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        bookingId = bookingId,
        senderUid = senderUid,
        senderName = senderName,
        text = text,
        createdAt = createdAt
    )
}
