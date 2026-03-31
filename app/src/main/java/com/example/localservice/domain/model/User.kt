package com.example.localservice.domain.model

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val phone: String = "",
    val role: UserRole = UserRole.UNKNOWN,
    val photoUrl: String = "",
    val createdAt: Long = 0L
)
