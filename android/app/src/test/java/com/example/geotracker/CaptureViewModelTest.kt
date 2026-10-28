package com.example.geotracker

import com.example.geotracker.data.*
import com.example.geotracker.data.remote.ApiService
import com.example.geotracker.model.*
import com.example.geotracker.ui.capture.CaptureViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*
import java.io.IOException

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureViewModelTest {
    private val dispatcher = StandardTestDispatcher()
    @Before fun setup() { Dispatchers.setMain(dispatcher) }
    @After fun teardown() { Dispatchers.resetMain() }

    private class TestApi : ApiService {
        var sent: LocationRequest? = null
        var saveCalls = 0
        var saveResult: CompletableDeferred<Unit>? = null
        var failure: Exception? = null
        override suspend fun login(request: LoginRequest): ApiResponse<LoginResponse> = error("No se usa")
        override suspend fun getLocations(): ApiResponse<List<LocationItem>> = error("No se usa")
        override suspend fun saveLocation(request: LocationRequest): ApiResponse<LocationItem> {
            saveCalls++
            sent = request
            saveResult?.await()
            failure?.let { throw it }
            return ApiResponse(201, "Ubicación guardada", LocationItem(1, request.name, request.description,
                request.latitude, request.longitude, "2026-10-27T12:00:00Z"))
        }
    }

    @Test fun keepsLoadingDuringGpsAndUploadSendsAllFieldsAndPreventsDuplicateRequests() = runTest(dispatcher) {
        val gpsResult = CompletableDeferred<Coordinates>()
        var gpsCalls = 0
        val gps = object : LocationSource {
            override suspend fun current(): Coordinates { gpsCalls++; return gpsResult.await() }
        }
        val api = TestApi().apply { saveResult = CompletableDeferred() }
        val vm = CaptureViewModel(LocationRepository(api), gps)
        vm.name(" Universidad ")
        vm.description(" Entrada principal ")
        vm.capture()
        runCurrent()
        assertTrue(vm.state.value.loading)
        assertEquals(0, api.saveCalls)
        vm.capture()
        gpsResult.complete(Coordinates(14.6349, -90.5069))
        runCurrent()
        assertTrue(vm.state.value.loading)
        assertEquals(1, gpsCalls)
        assertEquals(1, api.saveCalls)
        assertEquals(LocationRequest("Universidad", "Entrada principal", 14.6349, -90.5069), api.sent)
        api.saveResult!!.complete(Unit)
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertNotNull(vm.state.value.message)
        assertNull(vm.state.value.error)
        assertEquals("", vm.state.value.name)
        assertEquals("", vm.state.value.description)
        vm.permissionDenied()
        assertNull(vm.state.value.message)
        assertNotNull(vm.state.value.error)
    }

    @Test fun rejectsEmptyAndOversizedFormsBeforeRequestingGpsOrCallingBackend() = runTest(dispatcher) {
        val gps = object : LocationSource { override suspend fun current(): Coordinates = error("No debe solicitar GPS") }
        val api = TestApi()
        for ((name, description) in listOf("" to "Detalle", "Lugar" to " ", "a".repeat(121) to "Detalle", "Lugar" to "a".repeat(1001))) {
            val vm = CaptureViewModel(LocationRepository(api), gps)
            vm.name(name)
            vm.description(description)
            vm.capture()
            advanceUntilIdle()
            assertFalse(vm.state.value.loading)
            assertNotNull(vm.state.value.error)
        }
        assertEquals(0, api.saveCalls)
    }

    @Test fun gpsFailureStopsLoadingPreservesFormAndDoesNotSave() = runTest(dispatcher) {
        val gps = object : LocationSource {
            override suspend fun current(): Coordinates = throw IllegalStateException("No se obtuvo la ubicación")
        }
        val api = TestApi()
        val vm = CaptureViewModel(LocationRepository(api), gps)
        vm.name("Biblioteca")
        vm.description("Entrada")
        vm.capture()
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertEquals("No se obtuvo la ubicación", vm.state.value.error)
        assertEquals("Biblioteca", vm.state.value.name)
        assertEquals(0, api.saveCalls)
    }

    @Test fun backendFailureStopsLoadingPreservesFormAndNeverShowsSuccess() = runTest(dispatcher) {
        val gps = object : LocationSource { override suspend fun current() = Coordinates(14.63, -90.5) }
        val api = TestApi().apply { failure = IOException("Sin conexión") }
        val vm = CaptureViewModel(LocationRepository(api), gps)
        vm.name("Biblioteca")
        vm.description("Entrada")
        vm.capture()
        advanceUntilIdle()
        assertFalse(vm.state.value.loading)
        assertNotNull(vm.state.value.error)
        assertNull(vm.state.value.message)
        assertEquals("Biblioteca", vm.state.value.name)
        assertEquals(1, api.saveCalls)
    }

    @Test fun deniedPermissionProducesErrorWithoutGpsOrNetworkRequests() = runTest(dispatcher) {
        val gps = object : LocationSource { override suspend fun current(): Coordinates = error("No debe solicitar GPS") }
        val api = TestApi()
        val vm = CaptureViewModel(LocationRepository(api), gps)
        vm.permissionDenied()
        advanceUntilIdle()
        assertNotNull(vm.state.value.error)
        assertFalse(vm.state.value.loading)
        assertEquals(0, api.saveCalls)
    }
}
