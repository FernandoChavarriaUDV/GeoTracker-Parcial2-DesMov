package com.example.geotracker.data

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationProvider(context: Context) : LocationSource {
    private val client = LocationServices.getFusedLocationProviderClient(context)

    // La pantalla solicita los permisos antes de iniciar esta operación.
    @SuppressLint("MissingPermission")
    override suspend fun current(): Coordinates = suspendCancellableCoroutine { continuation ->
        val cancellation = CancellationTokenSource()
        continuation.invokeOnCancellation { cancellation.cancel() }
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
            .setMaxUpdateAgeMillis(0)
            .setDurationMillis(20000)
            .build()
        client.getCurrentLocation(request, cancellation.token)
            .addOnSuccessListener { location ->
                if (continuation.isActive) {
                    if (location == null) continuation.resumeWithException(
                        IllegalStateException("No se obtuvo la ubicación. Activa la ubicación del teléfono e intenta otra vez."))
                    else continuation.resume(Coordinates(location.latitude, location.longitude))
                }
            }
            .addOnFailureListener { if (continuation.isActive) continuation.resumeWithException(it) }
            .addOnCanceledListener {
                if (continuation.isActive) continuation.resumeWithException(
                    IllegalStateException("La captura de ubicación fue cancelada. Intenta nuevamente."))
            }
    }
}
