# FlexibleSDK Processor

A flexible SDK processor for Kotlin annotation processing and code generation.

## Features

- Kotlin annotation processing with KSP (Kotlin Symbol Processing)
- Automatic code generation for service registries
- Gradle plugin integration
- Support for flexible SDK architectures

## Installation

### Using JitPack

Add the JitPack repository to your `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

Add the dependency:

```kotlin
dependencies {
    implementation("com.github.Redamancywu:FlexibleSDK-Processor:0.0.3")
}
```

### Using Gradle Plugin

Apply the plugin in your `build.gradle.kts`:

```kotlin
plugins {
    id("com.flexiblesdk.processor") version "0.0.3"
}
```

## Usage

### Basic Annotation Processing

1. Annotate your classes with FlexibleSDK annotations
2. Run the build process
3. Generated code will be available in the build output

### Service Registry

The processor automatically generates service registries for annotated services, enabling flexible dependency injection and service discovery.

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

If you encounter any issues or have questions, please [open an issue](https://github.com/Redamancywu/FlexibleSDK-Processor/issues) on GitHub.
