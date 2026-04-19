package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Review
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ReviewUiState(
    val rating: Int = 0,
    val comment: String = "",
    val isSubmitting: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun onRatingChanged(rating: Int) = _uiState.update { it.copy(rating = rating) }
    fun onCommentChanged(comment: String) = _uiState.update { it.copy(comment = comment) }

    fun submitReview(
        providerUid: String,
        clientUid: String,
        clientName: String
    ) {
        val rating = _uiState.value.rating
        if (rating == 0) {
            _uiState.update { it.copy(error = "Seleccioná una calificación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }
            val result = reviewRepository.addReview(
                Review(
                    providerUid = providerUid,
                    clientUid   = clientUid,
                    clientName  = clientName,
                    rating      = rating.toFloat(),
                    comment     = _uiState.value.comment.trim()
                )
            )
            when (result) {
                is Result.Success -> _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
                is Result.Error   -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
