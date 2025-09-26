package com.example.basic

/**
 * Simple user service interface
 */
interface UserService {
    fun getUser(id: String): User
    fun createUser(name: String, email: String): User
    fun getAllUsers(): List<User>
}

/**
 * Simple user data class
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)