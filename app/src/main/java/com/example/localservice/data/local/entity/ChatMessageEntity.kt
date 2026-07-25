package com.example.localservice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey val id: String,
    val bookingId: String,
    val senderUid: String,
    val senderName: String,
    val text: String,
    val createdAt: Long
)
