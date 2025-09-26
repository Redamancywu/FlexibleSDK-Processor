# Android Project Example

This example demonstrates how to use FlexibleSDK Processor in an Android application, showcasing service registration and dependency injection in an Android context.

## Overview

This example shows:
- Android application with FlexibleSDK Processor
- Service registration for Android-specific components
- Integration with Android lifecycle
- Repository pattern with Room database
- MVVM architecture with ViewModels

## Project Structure

```
src/main/
├── kotlin/com/example/android/
│   ├── AndroidApplication.kt          # Application class
│   ├── services/
│   │   ├── UserService.kt            # User service interface
│   │   ├── UserServiceImpl.kt        # Service implementation
│   │   ├── DatabaseService.kt        # Database service
│   │   └── PreferenceService.kt      # Shared preferences service
│   ├── repositories/
│   │   └── UserRepository.kt         # Data repository
│   ├── viewmodels/
│   │   └── UserViewModel.kt          # ViewModel for UI
│   ├── activities/
│   │   └── MainActivity.kt           # Main activity
│   └── models/
│       └── User.kt                   # Data models
└── res/
    ├── layout/
    │   ├── activity_main.xml         # Main activity layout
    │   └── item_user.xml             # User list item layout
    └── values/
        ├── strings.xml               # String resources
        └── colors.xml                # Color resources
```

## Building and Running

1. Open the project in Android Studio
2. Sync the project with Gradle files
3. Build and run on an Android device or emulator

```bash
# Or build from command line
./gradlew assembleDebug
./gradlew installDebug
```

## Key Features

- **Service Registration**: Android services registered with FlexibleSDK
- **Dependency Injection**: Automatic dependency resolution
- **Database Integration**: Room database with service layer
- **MVVM Pattern**: Clean architecture with ViewModels
- **Lifecycle Awareness**: Proper Android lifecycle management