package com.attendance.app.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.domain.repository.AiChatMessage
import com.attendance.app.domain.repository.AiRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AiAssistantState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage("Hello! I'm your AI Assistant. How can I help you today?", isUser = false)
    ),
    val isLoading: Boolean = false,
    val currentInput: String = "",
    val voiceResult: String? = null
)

sealed class AiAction {
    data class Navigate(val route: String) : AiAction()
    object SaveAttendance : AiAction()
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

fun ChatMessage.toDomain() = AiChatMessage(text, isUser)

@HiltViewModel
class AiAssistantViewModel @Inject constructor(
    private val aiRepository: AiRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AiAssistantState())
    val state: StateFlow<AiAssistantState> = _state.asStateFlow()

    private val _actionEvent = MutableSharedFlow<AiAction>()
    val actionEvent = _actionEvent.asSharedFlow()

    fun onInputChange(input: String) {
        _state.update { it.copy(currentInput = input) }
    }

    fun onVoiceInputCaptured(text: String) {
        _state.update { it.copy(currentInput = text) }
        sendMessage()
    }

    fun sendMessage() {
        val input = _state.value.currentInput.trim()
        if (input.isEmpty()) return

        val userMessage = ChatMessage(input, isUser = true)
        val history = _state.value.messages.map { it.toDomain() }

        _state.update {
            it.copy(
                messages = it.messages + userMessage,
                currentInput = "",
                isLoading = true
            )
        }

        viewModelScope.launch {
            aiRepository.processAiCommand(input, history).collect { response ->
                var cleanText = response

                // Parse Navigation
                if (response.contains("ACTION:NAVIGATE")) {
                    val navigateLine = response.lines().find { it.contains("ACTION:NAVIGATE") }
                    navigateLine?.let { line ->
                        val route = line.split("|").find { it.startsWith("ROUTE:") }?.removePrefix("ROUTE:")?.trim()
                        route?.let { _actionEvent.emit(AiAction.Navigate(it)) }
                        cleanText = cleanText.replace(line, "").trim()
                    }
                }

                // Parse Attendance Action
                if (response.contains("ACTION:MARK_ATTENDANCE")) {
                    val attendanceLine = response.lines().find { it.contains("ACTION:MARK_ATTENDANCE") }
                    attendanceLine?.let { line ->
                        _actionEvent.emit(AiAction.SaveAttendance)
                        cleanText = cleanText.replace(line, "").trim()
                    }
                }

                if (cleanText.isNotEmpty()) {
                    _state.update {
                        it.copy(
                            messages = it.messages + ChatMessage(cleanText, isUser = false),
                            isLoading = false
                        )
                    }
                } else {
                    _state.update { it.copy(isLoading = false) }
                }
            }
        }
    }
}
