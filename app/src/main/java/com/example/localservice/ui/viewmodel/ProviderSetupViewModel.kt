package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProviderSetupUiState(
    val selectedCategory: ServiceCategory? = null,
    val description: String = "",
    val zone: String = "",
    val city: String = "",
    val priceFrom: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false    // true cuando el perfil se guardó OK
)

@HiltViewModel
class ProviderSetupViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProviderSetupUiState())
    val uiState: StateFlow<ProviderSetupUiState> = _uiState.asStateFlow()

    fun onCategorySelected(category: ServiceCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDescriptionChanged(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onZoneChanged(value: String) {
        _uiState.update { it.copy(zone = value) }
    }

    fun onCityChanged(value: String) {
        _uiState.update { it.copy(city = value) }
    }

    fun onPriceFromChanged(value: String) {
        if (value.all { it.isDigit() }) {
            _uiState.update { it.copy(priceFrom = value) }
        }
    }

    val isFormValid: Boolean
        get() = _uiState.value.selectedCategory != null &&
                _uiState.value.zone.isNotBlank() &&
                _uiState.value.city.isNotBlank()

    fun saveProfile(uid: String, name: String) {
        val state = _uiState.value
        val category = state.selectedCategory ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val provider = Provider(
                uid         = uid,
                name        = name,
                category    = category,
                description = state.description.trim(),
                zone        = state.zone.trim(),
                city        = state.city.trim(),
                priceFrom   = state.priceFrom.toIntOrNull() ?: 0,
                isAvailable = true,
                createdAt   = System.currentTimeMillis()
            )

            when (val result = providerRepository.updateProviderProfile(provider)) {
                is Result.Success -> _uiState.update {
                    it.copy(isLoading = false, isComplete = true)
                }
                is Result.Error -> _uiState.update {
                    it.copy(isLoading = false, error = result.message)
                }
                else -> Unit
            }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
