package com.example.geotracker.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.data.*
import com.example.geotracker.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CaptureViewModel(private val repository: LocationRepository, private val gps: LocationSource) : ViewModel() {
    private val _state = MutableStateFlow(CaptureState())
    val state = _state.asStateFlow()
    fun name(value: String) { _state.update { it.copy(name = value, error = null, message = null) } }
    fun description(value: String) { _state.update { it.copy(description = value, error = null, message = null) } }
    fun permissionDenied() { _state.update { it.copy(message = null, error = "Se necesita permiso de ubicación. Puedes habilitarlo en Ajustes > Aplicaciones > GeoTracker > Permisos.") } }
    fun capture() {
        val form = state.value
        if (form.loading) return
        if (form.name.isBlank() || form.description.isBlank() || form.name.trim().length > 120 || form.description.trim().length > 1000) {
            _state.update { it.copy(error = "Ingresa nombre (máximo 120 caracteres) y descripción (máximo 1000).", message = null) }
            return
        }
        _state.update { it.copy(loading = true, error = null, message = null) }
        viewModelScope.launch {
            try {
                val location = gps.current()
                repository.save(LocationRequest(form.name.trim(), form.description.trim(), location.latitude, location.longitude))
                _state.value = CaptureState(message = "Ubicación guardada correctamente.")
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) {
                _state.update { it.copy(loading = false, error = errorMessage(error)) }
            }
        }
    }
}
