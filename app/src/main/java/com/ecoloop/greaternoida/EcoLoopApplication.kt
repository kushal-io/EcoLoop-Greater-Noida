package com.ecoloop.greaternoida

import android.app.Application
import android.util.Log
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.OnMapsSdkInitializedCallback
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EcoLoopApplication : Application(), OnMapsSdkInitializedCallback {

    override fun onCreate() {
        super.onCreate()
        // Force latest Google Maps renderer to prevent blank/white screen on modern devices
        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST, this)
        } catch (e: Exception) {
            Log.e("EcoLoopApp", "Failed to initialize Google Maps SDK", e)
        }
    }

    override fun onMapsSdkInitialized(renderer: MapsInitializer.Renderer) {
        Log.i("EcoLoopApp", "Google Maps initialized with renderer: $renderer")
    }
}
