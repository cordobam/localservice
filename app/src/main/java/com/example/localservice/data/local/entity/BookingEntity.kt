package com.example.localservice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookings")
data class BookingEntity(
    @PrimaryKey val id: String,
    val providerUid: String,
    val providerName: String,
    val clientUid: String,
    val clientName: String,
    val clientPhone: String,
    val category: String,
    val description: String,
    val status: String,
    val budgetAmount: Int,
    val budgetApproved: Boolean,
    val budgetNote: String,
    val publicSlug: String,
    val stagesJson: String,
    val createdAt: Long,
    val updatedAt: Long
)
