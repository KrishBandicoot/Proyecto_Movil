package com.example.kkarhua.utils

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.core.content.ContextCompat

class CameraHelper(private val context: Context) {

    companion object {
        const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
    }

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            CAMERA_PERMISSION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun requestCameraPermission(
        launcher: ActivityResultLauncher<String>
    ) {
        launcher.launch(CAMERA_PERMISSION)
    }

    fun showPermissionDeniedMessage() {
        Toast.makeText(
            context,
            "Se necesita permiso de cámara para tomar fotos de productos",
            Toast.LENGTH_LONG
        ).show()
    }

    fun isCameraAvailable(): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }
}