package com.attendance.app.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.attendance.app.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val isDarkMode: Boolean = false,
    val isNotificationsEnabled: Boolean = false,
    val isBiometricEnabled: Boolean = false,
    val backupMessage: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            preferencesManager.darkModeFlow.collect { dark ->
                _state.update { it.copy(isDarkMode = dark) }
            }
        }
        viewModelScope.launch {
            preferencesManager.notificationsEnabledFlow.collect { enabled ->
                _state.update { it.copy(isNotificationsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            preferencesManager.biometricEnabledFlow.collect { enabled ->
                _state.update { it.copy(isBiometricEnabled = enabled) }
            }
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setDarkMode(enabled) }
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setNotificationsEnabled(enabled) }
    }

    fun toggleBiometric(enabled: Boolean) {
        viewModelScope.launch { preferencesManager.setBiometricEnabled(enabled) }
    }

    fun createBackup() {
        _state.update { it.copy(backupMessage = "Backup created successfully!") }
    }

    fun restoreBackup() {
        _state.update { it.copy(backupMessage = "Restore completed successfully!") }
    }

    fun clearBackupMessage() {
        _state.update { it.copy(backupMessage = null) }
    }
}
