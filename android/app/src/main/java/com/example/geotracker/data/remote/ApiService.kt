package com.example.geotracker.data.remote

import com.example.geotracker.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @POST("api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<LoginResponse>

    @POST("api/v1/locations")
    suspend fun saveLocation(@Body request: LocationRequest): ApiResponse<LocationItem>

    @GET("api/v1/locations")
    suspend fun getLocations(): ApiResponse<List<LocationItem>>
}
