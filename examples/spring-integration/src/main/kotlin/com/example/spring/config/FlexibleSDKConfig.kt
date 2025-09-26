package com.example.spring.config

import com.example.spring.registry.SpringServiceRegistry
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.slf4j.LoggerFactory
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

/**
 * Configuration class for FlexibleSDK integration with Spring
 */
@Configuration
class FlexibleSDKConfig {
    
    private val logger = LoggerFactory.getLogger(FlexibleSDKConfig::class.java)
    
    @PostConstruct
    fun initializeFlexibleSDK() {
        logger.info("Initializing FlexibleSDK Processor...")
        
        // Initialize the service registry
        try {
            val serviceCount = SpringServiceRegistry.getAllServices().size
            val moduleCount = SpringServiceRegistry.getAllModules().size
            
            logger.info("FlexibleSDK initialized successfully:")
            logger.info("  - Services registered: $serviceCount")
            logger.info("  - Modules loaded: $moduleCount")
            
            // Log registered services
            SpringServiceRegistry.getAllServices().values.forEach { service ->
                logger.debug("Registered service: ${service.implementationClass}")
                logger.debug("  Interfaces: ${service.interfaces.joinToString(", ")}")
                logger.debug("  Priority: ${service.priority}")
                logger.debug("  Singleton: ${service.singleton}")
                if (service.dependencies.isNotEmpty()) {
                    logger.debug("  Dependencies: ${service.dependencies.joinToString(", ")}")
                }
            }
            
        } catch (e: Exception) {
            logger.error("Failed to initialize FlexibleSDK", e)
            throw e
        }
    }
    
    @PreDestroy
    fun cleanupFlexibleSDK() {
        logger.info("Cleaning up FlexibleSDK resources...")
        // Perform any necessary cleanup
    }
    
    /**
     * Bean to provide access to the FlexibleSDK service registry
     */
    @Bean
    @DependsOn("flexibleSDKConfig")
    fun serviceRegistryInfo(): ServiceRegistryInfo {
        return ServiceRegistryInfo(
            totalServices = SpringServiceRegistry.getAllServices().size,
            totalModules = SpringServiceRegistry.getAllModules().size,
            registryClass = SpringServiceRegistry::class.java.simpleName
        )
    }
}

/**
 * Information about the service registry
 */
data class ServiceRegistryInfo(
    val totalServices: Int,
    val totalModules: Int,
    val registryClass: String
)