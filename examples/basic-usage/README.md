# Basic Usage Example

This example demonstrates the basic usage of FlexibleSDK Processor with simple service registration.

## Overview

This example shows how to:
- Define a simple service interface
- Implement the service with `@ServiceProvider` annotation
- Use the generated service registry

## Running the Example

1. Build the project:
   ```bash
   ./gradlew build
   ```

2. Run the example:
   ```bash
   ./gradlew run
   ```

## Code Structure

- `src/main/kotlin/com/example/basic/` - Main source code
  - `UserService.kt` - Service interface
  - `UserServiceImpl.kt` - Service implementation
  - `Main.kt` - Example usage