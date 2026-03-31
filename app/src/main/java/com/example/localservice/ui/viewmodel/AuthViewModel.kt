package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.User
import com.example.localservice.domain.model.UserRole
import com.example.localservice.util.Result
import com.example.localservice.domain.repository.AuthRepository
import com.example.localservice.util.isValidEmail
import com.example.localservice.util.isValidPassword
import com.example.localservice.util.toFriendlyError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// --- UiState ---
// Representa todo lo que la pantalla necesita saber para dibujarse.
// Es una data class, no una sealed class, porque los estados
// no son excluyentes: podés estar cargando Y tener un error previo.
data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentUser: User? = null,
    val isLoggedIn: Boolean = false
)

// --- AuthViewModel ---
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        // Al arrancar la app, verificamos si ya hay una sesión activa
        checkCurrentSession()
    }

    private fun checkCurrentSession() {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _uiState.update {
                    it.copy(
                        currentUser = user,
                        isLoggedIn = user != null
                    )
                }
            }
        }
    }

    fun login(email: String, password: String) {
        // Validaciones antes de llamar a Firebase
        if (!email.isValidEmail()) {
            _uiState.update { it.copy(error = "El correo no es válido") }
            return
        }
        if (!password.isValidPassword()) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.login(email, password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = result.data,
                            isLoggedIn = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message.toFriendlyError()
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun register(
        name: String,
        email: String,
        password: String,
        phone: String,
        role: UserRole
    ) {
        if (name.isBlank()) {
            _uiState.update { it.copy(error = "Ingresá tu nombre") }
            return
        }
        if (!email.isValidEmail()) {
            _uiState.update { it.copy(error = "El correo no es válido") }
            return
        }
        if (!password.isValidPassword()) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            when (val result = authRepository.register(name, email, password, phone, role)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentUser = result.data,
                            isLoggedIn = true
                        )
                    }
                }
                is Result.Error -> {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = result.message.toFriendlyError()
                        )
                    }
                }
                is Result.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { AuthUiState() } // resetea el estado
        }
    }

    // Limpia el error después de mostrarlo en la UI
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
