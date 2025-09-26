# Spring Integration Example

This example demonstrates how to integrate FlexibleSDK Processor with Spring Framework, combining Spring's dependency injection with FlexibleSDK's service registry.

## Overview

This example shows:
- Integration with Spring Boot
- Using FlexibleSDK services within Spring components
- Bridging Spring beans and FlexibleSDK services
- Configuration and lifecycle management

## Running the Example

```bash
cd examples/spring-integration
./gradlew bootRun
```

Or build and run:
```bash
./gradlew build
java -jar build/libs/spring-integration-1.0.0.jar
```

## Code Structure

```
src/main/kotlin/com/example/spring/
├── SpringIntegrationApplication.kt    # Spring Boot main class
├── config/
│   ├── FlexibleSDKConfig.kt          # FlexibleSDK configuration
│   └── ServiceBridge.kt              # Bridge between Spring and FlexibleSDK
├── services/
│   ├── UserService.kt                # User service interface
│   ├── UserServiceImpl.kt            # FlexibleSDK service implementation
│   ├── NotificationService.kt        # Notification service interface
│   └── EmailNotificationService.kt   # Spring service implementation
├── controllers/
│   └── UserController.kt             # REST controller
└── models/
    └── User.kt                       # Data models
```

## Key Features

- **Hybrid Architecture**: Combines Spring DI with FlexibleSDK service registry
- **Service Bridge**: Seamless integration between Spring beans and FlexibleSDK services
- **REST API**: Exposes services through Spring Web
- **Configuration**: Centralized configuration management
- **Lifecycle Management**: Proper initialization and cleanup