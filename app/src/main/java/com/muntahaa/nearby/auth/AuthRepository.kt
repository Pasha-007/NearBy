package com.muntahaa.nearby.auth

import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val authStatus: Flow<AuthStatus>

    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun login(email: String, password: String): Result<Unit>
    fun signOut()
}
