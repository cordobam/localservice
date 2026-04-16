package com.example.localservice.domain.model

data class Stage(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val status: StageStatus = StageStatus.PENDING,
    val estimatedDays: Int = 0,
    val order: Int = 0,
    val completedAt: Long? = null
)

enum class StageStatus {
    PENDING, ACTIVE, DONE, LATE
}

// Etapas predefinidas por rubro — el prestador las puede editar
object DefaultStages {
    fun forCategory(category: ServiceCategory): List<Stage> = when (category) {
        ServiceCategory.PLUMBER -> listOf(
            Stage(id = "1", name = "Diagnóstico", order = 1, estimatedDays = 1),
            Stage(id = "2", name = "Compra de materiales", order = 2, estimatedDays = 1),
            Stage(id = "3", name = "Reparación", order = 3, estimatedDays = 1),
            Stage(id = "4", name = "Prueba de funcionamiento", order = 4, estimatedDays = 1),
            Stage(id = "5", name = "Entrega", order = 5, estimatedDays = 1)
        )
        ServiceCategory.ELECTRICIAN -> listOf(
            Stage(id = "1", name = "Revisión eléctrica", order = 1, estimatedDays = 1),
            Stage(id = "2", name = "Compra de materiales", order = 2, estimatedDays = 1),
            Stage(id = "3", name = "Instalación", order = 3, estimatedDays = 2),
            Stage(id = "4", name = "Prueba de seguridad", order = 4, estimatedDays = 1),
            Stage(id = "5", name = "Entrega", order = 5, estimatedDays = 1)
        )
        ServiceCategory.CARPENTER -> listOf(
            Stage(id = "1", name = "Medición y diseño", order = 1, estimatedDays = 2),
            Stage(id = "2", name = "Compra de materiales", order = 2, estimatedDays = 2),
            Stage(id = "3", name = "Fabricación en taller", order = 3, estimatedDays = 7),
            Stage(id = "4", name = "Pintura y terminaciones", order = 4, estimatedDays = 3),
            Stage(id = "5", name = "Entrega e instalación", order = 5, estimatedDays = 1)
        )
        ServiceCategory.PAINTER -> listOf(
            Stage(id = "1", name = "Preparación de superficie", order = 1, estimatedDays = 1),
            Stage(id = "2", name = "Imprimación", order = 2, estimatedDays = 1),
            Stage(id = "3", name = "Primera mano", order = 3, estimatedDays = 1),
            Stage(id = "4", name = "Segunda mano", order = 4, estimatedDays = 1),
            Stage(id = "5", name = "Terminación y limpieza", order = 5, estimatedDays = 1)
        )
        ServiceCategory.MASON -> listOf(
            Stage(id = "1", name = "Relevamiento", order = 1, estimatedDays = 1),
            Stage(id = "2", name = "Demolición / preparación", order = 2, estimatedDays = 2),
            Stage(id = "3", name = "Construcción", order = 3, estimatedDays = 7),
            Stage(id = "4", name = "Revoque y terminaciones", order = 4, estimatedDays = 3),
            Stage(id = "5", name = "Limpieza y entrega", order = 5, estimatedDays = 1)
        )
        else -> listOf(
            Stage(id = "1", name = "Inicio del trabajo", order = 1, estimatedDays = 1),
            Stage(id = "2", name = "En proceso", order = 2, estimatedDays = 3),
            Stage(id = "3", name = "Revisión final", order = 3, estimatedDays = 1),
            Stage(id = "4", name = "Entrega", order = 4, estimatedDays = 1)
        )
    }
}
