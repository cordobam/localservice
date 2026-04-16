package com.example.localservice.ui.navigation

sealed class Screen(val route: String) {

    // Auth
    object Login         : Screen("login")
    object Register      : Screen("register")
    object RolePicker    : Screen("role_picker")
    object ProviderSetup : Screen("provider_setup")  // ← nuevo

    // Cliente (ServiLocal)
    object Search         : Screen("search")
    object ProviderDetail : Screen("provider_detail/{providerId}") {
        fun createRoute(providerId: String) = "provider_detail/$providerId"
    }
    object Booking        : Screen("booking/{providerId}") {
        fun createRoute(providerId: String) = "booking/$providerId"
    }
    object MyBookings     : Screen("my_bookings")
    object Tracking       : Screen("tracking/{slug}") {
        fun createRoute(slug: String) = "tracking/$slug"
    }
    object ClientMain    : Screen("client_main")

    // Prestador (ServiLocal Pro)
    object Dashboard        : Screen("dashboard")
    object IncomingRequests : Screen("incoming_requests")
    object ProjectDetail    : Screen("project_detail/{projectId}") {
        fun createRoute(projectId: String) = "project_detail/$projectId"
    }
    object StageEditor      : Screen("stage_editor/{projectId}") {
        fun createRoute(projectId: String) = "stage_editor/$projectId"
    }
    object BudgetBuilder    : Screen("budget_builder/{bookingId}") {
        fun createRoute(bookingId: String) = "budget_builder/$bookingId"
    }
    object MyProfile        : Screen("my_profile")
    object Agenda           : Screen("agenda")
    object Earnings         : Screen("earnings")

    object ProviderSetupRoute: Screen("provider_setup_route")

}
