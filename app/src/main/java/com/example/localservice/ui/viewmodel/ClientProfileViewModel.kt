package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.repository.AuthRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClientProfileUiState(
    val name: String = "",
    val phone: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val showPasswordDialog: Boolean = false,
    val newPassword: String = "",
    val isChangingPassword: Boolean = false,
    val passwordChanged: Boolean = false
)

@HiltViewModel
class ClientProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ClientProfileUiState())
    val uiState: StateFlow<ClientProfileUiState> = _uiState.asStateFlow()

    fun init(name: String, phone: String) {
        _uiState.update { it.copy(name = name, phone = phone) }
    }

    fun onNameChanged(v: String) = _uiState.update { it.copy(name = v) }
    fun onPhoneChanged(v: String) = _uiState.update { it.copy(phone = v) }
    fun onNewPasswordChanged(v: String) = _uiState.update { it.copy(newPassword = v) }

    fun saveProfile(uid: String) {
        val state = _uiState.value
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = authRepository.updateUserProfile(uid, state.name.trim(), state.phone.trim())) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false, isSaved = true) }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun showPasswordDialog() = _uiState.update { it.copy(showPasswordDialog = true) }
    fun dismissPasswordDialog() = _uiState.update { it.copy(showPasswordDialog = false, newPassword = "") }

    fun changePassword() {
        val password = _uiState.value.newPassword
        if (password.length < 6) {
            _uiState.update { it.copy(error = "La contraseña debe tener al menos 6 caracteres") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isChangingPassword = true, error = null) }
            when (val result = authRepository.updatePassword(password)) {
                is Result.Success -> {
                    _uiState.update {
                        it.copy(isChangingPassword = false, passwordChanged = true, showPasswordDialog = false, newPassword = "")
                    }
                }
                is Result.Error -> {
                    _uiState.update { it.copy(isChangingPassword = false, error = result.message) }
                }
                else -> Unit
            }
        }
    }

    fun clearSaved() = _uiState.update { it.copy(isSaved = false) }
    fun clearPasswordChanged() = _uiState.update { it.copy(passwordChanged = false) }
    fun clearError() = _uiState.update { it.copy(error = null) }
}
