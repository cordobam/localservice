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
    val error: String? = null,
    val editingReviewId: String? = null,
    val isDeleting: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class ReviewViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun onRatingChanged(rating: Int) = _uiState.update { it.copy(rating = rating) }
    fun onCommentChanged(comment: String) = _uiState.update { it.copy(comment = comment) }

    fun initForEdit(review: Review) {
        _uiState.update {
            it.copy(
                editingReviewId = review.id,
                rating = review.rating.toInt(),
                comment = review.comment
            )
        }
    }

    fun initForEditById(reviewId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = reviewRepository.getReviewById(reviewId)) {
                is Result.Success -> {
                    result.data?.let { review ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                editingReviewId = review.id,
                                rating = review.rating.toInt(),
                                comment = review.comment
                            )
                        }
                    } ?: _uiState.update { it.copy(isLoading = false, error = "No se encontró la reseña") }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun submitReview(
        providerUid: String,
        clientUid: String,
        clientName: String,
        bookingId: String
    ) {
        val state = _uiState.value
        if (state.rating == 0) {
            _uiState.update { it.copy(error = "Seleccioná una calificación") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, error = null) }

            val review = Review(
                id          = state.editingReviewId ?: "",
                providerUid = providerUid,
                clientUid   = clientUid,
                clientName  = clientName,
                rating      = state.rating.toFloat(),
                comment     = state.comment.trim(),
                bookingId   = bookingId
            )

            val result = if (state.editingReviewId != null) {
                reviewRepository.updateReview(review)
            } else {
                reviewRepository.addReview(review)
            }

            when (result) {
                is Result.Success -> _uiState.update { it.copy(isSubmitting = false, isSubmitted = true) }
                is Result.Error   -> _uiState.update { it.copy(isSubmitting = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }
            when (val result = reviewRepository.deleteReview(reviewId)) {
                is Result.Success -> _uiState.update { it.copy(isDeleting = false, isDeleted = true) }
                is Result.Error   -> _uiState.update { it.copy(isDeleting = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
