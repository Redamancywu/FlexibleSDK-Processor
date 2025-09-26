package com.example.modular

import com.example.modular.registry.ModularServiceRegistry
import com.example.modular.core.*
import com.example.modular.data.*
import com.example.modular.business.*

/**
 * Main class demonstrating modular architecture with FlexibleSDK Processor
 */
fun main() {
    println("=== FlexibleSDK Processor Modular Architecture Example ===")
    println()
    
    // Check if services are registered
    val databaseServiceInfo = ModularServiceRegistry.getServicesByInterface("com.example.modular.data.DatabaseService").firstOrNull()
    val userServiceInfo = ModularServiceRegistry.getServicesByInterface("com.example.modular.business.UserService").firstOrNull()
    val orderServiceInfo = ModularServiceRegistry.getServicesByInterface("com.example.modular.business.OrderService").firstOrNull()
    val loggingServiceInfo = ModularServiceRegistry.getServicesByInterface("com.example.modular.core.LoggingService").firstOrNull()
    
    if (databaseServiceInfo != null && userServiceInfo != null && orderServiceInfo != null && loggingServiceInfo != null) {
        println("✓ All services found in registry")
        
        // Show service information
        println("\n--- Service Information ---")
        println("Database Service: ${databaseServiceInfo.className}")
        println("  Interfaces: ${databaseServiceInfo.interfaces.joinToString(", ")}")
        println("  Dependencies: ${databaseServiceInfo.dependencies.joinToString(", ")}")
        
        println("User Service: ${userServiceInfo.className}")
        println("  Interfaces: ${userServiceInfo.interfaces.joinToString(", ")}")
        println("  Dependencies: ${userServiceInfo.dependencies.joinToString(", ")}")
        
        println("Order Service: ${orderServiceInfo.className}")
        println("  Interfaces: ${orderServiceInfo.interfaces.joinToString(", ")}")
        println("  Dependencies: ${orderServiceInfo.dependencies.joinToString(", ")}")
        
        println("Logging Service: ${loggingServiceInfo.className}")
        println("  Interfaces: ${loggingServiceInfo.interfaces.joinToString(", ")}")
        println("  Dependencies: ${loggingServiceInfo.dependencies.joinToString(", ")}")
        
        // Show module information
        println("\n--- Module Information ---")
        val allModules = ModularServiceRegistry.getAllModules()
        allModules.sortedByDescending { it.priority }.forEach { module: com.example.modular.registry.ModuleInfo ->
            println("Module: ${module.name} v${module.version}")
            println("  Description: ${module.description}")
            println("  Priority: ${module.priority}")
            if (module.dependencies.isNotEmpty()) {
                println("  Dependencies: ${module.dependencies.joinToString(", ")}")
            }
            println()
        }
        
        // Show service dependency graph
        println("--- Service Registry Information ---")
        val allServices = ModularServiceRegistry.getAllServices()
        println("Total services registered: ${allServices.size}")
        
        val servicesByPriority = allServices.sortedByDescending { it.priority }
        servicesByPriority.forEach { service: com.example.modular.registry.ServiceInfo ->
            println("Service: ${service.className}")
            println("  Interfaces: ${service.interfaces.joinToString(", ")}")
            println("  Priority: ${service.priority}")
            if (service.dependencies.isNotEmpty()) {
                println("  Dependencies: ${service.dependencies.joinToString(", ")}")
            }
            println("  Singleton: ${service.singleton}")
            println()
        }
        
    } else {
        println("✗ Some services not found in registry")
        println("Make sure to build the project first: ./gradlew build")
    }
    
    println("=== Example completed ===")
}