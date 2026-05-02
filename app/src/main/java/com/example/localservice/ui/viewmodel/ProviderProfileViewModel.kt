package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val description: String = "",
    val zone: String = "",
    val city: String = "",
    val priceFrom: String = "",
    val category: ServiceCategory = ServiceCategory.OTHER,
    val isAvailable: Boolean = true,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,

    val mpAlias: String = ""
)

@HiltViewModel
class ProviderProfileViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderProfileUiState())
    val uiState: StateFlow<ProviderProfileUiState> = _uiState.asStateFlow()

    private var originalProvider: Provider? = null

    fun init(providerUid: String) {
        viewModelScope.launch {
            when (val result = providerRepository.getProviderById(providerUid)) {
                is Result.Success -> {
                    val p = result.data
                    originalProvider = p
                    _uiState.update {
                        it.copy(
                            isLoading   = false,
                            name        = p.name,
                            description = p.description,
                            zone        = p.zone,
                            city        = p.city,
                            priceFrom   = if (p.priceFrom > 0) p.priceFrom.toString() else "",
                            category    = p.category,
                            isAvailable = p.isAvailable,
                            mpAlias     = p.mpAlias
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun onDescriptionChanged(v: String) = _uiState.update { it.copy(description = v) }
    fun onZoneChanged(v: String)        = _uiState.update { it.copy(zone = v) }
    fun onCityChanged(v: String)        = _uiState.update { it.copy(city = v) }
    fun onPriceFromChanged(v: String)   { if (v.all { it.isDigit() }) _uiState.update { s -> s.copy(priceFrom = v) } }
    fun onAvailabilityChanged(v: Boolean) = _uiState.update { it.copy(isAvailable = v) }

    fun saveProfile(providerUid: String) {
        val state = _uiState.value
        val base = originalProvider ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val updated = base.copy(
                description = state.description.trim(),
                zone        = state.zone.trim(),
                city        = state.city.trim(),
                priceFrom   = state.priceFrom.toIntOrNull() ?: 0,
                isAvailable = state.isAvailable,
                mpAlias = state.mpAlias.trim()
            )
            when (val result = providerRepository.updateProviderProfile(updated)) {
                is Result.Success -> {
                    originalProvider = updated
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                is Result.Error -> _uiState.update { it.copy(isSaving = false, error = result.message) }
                else -> Unit
            }
        }
    }

    fun clearSaved() = _uiState.update { it.copy(isSaved = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }

    fun onMpAliasChanged(v: String) = _uiState.update { it.copy(mpAlias = v) }
}
