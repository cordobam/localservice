package com.example.localservice.domain.model

enum class ServiceCategory(val displayName: String, val emoji: String) {
    PLUMBER("Plomero", "🔧"),
    ELECTRICIAN("Electricista", "⚡"),
    CARPENTER("Carpintero", "🪵"),
    PAINTER("Pintor", "🖌️"),
    LOCKSMITH("Cerrajero", "🔑"),
    GAS_TECHNICIAN("Gasista", "🔥"),
    CLEANER("Limpieza", "🧹"),
    GARDENER("Jardinero", "🌿"),
    MASON("Albañil", "🧱"),
    AC_TECHNICIAN("Aire acondicionado", "❄️"),
    MOVER("Mudanza", "📦"),
    OTHER("Otro", "🛠️");

    companion object {
        // Para mostrar en la UI — excluye OTHER del listado principal
        fun mainCategories() = entries.filter { it != OTHER }
    }
}
