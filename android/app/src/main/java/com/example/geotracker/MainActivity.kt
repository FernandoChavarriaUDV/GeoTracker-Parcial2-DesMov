package com.example.geotracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.*
import com.example.geotracker.data.AppContainer
import com.example.geotracker.ui.capture.*
import com.example.geotracker.ui.locations.*
import com.example.geotracker.ui.login.*
import com.example.geotracker.ui.map.MapScreen

class MainActivity : ComponentActivity() {
    private val container by lazy { AppContainer(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF185C45), secondary = Color(0xFF52675B))) {
                GeoTrackerApp(container)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeoTrackerApp(container: AppContainer) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val start = remember { if (container.session.token().isNullOrBlank()) "login" else "dashboard" }
    Scaffold(topBar = {
        if (route != null && route != "login") {
            TopAppBar(title = { Text("GeoTracker") }, navigationIcon = {
                if (route != "dashboard") TextButton(onClick = { nav.popBackStack() }) { Text("Volver") }
            }, actions = {
                TextButton(onClick = {
                    container.session.clear()
                    nav.navigate("login") { popUpTo(nav.graph.id) { inclusive = true } }
                }) { Text("Salir") }
            })
        }
    }) { padding ->
        NavHost(nav, startDestination = start, modifier = Modifier.padding(padding)) {
            composable("login") {
                val vm: LoginViewModel = viewModel(factory = viewModelFactory { initializer { LoginViewModel(container.authRepository) } })
                val state by vm.state.collectAsStateWithLifecycle()
                LaunchedEffect(state.loggedIn) {
                    if (state.loggedIn) nav.navigate("dashboard") { popUpTo("login") { inclusive = true } }
                }
                LoginScreen(state, vm::username, vm::password, vm::login)
            }
            composable("dashboard") {
                Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Mis lugares de interés", style = MaterialTheme.typography.headlineMedium)
                    Text("Registra tu ubicación y consulta los lugares que has guardado.")
                    Button(onClick = { nav.navigate("capture") }, modifier = Modifier.fillMaxWidth()) { Text("Capturar ubicación") }
                    OutlinedButton(onClick = { nav.navigate("locations") }, modifier = Modifier.fillMaxWidth()) { Text("Mis ubicaciones") }
                    OutlinedButton(onClick = { nav.navigate("map") }, modifier = Modifier.fillMaxWidth()) { Text("Ver mapa") }
                }
            }
            composable("capture") {
                val vm: CaptureViewModel = viewModel(factory = viewModelFactory {
                    initializer { CaptureViewModel(container.locationRepository, container.locationProvider) }
                })
                val state by vm.state.collectAsStateWithLifecycle()
                CaptureScreen(state, vm::name, vm::description, vm::capture, vm::permissionDenied)
            }
            composable("locations") {
                val vm: LocationsViewModel = viewModel(factory = viewModelFactory { initializer { LocationsViewModel(container.locationRepository) } })
                val state by vm.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { vm.load() }
                LocationsScreen(state, vm::load, { nav.navigate("map") })
            }
            composable("map") {
                val vm: LocationsViewModel = viewModel(factory = viewModelFactory { initializer { LocationsViewModel(container.locationRepository) } })
                val state by vm.state.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { vm.load() }
                MapScreen(state, vm::load, vm::select, vm::closeDialog)
            }
        }
    }
}
