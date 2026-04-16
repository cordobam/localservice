package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.DefaultStages
import com.example.localservice.domain.model.Stage
import com.example.localservice.domain.model.StageStatus
import com.example.localservice.domain.repository.BookingRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class StageEditorUiState(
    val stages: List<Stage> = emptyList(),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class StageEditorViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StageEditorUiState())
    val uiState: StateFlow<StageEditorUiState> = _uiState.asStateFlow()

    private var currentBookingId = ""

    fun init(bookingId: String) {
        currentBookingId = bookingId
        viewModelScope.launch {
            // Carga el booking para obtener la categoría y cargar etapas predefinidas
            bookingRepository.getBookingsForProvider("").take(1).collect { result ->
                if (result is Result.Success) {
                    val booking = result.data.firstOrNull { it.id == bookingId }
                    booking?.let {
                        // Si el booking ya tiene etapas las carga, sino carga las predefinidas
                        val stages = if (it.stages.isEmpty())
                            DefaultStages.forCategory(it.category)
                        else it.stages
                        _uiState.update { s -> s.copy(stages = stages) }
                    }
                }
            }
        }
    }

    fun addStage() {
        val newStage = Stage(
            id    = UUID.randomUUID().toString(),
            name  = "Nueva etapa",
            order = _uiState.value.stages.size + 1
        )
        _uiState.update { it.copy(stages = it.stages + newStage) }
    }

    fun removeStage(index: Int) {
        _uiState.update { state ->
            state.copy(stages = state.stages.toMutableList().also { it.removeAt(index) })
        }
    }

    fun updateStageName(index: Int, name: String) {
        _uiState.update { state ->
            val updated = state.stages.toMutableList()
            updated[index] = updated[index].copy(name = name)
            state.copy(stages = updated)
        }
    }

    fun updateStageDays(index: Int, days: String) {
        _uiState.update { state ->
            val updated = state.stages.toMutableList()
            updated[index] = updated[index].copy(estimatedDays = days.toIntOrNull() ?: 0)
            state.copy(stages = updated)
        }
    }

    fun updateStageStatus(index: Int, status: StageStatus) {
        _uiState.update { state ->
            val updated = state.stages.toMutableList()
            updated[index] = updated[index].copy(
                status      = status,
                completedAt = if (status == StageStatus.DONE) System.currentTimeMillis() else null
            )
            state.copy(stages = updated)
        }
    }

    fun saveStages() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val result = bookingRepository.updateStages(currentBookingId, _uiState.value.stages)
            when (result) {
                is Result.Success -> _uiState.update { it.copy(isSaving = false, isSaved = true) }
                is Result.Error   -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                else -> Unit
            }
        }
    }
}
