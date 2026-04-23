package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProjectDetailUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val stages: List<Stage> = emptyList(),
    val isActioning: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

@HiltViewModel
class ProjectDetailViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProjectDetailUiState())
    val uiState: StateFlow<ProjectDetailUiState> = _uiState.asStateFlow()

    fun init(bookingId: String) {
        viewModelScope.launch {
            when (val result = bookingRepository.getBookingById(bookingId)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isLoading = false,
                        booking   = result.data,
                        stages    = result.data.stages
                    )
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun completeWork() {
        val bookingId = _uiState.value.booking?.id ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isActioning = true) }
            when (val result = bookingRepository.updateBookingStatus(bookingId, BookingStatus.COMPLETED)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isActioning    = false,
                        booking        = it.booking?.copy(status = BookingStatus.COMPLETED),
                        successMessage = "Trabajo marcado como completado"
                    )
                }
                is Result.Error -> _uiState.update { it.copy(isActioning = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(successMessage = null, error = null) }
}
