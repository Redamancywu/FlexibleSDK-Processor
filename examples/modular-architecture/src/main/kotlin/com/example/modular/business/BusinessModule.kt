package com.example.modular.business

import com.flexiblesdk.processor.annotation.ServiceModule
import com.flexiblesdk.processor.annotation.ServiceProvider
import com.example.modular.core.LoggingService
import com.example.modular.data.*

@ServiceModule(
    name = "UserModule",
    version = "1.0.0",
    description = "User management services",
    priority = 50,
    dependencies = ["DatabaseModule"]
)
class UserModule

@ServiceModule(
    name = "OrderModule",
    version = "1.0.0",
    description = "Order management services",
    priority = 50,
    dependencies = ["DatabaseModule", "UserModule"]
)
class OrderModule

/**
 * User service interface
 */
interface UserService {
    fun getUserById(id: String): User?
    fun getAllUsers(): List<User>
    fun createUser(name: String, email: String): User
    fun updateUser(user: User): User
    fun deleteUser(id: String): Boolean
}

/**
 * Order service interface
 */
interface OrderService {
    fun getOrderById(id: String): Order?
    fun getOrdersByUserId(userId: String): List<Order>
    fun createOrder(userId: String, productName: String, amount: Double): Order
    fun deleteOrder(id: String): Boolean
    fun getUserOrderSummary(userId: String): OrderSummary
}

data class OrderSummary(
    val userId: String,
    val totalOrders: Int,
    val totalAmount: Double,
    val orders: List<Order>
)

@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    dependencies = [UserRepository::class, LoggingService::class],
    singleton = true
)
class UserServiceImpl(
    private val userRepository: UserRepository,
    private val loggingService: LoggingService
) : UserService {
    
    override fun getUserById(id: String): User? {
        loggingService.info("Getting user by id: $id")
        return userRepository.findById(id)
    }
    
    override fun getAllUsers(): List<User> {
        loggingService.info("Getting all users")
        return userRepository.findAll()
    }
    
    override fun createUser(name: String, email: String): User {
        loggingService.info("Creating user: $name")
        val user = User("", name, email)
        return userRepository.save(user)
    }
    
    override fun updateUser(user: User): User {
        loggingService.info("Updating user: ${user.name}")
        return userRepository.save(user)
    }
    
    override fun deleteUser(id: String): Boolean {
        loggingService.info("Deleting user: $id")
        return userRepository.deleteById(id)
    }
}

@ServiceProvider(
    interfaces = [OrderService::class],
    priority = 100,
    dependencies = [OrderRepository::class, UserRepository::class, LoggingService::class],
    singleton = true
)
class OrderServiceImpl(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val loggingService: LoggingService
) : OrderService {
    
    override fun getOrderById(id: String): Order? {
        loggingService.info("Getting order by id: $id")
        return orderRepository.findById(id)
    }
    
    override fun getOrdersByUserId(userId: String): List<Order> {
        loggingService.info("Getting orders for user: $userId")
        return orderRepository.findByUserId(userId)
    }
    
    override fun createOrder(userId: String, productName: String, amount: Double): Order {
        loggingService.info("Creating order for user $userId: $productName")
        
        // Validate user exists
        val user = userRepository.findById(userId)
            ?: throw IllegalArgumentException("User not found: $userId")
        
        val order = Order("", userId, productName, amount)
        return orderRepository.save(order)
    }
    
    override fun deleteOrder(id: String): Boolean {
        loggingService.info("Deleting order: $id")
        return orderRepository.deleteById(id)
    }
    
    override fun getUserOrderSummary(userId: String): OrderSummary {
        loggingService.info("Getting order summary for user: $userId")
        
        val orders = orderRepository.findByUserId(userId)
        val totalAmount = orders.sumOf { it.amount }
        
        return OrderSummary(
            userId = userId,
            totalOrders = orders.size,
            totalAmount = totalAmount,
            orders = orders
        )
    }
}