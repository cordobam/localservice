package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Booking
import com.example.localservice.domain.model.BookingStatus
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.domain.repository.ReviewRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class EarningsUiState(
    val isLoading: Boolean = true,
    val completedBookings: List<Booking> = emptyList(),
    val completedThisMonth: Int = 0,
    val earningsThisMonth: Int = 0,
    val totalCompleted: Int = 0,
    val averageRating: Float = 0f
)

@HiltViewModel
class EarningsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val reviewRepository: ReviewRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EarningsUiState())
    val uiState: StateFlow<EarningsUiState> = _uiState.asStateFlow()

    fun init(providerUid: String) {
        viewModelScope.launch {
            // Bookings completados
            bookingRepository.getBookingsForProvider(providerUid).collect { result ->
                if (result is Result.Success) {
                    val completed = result.data.filter { it.status == BookingStatus.COMPLETED }
                    val currentMonth = YearMonth.now()
                    val thisMonth = completed.filter { booking ->
                        val month = Instant.ofEpochMilli(booking.updatedAt)
                            .atZone(ZoneId.systemDefault())
                            .let { YearMonth.of(it.year, it.month) }
                        month == currentMonth
                    }
                    _uiState.update {
                        it.copy(
                            isLoading          = false,
                            completedBookings  = completed,
                            completedThisMonth = thisMonth.size,
                            earningsThisMonth  = thisMonth.sumOf { b -> b.budgetAmount },
                            totalCompleted     = completed.size
                        )
                    }
                }
            }
        }

        // Rating promedio
        viewModelScope.launch {
            reviewRepository.getReviewsForProvider(providerUid).collect { result ->
                if (result is Result.Success && result.data.isNotEmpty()) {
                    val avg = result.data.map { it.rating }.average().toFloat()
                    _uiState.update { it.copy(averageRating = avg) }
                }
            }
        }
    }
}
