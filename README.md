# FlexibleSDK Processor

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.Redamancywu.processor)](https://plugins.gradle.org/plugin/io.github.Redamancywu.processor)
[![GitHub release](https://img.shields.io/github/release/Redamancywu/FlexibleSDK-Processor-Standalone.svg)](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/releases)

**Language**: [English](README.md) | [中文](README_zh.md)

A powerful and flexible Kotlin annotation processor that automatically generates service registries for dependency injection and modular architecture. Built with KSP (Kotlin Symbol Processing) for optimal performance and seamless Gradle integration.

## 🚀 Features

- **Automatic Service Registry Generation**: Generate type-safe service registries from annotations
- **Kotlin Symbol Processing (KSP)**: Fast and efficient compile-time processing
- **Gradle Plugin Integration**: Easy setup with minimal configuration
- **Dependency Management**: Automatic dependency resolution and validation
- **Modular Architecture Support**: Organize services into modules with priorities
- **Type Safety**: Compile-time type checking and validation
- **Incremental Compilation**: Only processes changed files for faster builds
- **Debug Support**: Comprehensive logging and error reporting

## 📋 Requirements

- **Java**: 11 or higher
- **Kotlin**: 1.9.20 or higher
- **Gradle**: 7.0 or higher
- **KSP**: 1.9.20-1.0.14 or higher

## 🛠️ Installation

### Method 1: Gradle Plugin (Recommended)

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("io.github.Redamancywu.processor") version "1.0.0"
}
```

Configure the plugin:

```kotlin
flexibleSDK {
    serviceRegistryPackage.set("com.yourpackage.registry")
    serviceRegistryClassName.set("ServiceRegistry")
    enableDebugLogging.set(true)
}
```

### Method 2: Manual KSP Configuration

For more control, configure KSP manually:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // KSP processor
    ksp("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:1.0.0")
    
    // Compile-time annotations
    compileOnly("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:1.0.0")
}

ksp {
    arg("serviceRegistryPackage", "com.yourpackage.registry")
    arg("serviceRegistryClassName", "ServiceRegistry")
    arg("enableDebugLogging", "true")
}
```

## 📖 Complete Usage Guide

### Step 1: Define Service Interfaces

Create your service interfaces:

```kotlin
interface UserService {
    fun getUser(id: String): User
    fun createUser(user: User): User
    fun updateUser(user: User): User
    fun deleteUser(id: String): Boolean
}

interface DatabaseService {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnected(): Boolean
}
```

### Step 2: Implement Services with Annotations

Use `@ServiceProvider` to mark your service implementations:

```kotlin
import com.flexiblesdk.processor.annotation.ServiceProvider

@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    dependencies = [DatabaseService::class],
    singleton = true
)
class UserServiceImpl(
    private val databaseService: DatabaseService
) : UserService {
    
    override fun getUser(id: String): User {
        // Implementation
        return User(id, "John Doe")
    }
    
    override fun createUser(user: User): User {
        // Implementation
        return user
    }
    
    override fun updateUser(user: User): User {
        // Implementation
        return user
    }
    
    override fun deleteUser(id: String): Boolean {
        // Implementation
        return true
    }
}

@ServiceProvider(
    interfaces = [DatabaseService::class],
    priority = 200,
    singleton = true
)
class DatabaseServiceImpl : DatabaseService {
    
    override fun connect(): Boolean {
        println("Connecting to database...")
        return true
    }
    
    override fun disconnect(): Boolean {
        println("Disconnecting from database...")
        return true
    }
    
    override fun isConnected(): Boolean {
        return true
    }
}
```

### Step 3: Organize Services into Modules

Use `@ServiceModule` to group related services:

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

@ServiceModule(
    name = "CoreModule",
    version = "1.0.0",
    description = "Core infrastructure services",
    priority = 100
)
class CoreModule
```

### Step 4: Build Your Project

Run the build to generate the service registry:

```bash
./gradlew build
```

The processor will generate a `ServiceRegistry.kt` file in your specified package:

```kotlin
// Generated code - do not modify
package com.yourpackage.registry

import kotlin.reflect.KClass

object ServiceRegistry {
    
    /**
     * Get a service instance by interface class
     */
    fun <T : Any> getService(interfaceClass: KClass<T>): T? {
        return getService(interfaceClass.java) as? T
    }
    
    /**
     * Get a service instance by interface class
     */
    fun getService(interfaceClass: Class<*>): Any? {
        // Generated implementation
    }
    
    /**
     * Get all services implementing a specific interface
     */
    fun getServicesByInterface(interfaceClass: Class<*>): List<ServiceInfo> {
        // Generated implementation
    }
    
    /**
     * Get module information by name
     */
    fun getModule(name: String): ModuleInfo? {
        // Generated implementation
    }
    
    /**
     * Get all registered services
     */
    fun getAllServices(): Map<String, ServiceInfo> {
        // Generated implementation
    }
    
    /**
     * Get all registered modules
     */
    fun getAllModules(): Map<String, ModuleInfo> {
        // Generated implementation
    }
}
```

### Step 5: Use the Generated Service Registry

```kotlin
import com.yourpackage.registry.ServiceRegistry

fun main() {
    // Get a service by interface
    val userService = ServiceRegistry.getService(UserService::class)
    
    if (userService != null) {
        val user = userService.getUser("123")
        println("User: ${user.name}")
    }
    
    // Get all services implementing an interface
    val userServices = ServiceRegistry.getServicesByInterface(UserService::class.java)
    println("Found ${userServices.size} user services")
    
    // Get module information
    val userModule = ServiceRegistry.getModule("UserModule")
    println("Module: ${userModule?.name} v${userModule?.version}")
    
    // List all services
    val allServices = ServiceRegistry.getAllServices()
    allServices.forEach { (name, info) ->
        println("Service: $name, Priority: ${info.priority}")
    }
}
```

## ⚙️ Configuration Options

### Plugin Configuration

Configure the plugin in your `build.gradle.kts`:

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
    excludePackages.set(listOf("com.example.test", "com.example.mock"))
    
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

### Version Configuration

To avoid version configuration warnings, specify the processor version in `gradle.properties`:

```properties
# FlexibleSDK Processor version configuration
flexibleSDKProcessorVersion=1.0.0
```

**Version Resolution Priority:**
1. Project property: `flexibleSDK.processorVersion`
2. Gradle properties: `flexibleSDKProcessorVersion`
3. Project version (if not "unspecified")
4. Default version: 1.0.0 (shows warning)

**Alternative configuration methods:**

```kotlin
// Method 1: In build.gradle.kts (project property)
project.ext["flexibleSDK.processorVersion"] = "1.0.0"

// Method 2: Command line
./gradlew build -PflexibleSDK.processorVersion=1.0.0

// Method 3: gradle.properties (recommended)
flexibleSDKProcessorVersion=1.0.0
```

### Annotation Reference

#### @ServiceProvider

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],     // Required: Interfaces this service implements
    priority = 100,                        // Optional: Service priority (lower number = higher priority)
    dependencies = [DatabaseService::class], // Optional: Service dependencies
    singleton = true,                      // Optional: Whether this is a singleton service
    lazy = false,                          // Optional: Whether to initialize lazily
    module = "userModule"                  // Optional: Module name this service belongs to
)
```

#### @ServiceModule

```kotlin
@ServiceModule(
    name = "UserModule",                // Required: Module name
    version = "1.0.0",                  // Optional: Module version
    description = "User services",      // Optional: Module description
    priority = 50,                      // Optional: Module priority (lower number = higher priority)
    dependencies = ["CoreModule"],      // Optional: Module dependencies
    autoLoad = true                     // Optional: Whether to auto-load this module
)
```

## 🔧 Advanced Usage

### Custom Service Factory

Create custom service factories for complex initialization:

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    factory = UserServiceFactory::class
)
class UserServiceImpl : UserService {
    // Implementation
}

class UserServiceFactory : ServiceFactory<UserService> {
    override fun create(): UserService {
        // Custom initialization logic
        return UserServiceImpl()
    }
}
```

### Conditional Services

Use conditions to control when services are registered:

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    condition = ProductionCondition::class
)
class ProductionUserService : UserService {
    // Production implementation
}

class ProductionCondition : ServiceCondition {
    override fun matches(): Boolean {
        return System.getProperty("env") == "production"
    }
}
```

### Service Profiles

Organize services by profiles:

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    profiles = ["dev", "test"]
)
class MockUserService : UserService {
    // Mock implementation for development and testing
}
```

## 🐛 Troubleshooting

### Common Issues

1. **Service not found**: Ensure the service is annotated with `@ServiceProvider`
2. **Circular dependencies**: Check your dependency graph for cycles
3. **Build fails**: Verify KSP plugin is applied and configured correctly
4. **Generated code not found**: Check the output directory and package configuration

### Debug Mode

Enable debug logging to see detailed processing information:

```kotlin
flexibleSDK {
    enableDebugLogging.set(true)
    logLevel.set("DEBUG")
}
```

### Performance Optimization

For large projects, consider these optimizations:

```kotlin
flexibleSDK {
    enableIncremental.set(true)
    includePackages.set(listOf("com.yourpackage.services")) // Limit processing scope
    showPerformanceStats.set(true) // Monitor processing time
}
```

## 📚 Examples

Check out the [examples directory](examples/) for complete sample projects:

- [Basic Usage](examples/basic-usage/) - Simple service registration and dependency injection
- [Modular Architecture](examples/modular-architecture/) - Complex multi-module setup with service organization
- [Spring Integration](examples/spring-integration/) - Integration with Spring Framework and Boot
- [Android Project](examples/android-project/) - Complete Android application with Room database and MVVM pattern

## 🤝 Contributing

We welcome contributions! Please see our [Contributing Guide](CONTRIBUTING.md) for details.

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## 🆘 Support

- **Documentation**: [Wiki](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/wiki)
- **Issues**: [GitHub Issues](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/issues)
- **Discussions**: [GitHub Discussions](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/discussions)

## 🙏 Acknowledgments

- [Kotlin Symbol Processing (KSP)](https://github.com/google/ksp) - The foundation of this processor
- [Gradle](https://gradle.org/) - Build automation platform
- All contributors who have helped improve this project

---

**Made with ❤️ by [Redamancywu](https://github.com/Redamancywu)**
