package com.example.geotracker.data

data class Coordinates(val latitude: Double, val longitude: Double)

interface LocationSource {
    suspend fun current(): Coordinates
}
