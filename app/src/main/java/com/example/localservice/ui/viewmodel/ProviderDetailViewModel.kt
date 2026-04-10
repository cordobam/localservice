package com.example.localservice.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.Review
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderDetailUiState(
    val isLoading: Boolean = true,
    val provider: Provider? = null,
    val reviews: List<Review> = emptyList(),
    val error: String? = null,
    // Estado del bottom sheet de solicitud
    val showRequestSheet: Boolean = false,
    val requestDescription: String = "",
    val isSubmitting: Boolean = false,
    val bookingSuccess: Boolean = false,
    val bookingSlug: String = ""   // para mostrar el link de seguimiento
)

@HiltViewModel
class ProviderDetailViewModel @Inject constructor(
    private val providerRepository: ProviderRepository,
    private val reviewRepository: ReviewRepository,
    private val bookingRepository: BookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Recibe el providerId desde la navegación
    private val providerId: String = checkNotNull(savedStateHandle["providerId"])

    private val _uiState = MutableStateFlow(ProviderDetailUiState())
    val uiState: StateFlow<ProviderDetailUiState> = _uiState.asStateFlow()

    init {
        loadProvider()
        loadReviews()
    }

    private fun loadProvider() {
        viewModelScope.launch {
            when (val result = providerRepository.getProviderById(providerId)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, provider = result.data)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    private fun loadReviews() {
        viewModelScope.launch {
            reviewRepository.getReviewsForProvider(providerId).collect { result ->
                if (result is Result.Success) {
                    _uiState.update { it.copy(reviews = result.data) }
                }
            }
        }
    }

    fun onRequestDescriptionChanged(text: String) {
        _uiState.update { it.copy(requestDescription = text) }
    }

    fun showRequestSheet() {
        _uiState.update { it.copy(showRequestSheet = true) }
    }

    fun hideRequestSheet() {
        _uiState.update { it.copy(showRequestSheet = false, requestDescription = "") }
    }

    // Solicitud directa — el cliente describe qué necesita
    fun submitDirectRequest(
        clientUid: String,
        clientName: String,
        clientPhone: String
    ) {
        val provider = _uiState.value.provider ?: return
        val description = _uiState.value.requestDescription.trim()
        if (description.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }

            val booking = Booking(
                providerUid  = provider.uid,
                providerName = provider.name,
                clientUid    = clientUid,
                clientName   = clientName,
                clientPhone  = clientPhone,
                category     = provider.category,
                description  = description
            )

            when (val result = bookingRepository.createBooking(booking)) {
                is Result.Success -> _uiState.update {
                    it.copy(
                        isSubmitting  = false,
                        showRequestSheet = false,
                        bookingSuccess = true,
                        bookingSlug   = result.data.publicSlug
                    )
                }
                is Result.Error -> _uiState.update {
                    it.copy(isSubmitting = false, error = result.message)
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
    fun clearBookingSuccess() = _uiState.update { it.copy(bookingSuccess = false) }
}
