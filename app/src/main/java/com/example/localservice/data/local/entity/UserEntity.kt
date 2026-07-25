package com.example.localservice.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val role: String,
    val photoUrl: String,
    val createdAt: Long
)
