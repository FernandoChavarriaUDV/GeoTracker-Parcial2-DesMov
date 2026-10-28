package com.example.geotracker.data.remote

import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val token: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
        if (!chain.request().url.encodedPath.endsWith("/auth/login")) {
            token()?.takeIf { it.isNotBlank() }?.let { request.header("Authorization", "Bearer $it") }
        }
        return chain.proceed(request.build())
    }
}
