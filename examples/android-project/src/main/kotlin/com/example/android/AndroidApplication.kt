package com.example.android

import android.app.Application
import android.util.Log
import com.example.android.registry.AndroidServiceRegistry

/**
 * Android Application class with FlexibleSDK initialization
 */
class AndroidApplication : Application() {
    
    companion object {
        private const val TAG = "AndroidApplication"
    }
    
    override fun onCreate() {
        super.onCreate()
        
        Log.d(TAG, "Initializing FlexibleSDK Processor...")
        
        try {
            // Initialize FlexibleSDK service registry
            val serviceCount = AndroidServiceRegistry.getAllServices().size
            val moduleCount = AndroidServiceRegistry.getAllModules().size
            
            Log.i(TAG, "FlexibleSDK initialized successfully:")
            Log.i(TAG, "  - Services registered: $serviceCount")
            Log.i(TAG, "  - Modules loaded: $moduleCount")
            
            // Log registered services in debug mode
            if (BuildConfig.DEBUG) {
                AndroidServiceRegistry.getAllServices().values.forEach { service ->
                    Log.d(TAG, "Registered service: ${service.implementationClass}")
                    Log.d(TAG, "  Interfaces: ${service.interfaces.joinToString(", ")}")
                    Log.d(TAG, "  Priority: ${service.priority}")
                    Log.d(TAG, "  Singleton: ${service.singleton}")
                    if (service.dependencies.isNotEmpty()) {
                        Log.d(TAG, "  Dependencies: ${service.dependencies.joinToString(", ")}")
                    }
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FlexibleSDK", e)
        }
    }
}