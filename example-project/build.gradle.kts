plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("io.github.Redamancywu.processor") version "1.0.0"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    
    // 测试依赖
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("junit:junit:4.13.2")
}

// FlexibleSDK 插件配置
flexibleSDK {
    // 必需：生成的服务注册表的包名
    serviceRegistryPackage.set("com.example.registry")
    
    // 必需：生成的服务注册表的类名
    serviceRegistryClassName.set("ServiceRegistry")
    
    // 可选：启用调试日志
    enableDebugLogging.set(true)
    
    // 可选：日志级别
    logLevel.set("INFO")
    
    // 可选：启用增量编译
    enableIncremental.set(true)
    
    // 可选：验证服务依赖
    validateDependencies.set(true)
    
    // 可选：显示处理进度
    showProgress.set(true)
    
    // 可选：显示性能统计
    showPerformanceStats.set(true)
    
    // 可选：显示详细验证结果
    showDetailedValidation.set(true)
}

kotlin {
    jvmToolchain(11)
}