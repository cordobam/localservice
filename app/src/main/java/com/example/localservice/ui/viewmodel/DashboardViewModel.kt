package com.example.localservice.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.Review
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val providerUid: String = "",
    val providerName: String = "",

    // Pedidos pendientes de respuesta
    val pendingBookings: List<Booking> = emptyList(),

    // Trabajos en curso
    val activeBookings: List<Booking> = emptyList(),

    // Reseñas
    val reviews: List<Review> = emptyList(),
    val averageRating: Float = 0f,

    // Agenda — mapa fecha → lista de bookings ese día
    val agendaByDate: Map<LocalDate, List<Booking>> = emptyMap(),
    val selectedDate: LocalDate = LocalDate.now(),

    // Feedback de acciones
    val error: String? = null,
    val successMessage: String? = null,
    val isActioning: Boolean = false,   // mientras acepta/rechaza/manda presupuesto

    // Bottom sheet de presupuesto
    val showBudgetSheet: Boolean = false,
    val budgetTargetBookingId: String = "",
    val budgetAmount: String = "",
    val budgetNote: String = ""
) {
    val pendingCount: Int get() = pendingBookings.size
    val activeCount: Int get() = activeBookings.size
    val selectedDateBookings: List<Booking> get() = agendaByDate[selectedDate] ?: emptyList()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()


    fun init(providerUid: String, providerName: String) {
        // SACÁ esta línea:
        // if (_uiState.value.providerUid == providerUid) return

        _uiState.update { it.copy(providerUid = providerUid, providerName = providerName) }
        observeBookings(providerUid)
        observeReviews(providerUid)
    }

    private fun observeBookings(providerUid: String) {
        viewModelScope.launch {
            bookingRepository.getBookingsForProvider(providerUid).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                    is Result.Success -> {
                        val all = result.data

                        // Pedidos que esperan respuesta
                        val pending = all.filter { it.status == BookingStatus.PENDING }

                        // Trabajos activos (presupuesto aprobado o en progreso)
                        val active = all.filter {
                            it.status in listOf(
                                BookingStatus.BUDGET_APPROVED,
                                BookingStatus.IN_PROGRESS,
                                BookingStatus.BUDGET_SENT
                            )
                        }

                        // Agenda: agrupa bookings activos por fecha de creación
                        // En Fase 4 esto usará una fecha de trabajo asignada
                        val agendaByDate = active.groupBy { booking ->
                            Instant.ofEpochMilli(booking.createdAt)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }

                        _uiState.update {
                            it.copy(
                                isLoading     = false,
                                pendingBookings = pending,
                                activeBookings  = active,
                                agendaByDate    = agendaByDate
                            )
                        }
                    }
                }
            }
        }
    }

    private fun observeReviews(providerUid: String) {
        viewModelScope.launch {
            reviewRepository.getReviewsForProvider(providerUid).collect { result ->
                if (result is Result.Success) {
                    val reviews = result.data
                    val avg = if (reviews.isEmpty()) 0f
                              else reviews.map { it.rating }.average().toFloat()
                    _uiState.update { it.copy(reviews = reviews, averageRating = avg) }
                }
            }
        }
    }

    // --- Acciones sobre pedidos ---

    // Acepta el pedido directo (sin presupuesto)
    fun acceptBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.IN_PROGRESS, "Pedido aceptado")
    }

    // Rechaza el pedido
    fun rejectBooking(bookingId: String) {
        updateStatus(bookingId, BookingStatus.CANCELLED, "Pedido rechazado")
    }

    // Abre el sheet para armar el presupuesto
    fun openBudgetSheet(bookingId: String) {
        _uiState.update {
            it.copy(
                showBudgetSheet       = true,
                budgetTargetBookingId = bookingId,
                budgetAmount          = "",
                budgetNote            = ""
            )
        }
    }

    fun closeBudgetSheet() {
        _uiState.update { it.copy(showBudgetSheet = false) }
    }

    fun onBudgetAmountChanged(value: String) {
        // Solo números
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(budgetAmount = value) }
        }
    }

    fun onBudgetNoteChanged(value: String) {
        _uiState.update { it.copy(budgetNote = value) }
    }

    fun sendBudget() {
        val bookingId = _uiState.value.budgetTargetBookingId
        val amount = _uiState.value.budgetAmount.toIntOrNull() ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true) }
            when (val result = bookingRepository.updateBookingStatus(bookingId, BookingStatus.BUDGET_SENT)) {
                is Result.Success -> {
                    // También actualizamos el monto — requiere método en repo
                    // Por ahora usamos el cambio de estado como señal
                    _uiState.update {
                        it.copy(
                            isActioning    = false,
                            showBudgetSheet = false,
                            successMessage  = "Presupuesto de $$amount enviado al cliente"
                        )
                    }
                }
                is Result.Error -> _uiState.update {
                    it.copy(isActioning = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    // --- Agenda ---
    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    // --- Helpers ---
    private fun updateStatus(bookingId: String, status: BookingStatus, successMsg: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true) }
            when (val result = bookingRepository.updateBookingStatus(bookingId, status)) {
                is Result.Success -> _uiState.update {
                    it.copy(isActioning = false, successMessage = successMsg)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isActioning = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(error = null, successMessage = null) }
    }
}
