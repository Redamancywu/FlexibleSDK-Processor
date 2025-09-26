package com.example.modular.data

import com.flexiblesdk.processor.annotation.ServiceModule
import com.flexiblesdk.processor.annotation.ServiceProvider
import com.example.modular.core.ConfigService
import com.example.modular.core.LoggingService

@ServiceModule(
    name = "DatabaseModule",
    version = "1.0.0",
    description = "Data access services",
    priority = 80,
    dependencies = ["CoreModule"]
)
class DatabaseModule

/**
 * Database connection interface
 */
interface DatabaseService {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnected(): Boolean
    fun executeQuery(sql: String): List<Map<String, Any>>
}

/**
 * User repository interface
 */
interface UserRepository {
    fun findById(id: String): User?
    fun findAll(): List<User>
    fun save(user: User): User
    fun deleteById(id: String): Boolean
}

/**
 * Order repository interface
 */
interface OrderRepository {
    fun findById(id: String): Order?
    fun findByUserId(userId: String): List<Order>
    fun save(order: Order): Order
    fun deleteById(id: String): Boolean
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Order(
    val id: String,
    val userId: String,
    val productName: String,
    val amount: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@ServiceProvider(
    interfaces = [DatabaseService::class],
    priority = 150,
    dependencies = [ConfigService::class, LoggingService::class],
    singleton = true
)
class DatabaseServiceImpl(
    private val configService: ConfigService,
    private val loggingService: LoggingService
) : DatabaseService {
    
    private var connected = false
    
    override fun connect(): Boolean {
        val dbUrl = configService.getProperty("database.url")
        loggingService.info("Connecting to database: $dbUrl")
        connected = true
        loggingService.info("Database connected successfully")
        return true
    }
    
    override fun disconnect(): Boolean {
        loggingService.info("Disconnecting from database")
        connected = false
        return true
    }
    
    override fun isConnected(): Boolean = connected
    
    override fun executeQuery(sql: String): List<Map<String, Any>> {
        loggingService.info("Executing query: $sql")
        // Mock implementation
        return emptyList()
    }
}

@ServiceProvider(
    interfaces = [UserRepository::class],
    priority = 120,
    dependencies = [DatabaseService::class, LoggingService::class],
    singleton = true
)
class UserRepositoryImpl(
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService
) : UserRepository {
    
    private val users = mutableMapOf<String, User>()
    private var nextId = 1
    
    init {
        // Sample data
        save(User("1", "John Doe", "john@example.com"))
        save(User("2", "Jane Smith", "jane@example.com"))
    }
    
    override fun findById(id: String): User? {
        loggingService.info("Finding user by id: $id")
        return users[id]
    }
    
    override fun findAll(): List<User> {
        loggingService.info("Finding all users")
        return users.values.toList()
    }
    
    override fun save(user: User): User {
        val savedUser = if (user.id.isEmpty()) {
            user.copy(id = (nextId++).toString())
        } else {
            user
        }
        users[savedUser.id] = savedUser
        loggingService.info("Saved user: ${savedUser.name}")
        return savedUser
    }
    
    override fun deleteById(id: String): Boolean {
        loggingService.info("Deleting user by id: $id")
        return users.remove(id) != null
    }
}

@ServiceProvider(
    interfaces = [OrderRepository::class],
    priority = 120,
    dependencies = [DatabaseService::class, LoggingService::class],
    singleton = true
)
class OrderRepositoryImpl(
    private val databaseService: DatabaseService,
    private val loggingService: LoggingService
) : OrderRepository {
    
    private val orders = mutableMapOf<String, Order>()
    private var nextId = 1
    
    init {
        // Sample data
        save(Order("1", "1", "Laptop", 999.99))
        save(Order("2", "1", "Mouse", 29.99))
        save(Order("3", "2", "Keyboard", 79.99))
    }
    
    override fun findById(id: String): Order? {
        loggingService.info("Finding order by id: $id")
        return orders[id]
    }
    
    override fun findByUserId(userId: String): List<Order> {
        loggingService.info("Finding orders by user id: $userId")
        return orders.values.filter { it.userId == userId }
    }
    
    override fun save(order: Order): Order {
        val savedOrder = if (order.id.isEmpty()) {
            order.copy(id = (nextId++).toString())
        } else {
            order
        }
        orders[savedOrder.id] = savedOrder
        loggingService.info("Saved order: ${savedOrder.productName} for user ${savedOrder.userId}")
        return savedOrder
    }
    
    override fun deleteById(id: String): Boolean {
        loggingService.info("Deleting order by id: $id")
        return orders.remove(id) != null
    }
}