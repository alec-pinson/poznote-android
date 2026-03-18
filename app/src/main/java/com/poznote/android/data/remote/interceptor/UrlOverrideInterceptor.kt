package com.poznote.android.data.remote.interceptor

import com.poznote.android.data.local.AuthPreferences
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UrlOverrideInterceptor @Inject constructor(
    private val authPreferences: AuthPreferences
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val serverUrl = authPreferences.serverUrl.trimEnd('/')
        val baseUrl = serverUrl.toHttpUrlOrNull()
            ?: return chain.proceed(chain.request())

        val originalRequest = chain.request()
        val newUrl = originalRequest.url.newBuilder()
            .scheme(baseUrl.scheme)
            .host(baseUrl.host)
            .port(baseUrl.port)
            .build()

        val newRequest = originalRequest.newBuilder().url(newUrl).build()
        return chain.proceed(newRequest)
    }
}
