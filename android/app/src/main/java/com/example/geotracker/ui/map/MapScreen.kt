package com.example.geotracker.ui.map

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geotracker.BuildConfig
import com.example.geotracker.model.*
import com.example.geotracker.ui.components.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.*

@Composable
fun MapScreen(state: LocationsState, onRefresh: () -> Unit, onSelect: (LocationItem) -> Unit, onClose: () -> Unit) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Mapa de lugares", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
            TextButton(onClick = onRefresh, enabled = !state.loading) { Text("Actualizar") }
        }
        when {
            state.loading -> Loading()
            state.error != null -> Box(Modifier.padding(20.dp)) { Message(state.error, error = true) }
            state.items.isEmpty() -> Text("Todavía no hay ubicaciones para mostrar.", Modifier.padding(20.dp))
            !BuildConfig.MAPS_CONFIGURED -> Text("Configura la clave de Google Maps para mostrar tus ubicaciones en el mapa.", Modifier.padding(20.dp))
            else -> LocationsMap(state.items, onSelect, Modifier.fillMaxSize())
        }
    }
    state.selected?.let { item ->
        AlertDialog(onDismissRequest = onClose,
            title = { Text(item.name) },
            text = { Text("${item.description}\n\nLatitud: ${item.latitude}\nLongitud: ${item.longitude}",
                Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = onClose) { Text("Cerrar") } })
    }
}

@Composable
private fun LocationsMap(items: List<LocationItem>, onSelect: (LocationItem) -> Unit, modifier: Modifier) {
    val camera = rememberCameraPositionState()
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(items, loaded) {
        if (loaded && items.isNotEmpty()) {
            val first = items.first()
            val samePoint = items.all { it.latitude == first.latitude && it.longitude == first.longitude }
            val update = if (samePoint) CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 16f)
            else {
                val bounds = LatLngBounds.builder()
                items.forEach { bounds.include(LatLng(it.latitude, it.longitude)) }
                CameraUpdateFactory.newLatLngBounds(bounds.build(), 100)
            }
            camera.move(update)
        }
    }
    GoogleMap(modifier = modifier, cameraPositionState = camera, onMapLoaded = { loaded = true }) {
        items.forEach { item ->
            key(item.id) {
                Marker(state = rememberMarkerState(position = LatLng(item.latitude, item.longitude)),
                    title = item.name, onClick = {
                        onSelect(item)
                        true // Consumir el clic evita que se abra el InfoWindow predeterminado.
                    })
            }
        }
    }
}
