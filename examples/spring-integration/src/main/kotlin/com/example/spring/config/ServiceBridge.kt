package com.example.spring.config

import com.example.spring.registry.SpringServiceRegistry
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory
import kotlin.reflect.KClass

/**
 * Bridge between Spring's dependency injection and FlexibleSDK's service registry
 * This component allows seamless integration between the two systems
 */
@Component
class ServiceBridge @Autowired constructor(
    private val applicationContext: ApplicationContext
) {
    
    private val logger = LoggerFactory.getLogger(ServiceBridge::class.java)
    
    /**
     * Get a service from FlexibleSDK registry, with fallback to Spring context
     */
    inline fun <reified T : Any> getService(): T? {
        return getService(T::class)
    }
    
    /**
     * Get a service from FlexibleSDK registry, with fallback to Spring context
     */
    fun <T : Any> getService(serviceClass: KClass<T>): T? {
        // First try FlexibleSDK registry
        val flexibleService = SpringServiceRegistry.getService(serviceClass)
        if (flexibleService != null) {
            logger.debug("Service ${serviceClass.simpleName} found in FlexibleSDK registry")
            return flexibleService
        }
        
        // Fallback to Spring context
        return try {
            val springBean = applicationContext.getBean(serviceClass.java)
            logger.debug("Service ${serviceClass.simpleName} found in Spring context")
            springBean
        } catch (e: Exception) {
            logger.debug("Service ${serviceClass.simpleName} not found in either registry")
            null
        }
    }
    
    /**
     * Get all services of a specific type from both registries
     */
    inline fun <reified T : Any> getAllServices(): List<T> {
        return getAllServices(T::class)
    }
    
    /**
     * Get all services of a specific type from both registries
     */
    fun <T : Any> getAllServices(serviceClass: KClass<T>): List<T> {
        val services = mutableListOf<T>()
        
        // Get from FlexibleSDK registry
        val flexibleService = SpringServiceRegistry.getService(serviceClass)
        if (flexibleService != null) {
            services.add(flexibleService)
        }
        
        // Get from Spring context
        try {
            val springBeans = applicationContext.getBeansOfType(serviceClass.java).values
            services.addAll(springBeans)
        } catch (e: Exception) {
            logger.debug("No Spring beans found for ${serviceClass.simpleName}")
        }
        
        return services.distinct()
    }
    
    /**
     * Check if a service is available in either registry
     */
    inline fun <reified T : Any> hasService(): Boolean {
        return hasService(T::class)
    }
    
    /**
     * Check if a service is available in either registry
     */
    fun <T : Any> hasService(serviceClass: KClass<T>): Boolean {
        return getService(serviceClass) != null
    }
    
    /**
     * Get service registry statistics
     */
    fun getRegistryStats(): RegistryStats {
        val flexibleServices = SpringServiceRegistry.getAllServices()
        val flexibleModules = SpringServiceRegistry.getAllModules()
        
        val springBeans = applicationContext.beanDefinitionNames.size
        
        return RegistryStats(
            flexibleSDKServices = flexibleServices.size,
            flexibleSDKModules = flexibleModules.size,
            springBeans = springBeans,
            totalManagedComponents = flexibleServices.size + springBeans
        )
    }
}

/**
 * Statistics about the service registries
 */
data class RegistryStats(
    val flexibleSDKServices: Int,
    val flexibleSDKModules: Int,
    val springBeans: Int,
    val totalManagedComponents: Int
)