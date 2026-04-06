package com.example.localservice.domain.model

data class Provider(
    val uid: String = "",
    val name: String = "",
    val photoUrl: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val description: String = "",
    val zone: String = "",           // barrio / zona: "Nueva Córdoba", "Palermo", etc.
    val city: String = "",           // ciudad: "Córdoba", "Buenos Aires", etc.
    val lat: Double = 0.0,           // coordenadas para el mapa
    val lng: Double = 0.0,
    val rating: Float = 0f,
    val reviewCount: Int = 0,
    val priceFrom: Int = 0,          // precio orientativo desde
    val isAvailable: Boolean = true,
    val createdAt: Long = 0L
)
