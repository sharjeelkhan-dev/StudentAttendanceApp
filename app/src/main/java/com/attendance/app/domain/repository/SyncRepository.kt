package com.attendance.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    suspend fun uploadDataToCloud(): Result<Unit>
    suspend fun downloadDataFromCloud(): Result<Unit>
    fun observeCloudChanges(): Flow<Unit>
}
