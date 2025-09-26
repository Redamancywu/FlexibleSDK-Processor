plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    application
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // Use local project dependency for processor
    ksp(project(":"))
    compileOnly(project(":"))
}

ksp {
    arg("serviceRegistryPackage", "com.example.modular.registry")
    arg("serviceRegistryClassName", "ModularServiceRegistry")
    arg("enableDebugLogging", "true")
}

application {
    mainClass.set("com.example.modular.MainKt")
}

kotlin {
    jvmToolchain(11)
}