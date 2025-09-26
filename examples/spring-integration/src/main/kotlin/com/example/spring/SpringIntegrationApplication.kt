package com.example.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Spring Boot application demonstrating FlexibleSDK Processor integration
 */
@SpringBootApplication
class SpringIntegrationApplication

fun main(args: Array<String>) {
    runApplication<SpringIntegrationApplication>(*args)
}