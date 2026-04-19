package com.example.localservice.data.remote.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.localservice.domain.model.ChatMessage
import com.example.localservice.util.Result
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatFirestoreSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Mensajes van en subcolección: bookings/{bookingId}/messages
    private fun messagesRef(bookingId: String) =
        firestore.collection("bookings").document(bookingId).collection("messages")

    fun getMessages(bookingId: String): Flow<Result<List<ChatMessage>>> = callbackFlow {
        trySend(Result.Loading)
        val listener = messagesRef(bookingId)
            .orderBy("createdAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    trySend(Result.Error(err.message ?: "Error al cargar mensajes"))
                    return@addSnapshotListener
                }
                val messages = snap?.documents?.mapNotNull { doc ->
                    try {
                        ChatMessage(
                            id          = doc.id,
                            bookingId   = bookingId,
                            senderUid   = doc.getString("senderUid") ?: "",
                            senderName  = doc.getString("senderName") ?: "",
                            text        = doc.getString("text") ?: "",
                            createdAt   = doc.getLong("createdAt") ?: 0L
                        )
                    } catch (e: Exception) { null }
                } ?: emptyList()
                trySend(Result.Success(messages))
            }
        awaitClose { listener.remove() }
    }

    suspend fun sendMessage(message: ChatMessage): Result<Unit> {
        return try {
            val id = UUID.randomUUID().toString()
            messagesRef(message.bookingId).document(id).set(
                mapOf(
                    "senderUid"  to message.senderUid,
                    "senderName" to message.senderName,
                    "text"       to message.text,
                    "createdAt"  to System.currentTimeMillis()
                )
            ).await()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(e.message ?: "Error al enviar mensaje", e)
        }
    }
}
