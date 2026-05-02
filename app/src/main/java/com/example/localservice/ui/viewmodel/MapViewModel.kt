package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MapUiState(
    val isLoading: Boolean = true,
    val providers: List<Provider> = emptyList(),
    val selectedCategory: ServiceCategory? = null,
    val error: String? = null
) {
    val filteredProviders: List<Provider>
        get() = if (selectedCategory == null) providers
                else providers.filter { it.category == selectedCategory }
}

@HiltViewModel
class MapViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            providerRepository.searchProviders(SearchFilter()).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> _uiState.update {
                        it.copy(isLoading = false, providers = result.data)
                    }
                    is Result.Error -> _uiState.update {
                        it.copy(isLoading = false, error = result.message)
                    }
                }
            }
        }
    }

    fun onCategorySelected(category: ServiceCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun reload() {
        _uiState.update { it.copy(isLoading = true) }
        load()
    }
}
