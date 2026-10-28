package com.example.geotracker.model

data class ApiResponse<T>(val httpCode: Int, val message: String, val body: T?)
data class LoginRequest(val username: String, val password: String)
data class LoginResponse(val login: Boolean, val token: String)
data class LocationRequest(val name: String, val description: String, val latitude: Double, val longitude: Double)
data class LocationItem(
    val id: Long,
    val name: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val createdAt: String
)
