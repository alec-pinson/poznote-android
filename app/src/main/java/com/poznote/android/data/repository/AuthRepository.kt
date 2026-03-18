package com.poznote.android.data.repository

import com.poznote.android.data.local.AuthPreferences
import com.poznote.android.data.remote.api.PoznoteApi
import com.poznote.android.data.remote.model.UserDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val api: PoznoteApi,
    private val authPreferences: AuthPreferences
) {
    suspend fun login(serverUrl: String, username: String, password: String): Result<UserDto> {
        // Temporarily set credentials so interceptors can use them
        authPreferences.serverUrl = serverUrl
        authPreferences.username = username
        authPreferences.password = password

        return runCatching {
            val user = api.getMe()
            authPreferences.userId = user.id
            user
        }.onFailure {
            // Clear on failure
            authPreferences.clear()
        }
    }

    fun logout() {
        authPreferences.clear()
    }

    fun isLoggedIn(): Boolean = authPreferences.isLoggedIn()
}
