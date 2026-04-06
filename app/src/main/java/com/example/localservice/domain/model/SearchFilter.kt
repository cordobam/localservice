package com.example.localservice.domain.model

data class SearchFilter(
    val category: ServiceCategory? = null,  // null = todos los rubros
    val zone: String? = null,               // null = todas las zonas
    val userLat: Double? = null,            // ubicación del usuario para distancia
    val userLng: Double? = null,
    val radiusKm: Double = 10.0             // radio de búsqueda en km
) {
    val hasActiveFilters: Boolean
        get() = category != null || zone != null
}
