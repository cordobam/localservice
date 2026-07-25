package com.example.localservice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "providers")
data class ProviderEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val photoUrl: String,
    val category: String,
    val description: String,
    val zone: String,
    val city: String,
    val lat: Double,
    val lng: Double,
    val rating: Float,
    val reviewCount: Int,
    val priceFrom: Int,
    val isAvailable: Boolean,
    val createdAt: Long,
    val mpAlias: String
)
