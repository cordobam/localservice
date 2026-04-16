package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TrackingUiState(
    val isLoading: Boolean = true,
    val booking: Booking? = null,
    val stages: List<Stage> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TrackingUiState())
    val uiState: StateFlow<TrackingUiState> = _uiState.asStateFlow()

    fun init(slug: String) {
        viewModelScope.launch {
            bookingRepository.getBookingBySlug(slug).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                    is Result.Success -> _uiState.update {
                        it.copy(
                            isLoading = false,
                            booking   = result.data,
                            stages    = result.data.stages
                        )
                    }
                }
            }
        }
    }
}
