package com.attendance.app.presentation.auth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val isSuccess: Boolean = false,
    val isSignUpMode: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password) }
    }

    fun toggleMode() {
        _state.update { it.copy(isSignUpMode = !it.isSignUpMode, error = null, message = null) }
    }

    fun onAuthAction() {
        val email = _state.value.email
        val password = _state.value.password

        if (email.isBlank() || password.isBlank()) {
            _state.update { it.copy(error = "Email and password cannot be empty", message = null) }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, message = null) }
            val result = if (_state.value.isSignUpMode) {
                authRepository.signUp(email, password)
            } else {
                authRepository.signIn(email, password)
            }

            result.fold(
                onSuccess = {
                    if (_state.value.isSignUpMode) {
                        // For Sign Up: Sign out immediately so they can log in manually
                        authRepository.signOut()
                        _state.update { 
                            it.copy(
                                isLoading = false, 
                                isSignUpMode = false,
                                password = "", // Clear password for security
                                message = "Account created successfully! Please login."
                            ) 
                        }
                    } else {
                        _state.update { it.copy(isLoading = false, isSuccess = true) }
                    }
                },
                onFailure = { e ->
                    _state.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
            )
        }
    }

}
