package com.example.spring.controllers

import com.example.spring.config.ServiceBridge
import com.example.spring.config.ServiceRegistryInfo
import com.example.spring.models.CreateUserRequest
import com.example.spring.models.UpdateUserRequest
import com.example.spring.models.User
import com.example.spring.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

/**
 * REST controller for user management
 * Demonstrates integration between Spring Web and FlexibleSDK services
 */
@RestController
@RequestMapping("/api/users")
class UserController @Autowired constructor(
    private val serviceBridge: ServiceBridge,
    private val serviceRegistryInfo: ServiceRegistryInfo
) {
    
    private val logger = LoggerFactory.getLogger(UserController::class.java)
    
    // Get UserService through the service bridge
    private val userService: UserService? by lazy {
        serviceBridge.getService<UserService>()
    }
    
    @GetMapping
    fun getAllUsers(): ResponseEntity<List<User>> {
        return userService?.let { service ->
            logger.info("Getting all users")
            ResponseEntity.ok(service.getAllUsers())
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: String): ResponseEntity<User> {
        return userService?.let { service ->
            logger.info("Getting user by id: $id")
            val user = service.getUserById(id)
            if (user != null) {
                ResponseEntity.ok(user)
            } else {
                ResponseEntity.notFound().build()
            }
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<User> {
        return userService?.let { service ->
            logger.info("Creating user: ${request.name}")
            try {
                val user = service.createUser(request.name, request.email)
                ResponseEntity.status(HttpStatus.CREATED).body(user)
            } catch (e: Exception) {
                logger.error("Error creating user", e)
                ResponseEntity.badRequest().build()
            }
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @PutMapping("/{id}")
    fun updateUser(
        @PathVariable id: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<User> {
        return userService?.let { service ->
            logger.info("Updating user: $id")
            val updatedUser = service.updateUser(id, request.name, request.email, request.active)
            if (updatedUser != null) {
                ResponseEntity.ok(updatedUser)
            } else {
                ResponseEntity.notFound().build()
            }
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @DeleteMapping("/{id}")
    fun deleteUser(@PathVariable id: String): ResponseEntity<Void> {
        return userService?.let { service ->
            logger.info("Deleting user: $id")
            val deleted = service.deleteUser(id)
            if (deleted) {
                ResponseEntity.noContent().build()
            } else {
                ResponseEntity.notFound().build()
            }
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @GetMapping("/count")
    fun getUserCount(): ResponseEntity<Map<String, Long>> {
        return userService?.let { service ->
            ResponseEntity.ok(mapOf("count" to service.getUserCount()))
        } ?: ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build()
    }
    
    @GetMapping("/registry/info")
    fun getRegistryInfo(): ResponseEntity<Map<String, Any>> {
        val stats = serviceBridge.getRegistryStats()
        
        return ResponseEntity.ok(mapOf(
            "flexibleSDK" to mapOf(
                "services" to stats.flexibleSDKServices,
                "modules" to stats.flexibleSDKModules,
                "registryClass" to serviceRegistryInfo.registryClass
            ),
            "spring" to mapOf(
                "beans" to stats.springBeans
            ),
            "total" to mapOf(
                "managedComponents" to stats.totalManagedComponents
            ),
            "userServiceAvailable" to serviceBridge.hasService<UserService>()
        ))
    }
    
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<Map<String, Any>> {
        val isHealthy = userService != null
        val status = if (isHealthy) "UP" else "DOWN"
        
        return ResponseEntity.ok(mapOf(
            "status" to status,
            "userService" to mapOf(
                "available" to isHealthy,
                "class" to (userService?.javaClass?.simpleName ?: "N/A")
            ),
            "timestamp" to System.currentTimeMillis()
        ))
    }
}