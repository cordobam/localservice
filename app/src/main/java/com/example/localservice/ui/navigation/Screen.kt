package com.example.localservice.ui.navigation

// Todas las rutas de la app en un solo lugar.
// Si necesitás agregar una pantalla nueva, la sumás acá.
sealed class Screen(val route: String) {

    // Auth
    object Login      : Screen("login")
    object Register   : Screen("register")
    object RolePicker : Screen("role_picker")

    // Cliente (ServiLocal)
    object ClientHome       : Screen("client_home")
    object Search           : Screen("search")
    object ProviderDetail   : Screen("provider_detail/{providerId}") {
        fun createRoute(providerId: String) = "provider_detail/$providerId"
    }
    object Booking          : Screen("booking/{providerId}") {
        fun createRoute(providerId: String) = "booking/$providerId"
    }
    object MyBookings       : Screen("my_bookings")
    object Tracking         : Screen("tracking/{projectId}") {
        fun createRoute(projectId: String) = "tracking/$projectId"
    }

    // Prestador (ServiLocal Pro)
    object ProviderHome         : Screen("provider_home")
    object Dashboard            : Screen("dashboard")
    object IncomingRequests     : Screen("incoming_requests")
    object ProjectDetail        : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    object StageEditor          : Screen("stage_editor/{projectId}") {
        fun createRoute(projectId: String) = "stage_editor/$projectId"
    }
    object BudgetBuilder        : Screen("budget_builder/{bookingId}") {
        fun createRoute(bookingId: String) = "budget_builder/$bookingId"
    }
    object MyProfile            : Screen("my_profile")
    object Agenda               : Screen("agenda")
    object Earnings             : Screen("earnings")
}
