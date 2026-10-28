package com.example.geotracker.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.geotracker.data.AuthRepository
import com.example.geotracker.data.errorMessage
import com.example.geotracker.model.LoginState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _state = MutableStateFlow(LoginState())
    val state = _state.asStateFlow()
    fun username(value: String) { _state.update { it.copy(username = value, error = null) } }
    fun password(value: String) { _state.update { it.copy(password = value, error = null) } }
    fun login() {
        val form = state.value
        if (form.loading) return
        if (form.username.isBlank() || form.password.isBlank()) {
            _state.update { it.copy(error = "Ingresa tu usuario y contraseña.") }
            return
        }
        _state.update { it.copy(loading = true, error = null) }
        viewModelScope.launch {
            try {
                repository.login(form.username, form.password)
                _state.update { it.copy(loading = false, loggedIn = true, password = "") }
            } catch (error: CancellationException) { throw error
            } catch (error: Exception) {
                _state.update { it.copy(loading = false, error = errorMessage(error)) }
            }
        }
    }
}
