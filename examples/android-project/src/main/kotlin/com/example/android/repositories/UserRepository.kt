package com.example.android.repositories

import com.example.android.models.CreateUserData
import com.example.android.models.UpdateUserData
import com.example.android.models.User
import com.example.android.registry.AndroidServiceRegistry
import com.example.android.services.UserService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * User repository that acts as a bridge between ViewModels and Services
 * This demonstrates how to use FlexibleSDK services in repository pattern
 */
class UserRepository {
    
    private val userService: UserService? by lazy {
        AndroidServiceRegistry.getService(UserService::class)
    }
    
    /**
     * Get all users as a Flow
     */
    fun getAllUsersFlow(): Flow<List<User>> = flow {
        userService?.let { service ->
            emit(service.getAllUsers())
        } ?: emit(emptyList())
    }
    
    /**
     * Create a new user
     */
    suspend fun createUser(userData: CreateUserData): Result<User> {
        return try {
            userService?.let { service ->
                val user = service.createUser(userData)
                Result.success(user)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user by ID
     */
    suspend fun getUserById(id: String): Result<User?> {
        return try {
            userService?.let { service ->
                val user = service.getUserById(id)
                Result.success(user)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Update user
     */
    suspend fun updateUser(id: String, updateData: UpdateUserData): Result<User?> {
        return try {
            userService?.let { service ->
                val user = service.updateUser(id, updateData)
                Result.success(user)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Delete user
     */
    suspend fun deleteUser(id: String): Result<Boolean> {
        return try {
            userService?.let { service ->
                val result = service.deleteUser(id)
                Result.success(result)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Search users
     */
    suspend fun searchUsers(query: String): Result<List<User>> {
        return try {
            userService?.let { service ->
                val users = service.searchUsers(query)
                Result.success(users)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get user count
     */
    suspend fun getUserCount(): Result<Int> {
        return try {
            userService?.let { service ->
                val count = service.getUserCount()
                Result.success(count)
            } ?: Result.failure(Exception("UserService not available"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Check if service is available
     */
    fun isServiceAvailable(): Boolean {
        return userService != null
    }
}