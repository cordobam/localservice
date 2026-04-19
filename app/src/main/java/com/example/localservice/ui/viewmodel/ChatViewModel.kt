package com.example.localservice.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.localservice.domain.model.ChatMessage
import com.example.localservice.domain.repository.ChatRepository
import com.example.localservice.util.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val isLoading: Boolean = true,
    val messages: List<ChatMessage> = emptyList(),
    val inputText: String = "",
    val isSending: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var bookingId = ""

    fun init(bookingId: String) {
        this.bookingId = bookingId
        viewModelScope.launch {
            chatRepository.getMessages(bookingId).collect { result ->
                when (result) {
                    is Result.Loading -> _uiState.update { it.copy(isLoading = true) }
                    is Result.Success -> _uiState.update { it.copy(isLoading = false, messages = result.data) }
                    is Result.Error   -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }

    fun onInputChanged(text: String) = _uiState.update { it.copy(inputText = text) }

    fun sendMessage(senderUid: String, senderName: String) {
        val text = _uiState.value.inputText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true, inputText = "") }
            chatRepository.sendMessage(
                ChatMessage(
                    bookingId  = bookingId,
                    senderUid  = senderUid,
                    senderName = senderName,
                    text       = text
                )
            )
            _uiState.update { it.copy(isSending = false) }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
