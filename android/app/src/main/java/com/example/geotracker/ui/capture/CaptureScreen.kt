package com.example.geotracker.ui.capture

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.geotracker.model.CaptureState
import com.example.geotracker.ui.components.Message

@Composable
fun CaptureScreen(state: CaptureState, onName: (String) -> Unit, onDescription: (String) -> Unit,
                  onCapture: () -> Unit, onDenied: () -> Unit) {
    val context = LocalContext.current
    val permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        if (result.values.any { it }) onCapture() else onDenied()
    }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Nueva ubicación", style = MaterialTheme.typography.headlineSmall)
        Text("Escribe los datos del lugar. Al guardar se obtendrá tu ubicación actual.")
        OutlinedTextField(state.name, onName, label = { Text("Nombre del lugar") }, singleLine = true,
            enabled = !state.loading, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(state.description, onDescription, label = { Text("Descripción") }, minLines = 3,
            enabled = !state.loading, modifier = Modifier.fillMaxWidth())
        state.error?.let { Message(it, error = true) }
        state.message?.let { Message(it) }
        Button(onClick = {
            if (permissions.any { ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED }) onCapture()
            else launcher.launch(permissions)
        }, enabled = !state.loading, modifier = Modifier.fillMaxWidth()) {
            Text(if (state.loading) "Obteniendo GPS y guardando…" else "Capturar y guardar ubicación")
        }
        if (state.loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        Text("Activa la ubicación del teléfono y permite el acceso cuando se solicite.",
            style = MaterialTheme.typography.bodySmall)
    }
}
