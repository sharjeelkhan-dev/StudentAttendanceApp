package com.attendance.app.domain.repository

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserEmail: String?
    val isUserSignedIn: Boolean
    
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signOut()
    fun authStateFlow(): Flow<Boolean>
}
