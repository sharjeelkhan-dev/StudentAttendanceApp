package com.attendance.app.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "attendance_prefs")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
        val SELECTED_CLASS_ID_KEY = longPreferencesKey("selected_class_id")
        val NOTIFICATIONS_ENABLED_KEY = booleanPreferencesKey("notifications_enabled")
        val BIOMETRIC_ENABLED_KEY = booleanPreferencesKey("biometric_enabled")
        val REMINDER_HOUR_KEY = intPreferencesKey("reminder_hour")
        val REMINDER_MINUTE_KEY = intPreferencesKey("reminder_minute")
    }

    val darkModeFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_MODE_KEY] ?: false
    }

    val selectedClassIdFlow: Flow<Long> = dataStore.data.map { prefs ->
        prefs[SELECTED_CLASS_ID_KEY] ?: -1L
    }

    val notificationsEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATIONS_ENABLED_KEY] ?: false
    }

    val biometricEnabledFlow: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[BIOMETRIC_ENABLED_KEY] ?: false
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_MODE_KEY] = enabled }
    }

    suspend fun setSelectedClassId(classId: Long) {
        dataStore.edit { prefs -> prefs[SELECTED_CLASS_ID_KEY] = classId }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[NOTIFICATIONS_ENABLED_KEY] = enabled }
    }

    suspend fun setBiometricEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[BIOMETRIC_ENABLED_KEY] = enabled }
    }
}
