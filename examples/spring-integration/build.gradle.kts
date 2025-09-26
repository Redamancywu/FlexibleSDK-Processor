plugins {
    kotlin("jvm")
    kotlin("plugin.spring") version "1.9.10"
    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
    id("com.google.devtools.ksp") version "1.9.10-1.0.13"
    application
}

group = "com.example"
version = "1.0.0"

dependencies {
    // Spring Boot dependencies
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    
    // Kotlin dependencies
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    
    // Use local project dependency for processor
    ksp(project(":"))
    compileOnly(project(":"))
    
    // Test dependencies
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
}

// KSP configuration
ksp {
    arg("serviceRegistryPackage", "com.example.spring.registry")
    arg("serviceRegistryClassName", "SpringServiceRegistry")
    arg("enableDebugLogging", "true")
}

application {
    mainClass.set("com.example.spring.SpringIntegrationApplicationKt")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}