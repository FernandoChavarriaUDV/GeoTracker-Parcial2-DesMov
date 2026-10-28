package com.example.geotracker

import com.example.geotracker.data.remote.AuthInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test

class AuthInterceptorTest {
    @Test
    fun sendsCurrentTokenOnlyOnAuthenticatedRequestsAndStopsAfterLogout() {
        val server = MockWebServer()
        server.start()
        try {
            var token: String? = "primer-token"
            val client = OkHttpClient.Builder().addInterceptor(AuthInterceptor { token }).build()
            fun call(path: String): String? {
                server.enqueue(MockResponse().setBody("{}"))
                client.newCall(Request.Builder().url(server.url(path)).build()).execute().use { it.body?.string() }
                return server.takeRequest().getHeader("Authorization")
            }
            assertNull(call("/api/v1/auth/login"))
            assertEquals("Bearer primer-token", call("/api/v1/locations"))
            token = "segundo-token"
            assertEquals("Bearer segundo-token", call("/api/v1/locations"))
            token = null
            assertNull(call("/api/v1/locations"))
        } finally { server.shutdown() }
    }
}
