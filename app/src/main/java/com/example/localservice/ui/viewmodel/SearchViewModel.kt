package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.Provider
import com.example.localservice.domain.model.SearchFilter
import com.example.localservice.domain.model.ServiceCategory
import com.example.localservice.domain.repository.ProviderRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val PAGE_SIZE = 20

data class SearchUiState(
    val isLoading: Boolean = false,
    val providers: List<Provider> = emptyList(),
    val error: String? = null,
    val selectedCategory: ServiceCategory? = null,
    val searchQuery: String = "",
    // Ubicación del usuario (null si no dio permiso)
    val userLat: Double? = null,
    val userLng: Double? = null,
    // Paginación cliente
    val visibleCount: Int = PAGE_SIZE
) {
    // Filtro activo derivado del estado — no hay que calcularlo en la UI
    val activeFilter: SearchFilter
        get() = SearchFilter(
            category  = selectedCategory,
            query     = searchQuery,
            userLat   = userLat,
            userLng   = userLng
        )

    val hasResults: Boolean get() = providers.isNotEmpty()
    val isEmpty: Boolean get() = !isLoading && providers.isEmpty() && error == null
    val hasMore: Boolean get() = visibleCount < providers.size
    val visibleProviders: List<Provider> get() = providers.take(visibleCount)
}

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val providerRepository: ProviderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    // Flow interno del filtro activo — cada cambio dispara una nueva búsqueda
    private val _filterFlow = MutableStateFlow(SearchFilter())

    init {
        observeFilter()
    }

    private fun observeFilter() {
        viewModelScope.launch {
            _filterFlow
                .debounce(300) // espera 300ms antes de buscar — evita queries por cada tecla
                .distinctUntilChanged()
                .flatMapLatest { filter ->
                    providerRepository.searchProviders(filter)
                }
                .collect { result ->
                    when (result) {
                        is Result.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
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
        _uiState.update { it.copy(selectedCategory = category, visibleCount = PAGE_SIZE) }
        _filterFlow.value = _uiState.value.activeFilter
    }

    fun onQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query, visibleCount = PAGE_SIZE) }
        _filterFlow.value = _uiState.value.activeFilter
    }

    fun onUserLocationUpdated(lat: Double, lng: Double) {
        _uiState.update { it.copy(userLat = lat, userLng = lng) }
        _filterFlow.value = _uiState.value.activeFilter
    }

    fun clearFilters() {
        _uiState.update {
            it.copy(selectedCategory = null, searchQuery = "", visibleCount = PAGE_SIZE)
        }
        _filterFlow.value = SearchFilter(userLat = _uiState.value.userLat, userLng = _uiState.value.userLng)
    }

    fun loadMore() {
        _uiState.update { it.copy(visibleCount = it.visibleCount + PAGE_SIZE) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
