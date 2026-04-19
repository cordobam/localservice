package com.example.localservice.domain.model

data class ChatMessage(
    val id: String = "",
    val bookingId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val createdAt: Long = 0L
) {
    fun isSentBy(uid: String) = senderUid == uid
}
