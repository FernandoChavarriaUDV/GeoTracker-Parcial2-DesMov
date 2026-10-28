package com.example.geotracker.ui.locations

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.geotracker.model.LocationsState
import com.example.geotracker.ui.components.*

@Composable
fun LocationsScreen(state: LocationsState, onRefresh: () -> Unit, onMap: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Mis ubicaciones", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRefresh, enabled = !state.loading) { Text("Actualizar") }
            Button(onClick = onMap) { Text("Ver mapa") }
        }
        if (state.loading) { Loading(); return@Column }
        state.error?.let { Message(it, error = true) }
        if (state.error == null && state.items.isEmpty()) Text("Todavía no has registrado ubicaciones.")
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(state.items, key = { it.id }) { item ->
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Text(item.description)
                        Text("Latitud: ${item.latitude}\nLongitud: ${item.longitude}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
