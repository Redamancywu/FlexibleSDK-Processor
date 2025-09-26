# FlexibleSDK 处理器

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.Redamancywu.processor)](https://plugins.gradle.org/plugin/io.github.Redamancywu.processor)
[![GitHub release](https://img.shields.io/github/release/Redamancywu/FlexibleSDK-Processor-Standalone.svg)](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/releases)

**语言**: [English](README.md) | [中文](README_zh.md)

一个强大而灵活的 Kotlin 注解处理器，可以自动生成用于依赖注入和模块化架构的服务注册表。基于 KSP（Kotlin Symbol Processing）构建，具有最佳性能和无缝的 Gradle 集成。

## 🚀 特性

- **自动服务注册表生成**：从注解自动生成类型安全的服务注册表
- **Kotlin 符号处理（KSP）**：快速高效的编译时处理
- **Gradle 插件集成**：简单设置，最少配置
- **依赖管理**：自动依赖解析和验证
- **模块化架构支持**：按优先级将服务组织到模块中
- **类型安全**：编译时类型检查和验证
- **增量编译**：仅处理更改的文件，加快构建速度
- **调试支持**：全面的日志记录和错误报告

## 📋 系统要求

- **Java**：11 或更高版本
- **Kotlin**：1.9.20 或更高版本
- **Gradle**：7.0 或更高版本
- **KSP**：1.9.20-1.0.14 或更高版本

## 🛠️ 安装

### 方法一：Gradle 插件（推荐）

在您的 `build.gradle.kts` 中添加插件：

```kotlin
plugins {
    kotlin("jvm") version "1.9.20"
    id("com.google.devtools.ksp") version "1.9.20-1.0.14"
    id("io.github.Redamancywu.processor") version "1.0.0"
}
```

配置插件：

```kotlin
flexibleSDK {
    serviceRegistryPackage.set("com.yourpackage.registry")
    serviceRegistryClassName.set("ServiceRegistry")
    enableDebugLogging.set(true)
}
```

### 方法二：手动 KSP 配置

如需更多控制，可手动配置 KSP：

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    // KSP 处理器
    ksp("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:1.0.0")
    
    // 编译时注解
    compileOnly("com.github.Redamancywu:FlexibleSDK-Processor-Standalone:1.0.0")
}

ksp {
    arg("serviceRegistryPackage", "com.yourpackage.registry")
    arg("serviceRegistryClassName", "ServiceRegistry")
    arg("enableDebugLogging", "true")
}
```

## 📖 完整使用指南

### 步骤 1：定义服务接口

创建您的服务接口：

```kotlin
interface UserService {
    fun getUser(id: String): User
    fun createUser(user: User): User
    fun updateUser(user: User): User
    fun deleteUser(id: String): Boolean
}

interface DatabaseService {
    fun connect(): Boolean
    fun disconnect(): Boolean
    fun isConnected(): Boolean
}
```

### 步骤 2：使用注解实现服务

使用 `@ServiceProvider` 标记您的服务实现：

```kotlin
import com.flexiblesdk.processor.annotation.ServiceProvider

@ServiceProvider(
    interfaces = [UserService::class],
    priority = 100,
    dependencies = [DatabaseService::class],
    singleton = true
)
class UserServiceImpl(
    private val databaseService: DatabaseService
) : UserService {
    
    override fun getUser(id: String): User {
        // 实现代码
        return User(id, "张三")
    }
    
    override fun createUser(user: User): User {
        // 实现代码
        return user
    }
    
    override fun updateUser(user: User): User {
        // 实现代码
        return user
    }
    
    override fun deleteUser(id: String): Boolean {
        // 实现代码
        return true
    }
}

@ServiceProvider(
    interfaces = [DatabaseService::class],
    priority = 200,
    singleton = true
)
class DatabaseServiceImpl : DatabaseService {
    
    override fun connect(): Boolean {
        println("连接数据库...")
        return true
    }
    
    override fun disconnect(): Boolean {
        println("断开数据库连接...")
        return true
    }
    
    override fun isConnected(): Boolean {
        return true
    }
}
```

### 步骤 3：将服务组织到模块中

使用 `@ServiceModule` 对相关服务进行分组：

```kotlin
import com.flexiblesdk.processor.annotation.ServiceModule

@ServiceModule(
    name = "UserModule",
    version = "1.0.0",
    description = "用户管理服务",
    priority = 50,
    dependencies = ["CoreModule"]
)
class UserModule

@ServiceModule(
    name = "CoreModule",
    version = "1.0.0",
    description = "核心基础设施服务",
    priority = 100
)
class CoreModule
```

### 步骤 4：构建您的项目

运行构建以生成服务注册表：

```bash
./gradlew build
```

处理器将在您指定的包中生成 `ServiceRegistry.kt` 文件：

```kotlin
// 生成的代码 - 请勿修改
package com.yourpackage.registry

import kotlin.reflect.KClass

object ServiceRegistry {
    
    /**
     * 通过接口类获取服务实例
     */
    fun <T : Any> getService(interfaceClass: KClass<T>): T? {
        return getService(interfaceClass.java) as? T
    }
    
    /**
     * 通过接口类获取服务实例
     */
    fun getService(interfaceClass: Class<*>): Any? {
        // 生成的实现
    }
    
    /**
     * 获取实现特定接口的所有服务
     */
    fun getServicesByInterface(interfaceClass: Class<*>): List<ServiceInfo> {
        // 生成的实现
    }
    
    /**
     * 通过名称获取模块信息
     */
    fun getModule(name: String): ModuleInfo? {
        // 生成的实现
    }
    
    /**
     * 获取所有注册的服务
     */
    fun getAllServices(): Map<String, ServiceInfo> {
        // 生成的实现
    }
    
    /**
     * 获取所有注册的模块
     */
    fun getAllModules(): Map<String, ModuleInfo> {
        // 生成的实现
    }
}
```

### 步骤 5：使用生成的服务注册表

```kotlin
import com.yourpackage.registry.ServiceRegistry

fun main() {
    // 通过接口获取服务
    val userService = ServiceRegistry.getService(UserService::class)
    
    if (userService != null) {
        val user = userService.getUser("123")
        println("用户: ${user.name}")
    }
    
    // 获取实现接口的所有服务
    val userServices = ServiceRegistry.getServicesByInterface(UserService::class.java)
    println("找到 ${userServices.size} 个用户服务")
    
    // 获取模块信息
    val userModule = ServiceRegistry.getModule("UserModule")
    println("模块: ${userModule?.name} v${userModule?.version}")
    
    // 列出所有服务
    val allServices = ServiceRegistry.getAllServices()
    allServices.forEach { (name, info) ->
        println("服务: $name, 优先级: ${info.priority}")
    }
}
```

## ⚙️ 配置选项

### 插件配置

在您的 `build.gradle.kts` 中配置插件：

```kotlin
flexibleSDK {
    // 必需：生成的服务注册表的包名
    serviceRegistryPackage.set("com.yourpackage.registry")
    
    // 必需：生成的服务注册表的类名
    serviceRegistryClassName.set("ServiceRegistry")
    
    // 可选：启用调试日志（默认：false）
    enableDebugLogging.set(true)
    
    // 可选：日志级别（DEBUG, INFO, WARN, ERROR，默认：INFO）
    logLevel.set("INFO")
    
    // 可选：启用增量编译（默认：true）
    enableIncremental.set(true)
    
    // 可选：验证服务依赖（默认：true）
    validateDependencies.set(true)
    
    // 可选：生成文档（默认：false）
    generateDocumentation.set(false)
    
    // 可选：排除处理的包
    excludePackages.set(listOf("com.example.test", "com.example.mock"))
    
    // 可选：仅包含特定包
    includePackages.set(listOf("com.yourpackage.services"))
    
    // 可选：处理过程中显示进度（默认：false）
    showProgress.set(true)
    
    // 可选：显示性能统计（默认：false）
    showPerformanceStats.set(true)
    
    // 可选：显示详细验证结果（默认：false）
    showDetailedValidation.set(true)
}
```

### 版本配置

为了避免版本配置警告，请在 `gradle.properties` 中指定处理器版本：

```properties
# FlexibleSDK 处理器版本配置
flexibleSDKProcessorVersion=1.0.0
```

**版本解析优先级：**
1. 项目属性：`flexibleSDK.processorVersion`
2. Gradle 属性：`flexibleSDKProcessorVersion`
3. 项目版本（如果不是 "unspecified"）
4. 默认版本：1.0.0（会显示警告）

**其他配置方法：**

```kotlin
// 方法1：在 build.gradle.kts 中（项目属性）
project.ext["flexibleSDK.processorVersion"] = "1.0.0"

// 方法2：命令行
./gradlew build -PflexibleSDK.processorVersion=1.0.0

// 方法3：gradle.properties（推荐）
flexibleSDKProcessorVersion=1.0.0
```

### 注解参考

#### @ServiceProvider

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],     // 必需：此服务实现的接口
    priority = 100,                        // 可选：服务优先级（数值越小优先级越高）
    dependencies = [DatabaseService::class], // 可选：服务依赖
    singleton = true,                      // 可选：是否为单例服务
    lazy = false,                          // 可选：是否延迟初始化
    module = "userModule"                  // 可选：所属模块名称
)
```

#### @ServiceModule

```kotlin
@ServiceModule(
    name = "UserModule",                // 必需：模块名称
    version = "1.0.0",                  // 可选：模块版本
    description = "用户服务",            // 可选：模块描述
    priority = 50,                      // 可选：模块优先级（数值越小优先级越高）
    dependencies = ["CoreModule"],      // 可选：模块依赖
    autoLoad = true                     // 可选：是否自动加载
)
```

## 🔧 高级用法

### 自定义服务工厂

为复杂初始化创建自定义服务工厂：

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    factory = UserServiceFactory::class
)
class UserServiceImpl : UserService {
    // 实现代码
}

class UserServiceFactory : ServiceFactory<UserService> {
    override fun create(): UserService {
        // 自定义初始化逻辑
        return UserServiceImpl()
    }
}
```

### 条件服务

使用条件控制服务何时注册：

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    condition = ProductionCondition::class
)
class ProductionUserService : UserService {
    // 生产环境实现
}

class ProductionCondition : ServiceCondition {
    override fun matches(): Boolean {
        return System.getProperty("env") == "production"
    }
}
```

### 服务配置文件

按配置文件组织服务：

```kotlin
@ServiceProvider(
    interfaces = [UserService::class],
    profiles = ["dev", "test"]
)
class MockUserService : UserService {
    // 开发和测试的模拟实现
}
```

## 🐛 故障排除

### 常见问题

1. **找不到服务**：确保服务已使用 `@ServiceProvider` 注解
2. **循环依赖**：检查您的依赖图是否存在循环
3. **构建失败**：验证 KSP 插件是否正确应用和配置
4. **找不到生成的代码**：检查输出目录和包配置

### 调试模式

启用调试日志以查看详细的处理信息：

```kotlin
flexibleSDK {
    enableDebugLogging.set(true)
    logLevel.set("DEBUG")
}
```

### 性能优化

对于大型项目，考虑这些优化：

```kotlin
flexibleSDK {
    enableIncremental.set(true)
    includePackages.set(listOf("com.yourpackage.services")) // 限制处理范围
    showPerformanceStats.set(true) // 监控处理时间
}
```

## 📚 示例

查看 [examples 目录](examples/) 获取完整的示例项目：

- [基础用法](examples/basic-usage/) - 简单的服务注册和依赖注入
- [模块化架构](examples/modular-architecture/) - 复杂的多模块设置和服务组织
- [Spring 集成](examples/spring-integration/) - 与 Spring 框架和 Boot 集成
- [Android 项目](examples/android-project/) - 完整的 Android 应用，包含 Room 数据库和 MVVM 模式

## 🤝 贡献

我们欢迎贡献！请查看我们的 [贡献指南](CONTRIBUTING.md) 了解详情。

1. Fork 仓库
2. 创建您的功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交您的更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 打开一个 Pull Request

## 📄 许可证

本项目采用 Apache License 2.0 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 🆘 支持

- **文档**：[Wiki](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/wiki)
- **问题**：[GitHub Issues](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/issues)
- **讨论**：[GitHub Discussions](https://github.com/Redamancywu/FlexibleSDK-Processor-Standalone/discussions)

## 🙏 致谢

- [Kotlin Symbol Processing (KSP)](https://github.com/google/ksp) - 本处理器的基础
- [Gradle](https://gradle.org/) - 构建自动化平台
- 所有帮助改进此项目的贡献者

---

**由 [Redamancywu](https://github.com/Redamancywu) 用 ❤️ 制作**