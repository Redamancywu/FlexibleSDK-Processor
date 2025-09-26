package com.example.basic

import com.example.basic.registry.ServiceRegistry

/**
 * Main class demonstrating basic usage of FlexibleSDK Processor
 */
fun main() {
    println("=== FlexibleSDK Processor Basic Usage Example ===")
    println()
    
    // Get the user service from the generated registry
    val userServiceInfos = ServiceRegistry.getServicesByInterface("com.example.basic.UserService")
    
    if (userServiceInfos.isNotEmpty()) {
        println("✓ UserService found in registry")
        
        // Create an instance of the service
        val userServiceInfo = userServiceInfos.first()
        val userService = Class.forName(userServiceInfo.className).getDeclaredConstructor().newInstance() as UserService
        
        // Get all users
        println("\n--- All Users ---")
        val allUsers = userService.getAllUsers()
        allUsers.forEach { user ->
            println("User: ${user.name} (${user.email}) - ID: ${user.id}")
        }
        
        // Create a new user
        println("\n--- Creating New User ---")
        val newUser = userService.createUser("Bob Wilson", "bob@example.com")
        
        // Get a specific user
        println("\n--- Getting Specific User ---")
        try {
            val retrievedUser = userService.getUser(newUser.id)
            println("Retrieved user: ${retrievedUser.name} (${retrievedUser.email})")
        } catch (e: Exception) {
            println("Error: ${e.message}")
        }
        
        // Show all services in registry
        println("\n--- Service Registry Information ---")
        val allServices = ServiceRegistry.getAllServices()
        println("Total services registered: ${allServices.size}")
        for (info in allServices) {
            println("Service: ${info.className}, Priority: ${info.priority}")
        }
        
    } else {
        println("✗ UserService not found in registry")
        println("Make sure to build the project first: ./gradlew build")
    }
    
    println("\n=== Example completed ===")
}