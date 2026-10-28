package com.example.geotracker

import com.example.geotracker.data.LocationRepository
import com.example.geotracker.data.remote.ApiService
import com.example.geotracker.model.*
import com.example.geotracker.ui.locations.LocationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class LocationsViewModelTest {
    @Test
    fun loadsServerLocationsManagesDialogAndShowsConnectionErrors() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        try {
            val place = LocationItem(1, "Universidad", "Entrada principal", 14.63, -90.5, "2026-10-27T12:00:00Z")
            var fail = false
            val api = object : ApiService {
                override suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> = error("No se usa")
                override suspend fun saveLocation(request: LocationRequest): ApiResponse<LocationItem> = error("No se usa")
                override suspend fun getLocations(): ApiResponse<List<LocationItem>> {
                    if (fail) throw IOException("Sin conexión")
                    return ApiResponse(200, "OK", listOf(place))
                }
            }
            val vm = LocationsViewModel(LocationRepository(api))
            vm.load()
            assertTrue(vm.state.value.loading)
            advanceUntilIdle()
            assertEquals(listOf(place), vm.state.value.items)
            vm.select(place)
            assertEquals(place, vm.state.value.selected)
            vm.closeDialog()
            assertNull(vm.state.value.selected)
            fail = true
            vm.load()
            advanceUntilIdle()
            assertFalse(vm.state.value.loading)
            assertTrue(vm.state.value.items.isEmpty())
            assertNotNull(vm.state.value.error)
        } finally { Dispatchers.resetMain() }
    }
}
