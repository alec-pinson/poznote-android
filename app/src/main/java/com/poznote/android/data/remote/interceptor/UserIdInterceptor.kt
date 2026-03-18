package com.poznote.android.data.remote.interceptor

import com.poznote.android.data.local.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UserIdInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {

    private val skipPaths = listOf("/users/me", "/users/profiles")

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val shouldSkip = skipPaths.any { path.contains(it) }
        val userId = authPreferences.userId
        return if (!shouldSkip && userId != -1) {
            chain.proceed(
                request.newBuilder()
                    .header("X-User-ID", userId.toString())
                    .build()
            )
        } else {
            chain.proceed(request)
        }
    }
}
