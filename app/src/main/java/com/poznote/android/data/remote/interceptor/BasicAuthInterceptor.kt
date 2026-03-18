package com.poznote.android.data.remote.interceptor

import android.util.Base64
import com.poznote.android.data.local.AuthPreferences
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class BasicAuthInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val username = authPreferences.username
        val password = authPreferences.password
        val credentials = Base64.encodeToString(
            "$username:$password".toByteArray(Charsets.UTF_8),
            Base64.NO_WRAP
        )
        val request = chain.request().newBuilder()
            .header("Authorization", "Basic $credentials")
            .build()
        return chain.proceed(request)
    }
}
