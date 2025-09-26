rootProject.name = "FlexibleSDK-Processor"

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

// Include example projects as subprojects
include(":examples:basic-usage")
include(":examples:modular-architecture")
include(":example-project")
// Note: spring-integration requires Java 17 and is excluded from main build
// Note: android-project requires Android SDK and is excluded from main build
