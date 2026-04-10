package com.example.localservice.domain.model

data class Review(
    val id: String = "",
    val providerUid: String = "",
    val clientUid: String = "",
    val clientName: String = "",
    val clientPhotoUrl: String = "",
    val rating: Float = 0f,
    val comment: String = "",
    val createdAt: Long = 0L
)
