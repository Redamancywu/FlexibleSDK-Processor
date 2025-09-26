package com.example.spring.services

import com.example.spring.models.User
import com.flexiblesdk.processor.annotation.ServiceProvider
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong

/**
 * User service interface
 */
interface UserService {
    fun createUser(name: String, email: String): User
    fun getUserById(id: String): User?
    fun getAllUsers(): List<User>
    fun updateUser(id: String, name: String?, email: String?, active: Boolean?): User?
    fun deleteUser(id: String): Boolean
    fun getUserCount(): Long
}

/**
 * User service implementation using FlexibleSDK Processor
 * This service is registered with FlexibleSDK and can be injected into Spring components
 */
@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    singleton = true,
    dependencies = ["NotificationService"]
)
@Component
class UserServiceImpl(
    private val notificationService: NotificationService
) : UserService {
    
    private val users = ConcurrentHashMap<String, User>()
    private val idGenerator = AtomicLong(1)
    
    init {
        // Initialize with some sample data
        val sampleUsers = listOf(
            User("1", "John Doe", "john@example.com", LocalDateTime.now().minusDays(30)),
            User("2", "Jane Smith", "jane@example.com", LocalDateTime.now().minusDays(15)),
            User("3", "Bob Johnson", "bob@example.com", LocalDateTime.now().minusDays(7))
        )
        
        sampleUsers.forEach { user ->
            users[user.id] = user
            idGenerator.set(maxOf(idGenerator.get(), user.id.toLongOrNull() ?: 0))
        }
    }
    
    override fun createUser(name: String, email: String): User {
        val id = idGenerator.incrementAndGet().toString()
        val user = User(id, name, email)
        users[id] = user
        
        // Send notification using injected service
        notificationService.sendWelcomeNotification(user)
        
        return user
    }
    
    override fun getUserById(id: String): User? {
        return users[id]
    }
    
    override fun getAllUsers(): List<User> {
        return users.values.sortedBy { it.createdAt }
    }
    
    override fun updateUser(id: String, name: String?, email: String?, active: Boolean?): User? {
        val existingUser = users[id] ?: return null
        
        val updatedUser = existingUser.copy(
            name = name ?: existingUser.name,
            email = email ?: existingUser.email,
            active = active ?: existingUser.active
        )
        
        users[id] = updatedUser
        
        // Send update notification
        notificationService.sendUpdateNotification(updatedUser)
        
        return updatedUser
    }
    
    override fun deleteUser(id: String): Boolean {
        val user = users.remove(id)
        if (user != null) {
            notificationService.sendDeletionNotification(user)
            return true
        }
        return false
    }
    
    override fun getUserCount(): Long {
        return users.size.toLong()
    }
}