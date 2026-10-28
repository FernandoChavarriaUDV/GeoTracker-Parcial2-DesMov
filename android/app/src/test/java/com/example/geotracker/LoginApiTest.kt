package com.example.geotracker

import com.example.geotracker.data.errorMessage
import com.example.geotracker.data.remote.ApiService
import com.example.geotracker.model.LoginRequest
import com.google.gson.JsonParser
import kotlinx.coroutines.test.runTest
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.*
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

class LoginApiTest {
    @Test
    fun sendsCredentialsToLoginEndpointAndReadsServerToken() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setBody("""
                {"httpCode":200,"message":"Inicio de sesión exitoso",
                 "body":{"login":true,"token":"token-devuelto-por-servidor"}}
            """.trimIndent()))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
            val response = api.login(LoginRequest("estudiante@example.com", "clave-de-prueba"))
            val request = server.takeRequest()
            assertEquals("POST", request.method)
            assertEquals("/api/v1/auth/login", request.path)
            val credentials = JsonParser.parseString(request.body.readUtf8()).asJsonObject
            assertEquals("estudiante@example.com", credentials["username"].asString)
            assertEquals("clave-de-prueba", credentials["password"].asString)
            assertEquals("token-devuelto-por-servidor", response.body?.token)
            assertEquals(true, response.body?.login)
        } finally { server.shutdown() }
    }

    @Test
    fun rejectsWrongCredentialsAndProvidesVisibleError() = runTest {
        val server = MockWebServer()
        server.start()
        try {
            server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
            val api = Retrofit.Builder().baseUrl(server.url("/"))
                .addConverterFactory(GsonConverterFactory.create()).build().create(ApiService::class.java)
            try {
                api.login(LoginRequest("estudiante@example.com", "incorrecta"))
                fail("El login no debe aceptar una respuesta HTTP 401")
            } catch (error: HttpException) {
                assertEquals(401, error.code())
                assertTrue(errorMessage(error).contains("Credenciales incorrectas"))
            }
        } finally { server.shutdown() }
    }

    @Test
    fun reportsConnectionFailureToUser() {
        assertEquals("No se pudo conectar. Revisa tu conexión a Internet.", errorMessage(IOException("Connection refused")))
    }
}
