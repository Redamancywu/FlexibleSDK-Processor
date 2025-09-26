package com.example.modular.core

import com.flexiblesdk.processor.annotation.ServiceModule
import com.flexiblesdk.processor.annotation.ServiceProvider

@ServiceModule(
    name = "CoreModule",
    version = "1.0.0",
    description = "Core infrastructure services",
    priority = 100
)
class CoreModule

/**
 * Configuration service interface
 */
interface ConfigService {
    fun getProperty(key: String): String?
    fun setProperty(key: String, value: String)
    fun getAllProperties(): Map<String, String>
}

/**
 * Logging service interface
 */
interface LoggingService {
    fun info(message: String)
    fun warn(message: String)
    fun error(message: String)
}

@ServiceProvider(
    interfaces = [ConfigService::class],
    priority = 200,
    singleton = true
)
class ConfigServiceImpl : ConfigService {
    private val properties = mutableMapOf<String, String>()
    
    init {
        // Default configuration
        properties["app.name"] = "Modular Architecture Example"
        properties["app.version"] = "1.0.0"
        properties["database.url"] = "jdbc:h2:mem:testdb"
    }
    
    override fun getProperty(key: String): String? = properties[key]
    
    override fun setProperty(key: String, value: String) {
        properties[key] = value
    }
    
    override fun getAllProperties(): Map<String, String> = properties.toMap()
}

@ServiceProvider(
    interfaces = [LoggingService::class],
    priority = 200,
    singleton = true
)
class LoggingServiceImpl : LoggingService {
    override fun info(message: String) {
        println("[INFO] $message")
    }
    
    override fun warn(message: String) {
        println("[WARN] $message")
    }
    
    override fun error(message: String) {
        println("[ERROR] $message")
    }
}