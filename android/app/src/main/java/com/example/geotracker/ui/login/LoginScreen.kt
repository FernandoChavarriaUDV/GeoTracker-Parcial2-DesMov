package com.example.geotracker.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.geotracker.model.LoginState
import com.example.geotracker.ui.components.Message

@Composable
fun LoginScreen(state: LoginState, onUsername: (String) -> Unit, onPassword: (String) -> Unit, onLogin: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Spacer(Modifier.height(32.dp))
        Text("GeoTracker", style = MaterialTheme.typography.headlineLarge)
        Text("Tus lugares de interés", style = MaterialTheme.typography.titleMedium)
        Text("Inicia sesión para registrar y consultar tus ubicaciones.")
        OutlinedTextField(state.username, onUsername, label = { Text("Usuario") },
            singleLine = true, enabled = !state.loading, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.password, onPassword, label = { Text("Contraseña") }, singleLine = true,
            visualTransformation = PasswordVisualTransformation(), enabled = !state.loading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), modifier = Modifier.fillMaxWidth())
        state.error?.let { Message(it, error = true) }
        Button(onClick = onLogin, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.loading) "Iniciando sesión…" else "Iniciar sesión")
        }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
    }
}
