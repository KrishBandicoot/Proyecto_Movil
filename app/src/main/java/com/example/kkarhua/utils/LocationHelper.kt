package com.example.kkarhua.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    companion object {
        const val LOCATION_PERMISSION = Manifest.permission.ACCESS_FINE_LOCATION
    }

    fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            LOCATION_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestLocationPermission(launcher: ActivityResultLauncher<String>) {
        launcher.launch(LOCATION_PERMISSION)
    }

    fun showPermissionDeniedMessage() {
        Toast.makeText(
            context,
            "Se necesita permiso de ubicación para encontrar tiendas cercanas",
            Toast.LENGTH_LONG
        ).show()
    }

    fun getCurrentLocation(
        onSuccess: (Location) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        if (!hasLocationPermission()) {
            onFailure(SecurityException("Permiso de ubicación no otorgado"))
            return
        }

        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        onSuccess(location)
                    } else {
                        onFailure(Exception("No se pudo obtener la ubicación"))
                    }
                }
                .addOnFailureListener { exception ->
                    onFailure(exception)
                }
        } catch (e: SecurityException) {
            onFailure(e)
        }
    }
}