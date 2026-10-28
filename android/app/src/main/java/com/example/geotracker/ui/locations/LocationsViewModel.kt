package com.example.geotracker.ui.locations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.data.LocationRepository
import com.example.geotracker.data.errorMessage
import com.example.geotracker.model.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LocationsViewModel(private val repository: LocationRepository) : ViewModel() {
    private val _state = MutableStateFlow(LocationsState())
    val state = _state.asStateFlow()
    fun load() {
        if (state.value.loading) return
        _state.update { it.copy(loading = true, selected = null, error = null) }
        viewModelScope.launch {
            try {
                _state.value = LocationsState(items = repository.list())
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) {
                _state.value = LocationsState(error = errorMessage(error))
            }
        }
    }
    fun select(item: LocationItem) { _state.update { it.copy(selected = item) } }
    fun closeDialog() { _state.update { it.copy(selected = null) } }
}
