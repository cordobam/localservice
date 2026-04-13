package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MyBookingsUiState(
    val isLoading: Boolean = true,
    val activeBookings: List<Booking> = emptyList(),    // pendientes + en curso
    val pastBookings: List<Booking> = emptyList(),      // completados + cancelados
    val error: String? = null,
    // Para aprobar presupuesto
    val isActioning: Boolean = false,
    val successMessage: String? = null
)

@HiltViewModel
class MyBookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MyBookingsUiState())
    val uiState: StateFlow<MyBookingsUiState> = _uiState.asStateFlow()

    fun init(clientUid: String) {
        viewModelScope.launch {
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
                        _uiState.update {
                            it.copy(
                                isLoading      = false,
                                activeBookings = active,
                                pastBookings   = past
                            )
                        }
                    }
                }
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
