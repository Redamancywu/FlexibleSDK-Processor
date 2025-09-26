package com.example.basic

import com.flexiblesdk.processor.annotation.ServiceProvider

/**
 * Simple implementation of UserService
 */
@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    singleton = true
)
class UserServiceImpl : UserService {
    
    private val users = mutableMapOf<String, User>()
    private var nextId = 1
    
    init {
        // Add some sample users
        createUser("John Doe", "john@example.com")
        createUser("Jane Smith", "jane@example.com")
    }
    
    override fun getUser(id: String): User {
        return users[id] ?: throw IllegalArgumentException("User not found: $id")
    }
    
    override fun createUser(name: String, email: String): User {
        val id = (nextId++).toString()
        val user = User(id, name, email)
        users[id] = user
        println("Created user: $user")
        return user
    }
    
    override fun getAllUsers(): List<User> {
        return users.values.toList()
    }
}