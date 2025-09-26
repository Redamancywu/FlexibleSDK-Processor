package com.example.services

import com.flexiblesdk.processor.annotation.ServiceProvider

/**
 * 用户服务接口
 */
interface UserService {
    fun getUserById(id: String): User?
    fun createUser(user: User): User
    fun updateUser(user: User): User
    fun deleteUser(id: String): Boolean
}

/**
 * 用户服务实现
 */
@ServiceProvider(
    interfaces = [UserService::class],
    priority = 1,
    module = "user"
)
class UserServiceImpl : UserService {
    
    override fun getUserById(id: String): User? {
        // 模拟数据库查询
        return User(id, "User $id", "user$id@example.com")
    }
    
    override fun createUser(user: User): User {
        // 模拟创建用户
        println("Creating user: ${user.name}")
        return user
    }
    
    override fun updateUser(user: User): User {
        // 模拟更新用户
        println("Updating user: ${user.name}")
        return user
    }
    
    override fun deleteUser(id: String): Boolean {
        // 模拟删除用户
        println("Deleting user: $id")
        return true
    }
}

/**
 * 用户数据类
 */
data class User(
    val id: String,
    val name: String,
    val email: String
)