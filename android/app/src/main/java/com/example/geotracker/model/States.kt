package com.example.geotracker.model

data class LoginState(
    val username: String = "", val password: String = "",
    val loading: Boolean = false, val loggedIn: Boolean = false, val error: String? = null
)
data class CaptureState(
    val name: String = "", val description: String = "",
    val loading: Boolean = false, val message: String? = null, val error: String? = null
)
data class LocationsState(
    val loading: Boolean = false, val items: List<LocationItem> = emptyList(),
    val selected: LocationItem? = null, val error: String? = null
)
