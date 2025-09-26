# Modular Architecture Example

This example demonstrates a complex multi-module setup using FlexibleSDK Processor with service modules and dependencies.

## Overview

This example shows how to:
- Organize services into modules with `@ServiceModule`
- Define service dependencies
- Use module priorities
- Create a layered architecture with core, data, and business modules

## Architecture

```
┌─────────────────┐
│  Business Layer │  (UserModule, OrderModule)
├─────────────────┤
│   Data Layer    │  (DatabaseModule)
├─────────────────┤
│   Core Layer    │  (CoreModule)
└─────────────────┘
```

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

- `src/main/kotlin/com/example/modular/`
  - `core/` - Core infrastructure services
  - `data/` - Data access services
  - `business/` - Business logic services
  - `Main.kt` - Example usage