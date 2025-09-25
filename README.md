# FlexibleSDK Processor

A flexible SDK processor for Kotlin annotation processing and code generation.

## Features

- Kotlin annotation processing with KSP (Kotlin Symbol Processing)
- Automatic code generation for service registries
- Gradle plugin integration
- Support for flexible SDK architectures

## Installation

### Method 1: Using Gradle Plugin (Recommended)

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("com.flexiblesdk.processor") version "0.0.3"
}
```

Configure the plugin:

```kotlin
flexibleSDK {
    serviceRegistryPackage.set("com.yourpackage.registry")
    serviceRegistryClassName.set("ServiceRegistry")
    enableDebugLogging.set(true)
    logLevel.set("INFO")
}
```

### Method 2: Manual KSP Configuration

If you prefer manual configuration, add the repositories and dependencies:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

plugins {
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

dependencies {
    // KSP processor
    ksp("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:0.0.3")
    
    // Compile-time annotations
    compileOnly("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:0.0.3")
    
    // Plugin automatically adds these dependencies:
    // ksp("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:0.0.3")
    // compileOnly("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:0.0.3")
}

ksp {
    arg("serviceRegistryPackage", "com.yourpackage.registry")
    arg("serviceRegistryClassName", "ServiceRegistry")
    arg("enableDebugLogging", "true")
    arg("logLevel", "INFO")
}
```

### Version Configuration

You can specify the processor version in `gradle.properties`:

```properties
flexibleSDKProcessorVersion=0.0.3
```

## Usage

### 1. Define Service Providers

Use `@ServiceProvider` annotation to mark your service implementations:

```kotlin
import com.flexiblesdk.processor.annotation.ServiceProvider

interface UserService {
    fun getUser(id: String): User
}

@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    dependencies = ["DatabaseService"]
)
class UserServiceImpl : UserService {
    override fun getUser(id: String): User {
        // Implementation
    }
}
```

### 2. Define Service Modules

Use `@ServiceModule` annotation to group related services:

```kotlin
import com.flexiblesdk.processor.annotation.ServiceModule

@ServiceModule(
    name = "UserModule",
    version = "1.0.0",
    description = "User management services",
    priority = 50,
    dependencies = ["CoreModule"]
)
class UserModule
```

### 3. Generated Service Registry

The processor will generate a service registry class (e.g., `ServiceRegistry.kt`):

```kotlin
// Generated code - do not modify
package com.yourpackage.registry

object ServiceRegistry {
    fun getService(interfaceClass: Class<*>): Any? { /* ... */ }
    fun getModule(name: String): ModuleInfo? { /* ... */ }
    fun getServicesByInterface(interfaceClass: Class<*>): List<ServiceInfo> { /* ... */ }
    fun getAllServices(): Map<String, ServiceInfo> { /* ... */ }
    fun getAllModules(): Map<String, ModuleInfo> { /* ... */ }
}
```

### 4. Using the Service Registry

```kotlin
// Get a service by interface
val userService = ServiceRegistry.getService(UserService::class.java) as? UserService

// Get all services implementing an interface
val userServices = ServiceRegistry.getServicesByInterface(UserService::class.java)

// Get module information
val userModule = ServiceRegistry.getModule("UserModule")
```

### Configuration Options

All available configuration options:

```kotlin
flexibleSDK {
    // Required: Package for generated service registry
    serviceRegistryPackage.set("com.yourpackage.registry")
    
    // Required: Class name for generated service registry
    serviceRegistryClassName.set("ServiceRegistry")
    
    // Optional: Enable debug logging (default: false)
    enableDebugLogging.set(true)
    
    // Optional: Log level (DEBUG, INFO, WARN, ERROR, default: INFO)
    logLevel.set("INFO")
    
    // Optional: Enable incremental compilation (default: true)
    enableIncremental.set(true)
    
    // Optional: Validate service dependencies (default: true)
    validateDependencies.set(true)
    
    // Optional: Generate documentation (default: false)
    generateDocumentation.set(false)
    
    // Optional: Exclude packages from processing
    excludePackages.set(listOf("com.example.test"))
    
    // Optional: Include only specific packages
    includePackages.set(listOf("com.yourpackage.services"))
    
    // Optional: Show progress during processing (default: false)
    showProgress.set(true)
    
    // Optional: Show performance statistics (default: false)
    showPerformanceStats.set(true)
    
    // Optional: Show detailed validation results (default: false)
    showDetailedValidation.set(true)
}
```

## Requirements

- Java 11 or higher
- Kotlin 1.9.20 or higher
- Gradle 7.0 or higher

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## Support

If you encounter any issues or have questions, please [open an issue](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/issues) on GitHub.
