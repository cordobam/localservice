package com.example.localservice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val id: String,
    val providerUid: String,
    val clientUid: String,
    val clientName: String,
    val clientPhotoUrl: String,
    val rating: Float,
    val comment: String,
    val bookingId: String,
    val createdAt: Long
)
