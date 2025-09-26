plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    application
}

group = "com.example"
version = "1.0.0"

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // FlexibleSDK 注解和核心类
    implementation(project(":"))
    
    // KSP 处理器 - 使用根项目
    ksp(project(":"))
    
    // 测试依赖
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
}

application {
    mainClass.set("com.example.MainKt")
}

kotlin {
    jvmToolchain(11)
}