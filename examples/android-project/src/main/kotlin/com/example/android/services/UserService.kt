package com.example.android.services

import com.example.android.models.CreateUserData
import com.example.android.models.UpdateUserData
import com.example.android.models.User
import com.flexiblesdk.processor.annotation.ServiceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * User service interface
 */
interface UserService {
    suspend fun createUser(userData: CreateUserData): User
    suspend fun getUserById(id: String): User?
    suspend fun getAllUsers(): List<User>
    suspend fun updateUser(id: String, updateData: UpdateUserData): User?
    suspend fun deleteUser(id: String): Boolean
    suspend fun getUserCount(): Int
    suspend fun searchUsers(query: String): List<User>
}

/**
 * User service implementation
 */
@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    singleton = true,
    dependencies = ["DatabaseService", "PreferenceService"]
)
class UserServiceImpl(
    private val databaseService: DatabaseService,
    private val preferenceService: PreferenceService
) : UserService {
    
    private val userDao by lazy { databaseService.getDatabase().userDao() }
    
    override suspend fun createUser(userData: CreateUserData): User = withContext(Dispatchers.IO) {
        val user = User(
            id = UUID.randomUUID().toString(),
            name = userData.name,
            email = userData.email
        )
        
        userDao.insertUser(user)
        
        // Update user count in preferences
        val currentCount = preferenceService.getInt("user_count", 0)
        preferenceService.setInt("user_count", currentCount + 1)
        
        user
    }
    
    override suspend fun getUserById(id: String): User? = withContext(Dispatchers.IO) {
        userDao.getUserById(id)
    }
    
    override suspend fun getAllUsers(): List<User> = withContext(Dispatchers.IO) {
        userDao.getAllUsers()
    }
    
    override suspend fun updateUser(id: String, updateData: UpdateUserData): User? = withContext(Dispatchers.IO) {
        val existingUser = userDao.getUserById(id) ?: return@withContext null
        
        val updatedUser = existingUser.copy(
            name = updateData.name ?: existingUser.name,
            email = updateData.email ?: existingUser.email,
            isActive = updateData.isActive ?: existingUser.isActive
        )
        
        userDao.updateUser(updatedUser)
        updatedUser
    }
    
    override suspend fun deleteUser(id: String): Boolean = withContext(Dispatchers.IO) {
        val user = userDao.getUserById(id)
        if (user != null) {
            userDao.deleteUser(user)
            
            // Update user count in preferences
            val currentCount = preferenceService.getInt("user_count", 0)
            preferenceService.setInt("user_count", maxOf(0, currentCount - 1))
            
            true
        } else {
            false
        }
    }
    
    override suspend fun getUserCount(): Int = withContext(Dispatchers.IO) {
        userDao.getUserCount()
    }
    
    override suspend fun searchUsers(query: String): List<User> = withContext(Dispatchers.IO) {
        val allUsers = userDao.getAllUsers()
        allUsers.filter { user ->
            user.name.contains(query, ignoreCase = true) || 
            user.email.contains(query, ignoreCase = true)
        }
    }
}