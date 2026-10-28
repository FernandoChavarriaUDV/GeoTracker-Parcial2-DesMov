package com.example.geotracker.data

import com.example.geotracker.data.remote.ApiService
import com.example.geotracker.model.*
import java.io.IOException
import retrofit2.HttpException

class AuthRepository(private val api: ApiService, private val session: SessionManager) {
    suspend fun login(username: String, password: String) {
        val result = api.login(LoginRequest(username.trim(), password)).body
        check(result != null && result.login && result.token.isNotBlank()) { "Respuesta de login inválida" }
        session.save(result.token)
    }
}

class LocationRepository(private val api: ApiService) {
    suspend fun list(): List<LocationItem> = checkNotNull(api.getLocations().body) { "Respuesta vacía del servidor" }
    suspend fun save(request: LocationRequest): LocationItem =
        checkNotNull(api.saveLocation(request).body) { "No se recibió la ubicación guardada" }
}

fun errorMessage(error: Exception): String = when (error) {
    is HttpException -> when (error.code()) {
        401 -> "Credenciales incorrectas o sesión vencida. Inicia sesión nuevamente."
        403 -> "No tienes permiso para esta acción."
        400 -> "Revisa el nombre, la descripción y las coordenadas."
        404 -> "El recurso solicitado no está disponible en el backend."
        else -> "El servidor no pudo completar la solicitud. Intenta nuevamente."
    }
    is IOException -> "No se pudo conectar. Revisa tu conexión a Internet."
    else -> error.message ?: "No se pudo completar la operación."
}
