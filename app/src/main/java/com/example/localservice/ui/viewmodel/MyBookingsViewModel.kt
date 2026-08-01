package com.example.localservice.ui.viewmodel

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
import javax.inject.Inject

data class MyBookingsUiState(
    val isLoading: Boolean = true,
    val activeBookings: List<Booking> = emptyList(),
    val pastBookings: List<Booking> = emptyList(),
    val pendingBudgetCount: Int = 0,
    val error: String? = null,
    val isActioning: Boolean = false,
    val successMessage: String? = null,
    val reviewsByBooking: Map<String, Review> = emptyMap()
)

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    fun init(clientUid: String) {
        viewModelScope.launch {
            launch { observeBookings(clientUid) }
            launch { observeClientReviews(clientUid) }
        }
    }

    private suspend fun observeBookings(clientUid: String) {
        bookingRepository.getBookingsForClient(clientUid).collect { result ->
            when (result) {
                is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Success -> {
                    val all = result.data
                    val active = all.filter {
                        it.status in listOf(
                            BookingStatus.PENDING,
                            BookingStatus.BUDGET_SENT,
                            BookingStatus.BUDGET_APPROVED,
                            BookingStatus.IN_PROGRESS
                        )
                    }
                    val past = all.filter {
                        it.status in listOf(
                            BookingStatus.COMPLETED,
                            BookingStatus.CANCELLED
                        )
                    }
                    val pendingBudgetCount = all.count {
                        it.status == BookingStatus.BUDGET_SENT
                    }
                    _uiState.update {
                        it.copy(
                            isLoading          = false,
                            activeBookings     = active,
                            pastBookings       = past,
                            pendingBudgetCount = pendingBudgetCount
                        )
                    }
                }
            }
        }
    }

    private suspend fun observeClientReviews(clientUid: String) {
        reviewRepository.getReviewsByClient(clientUid).collect { result ->
            if (result is Result.Success) {
                val map = result.data
                    .filter { it.bookingId.isNotBlank() }
                    .associateBy { it.bookingId }
                _uiState.update { it.copy(reviewsByBooking = map) }
            }
        }
    }

    // El cliente aprueba el presupuesto enviado por el prestador
    fun approveBudget(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true) }
            when (val result = bookingRepository.updateBookingStatus(
                bookingId, BookingStatus.BUDGET_APPROVED
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(isActioning = false, successMessage = "Presupuesto aprobado")
                }
                is Result.Error -> _uiState.update {
                    it.copy(isActioning = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    // El cliente rechaza el presupuesto
    fun rejectBudget(bookingId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true) }
            when (val result = bookingRepository.updateBookingStatus(
                bookingId, BookingStatus.CANCELLED
            )) {
                is Result.Success -> _uiState.update {
                    it.copy(isActioning = false, successMessage = "Presupuesto rechazado")
                }
                is Result.Error -> _uiState.update {
                    it.copy(isActioning = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(error = null, successMessage = null) }
}
