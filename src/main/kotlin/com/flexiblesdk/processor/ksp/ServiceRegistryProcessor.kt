package com.flexiblesdk.processor.ksp

import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.flexiblesdk.processor.annotation.ServiceProvider
import com.flexiblesdk.processor.annotation.ServiceModule
import com.flexiblesdk.processor.utils.LoggerUtils
import java.io.File
import java.security.MessageDigest

/**
 * FlexibleSDK 服务注册表处理器
 * 
 * 扫描 @ServiceProvider 和 @ServiceModule 注解，生成静态服务注册表
 */
class ServiceRegistryProcessor(
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
    private val options: Map<String, String>
) : SymbolProcessor {
    
    companion object {
        private const val SERVICE_PROVIDER_ANNOTATION = "com.flexiblesdk.processor.annotation.ServiceProvider"
        private const val SERVICE_MODULE_ANNOTATION = "com.flexiblesdk.processor.annotation.ServiceModule"
    }
    
    private val serviceProviders = mutableListOf<ServiceProviderInfo>()
    private val serviceModules = mutableListOf<ServiceModuleInfo>()
    private val processedFiles = mutableSetOf<String>()
    
    // 增量编译支持 - 增强版
    private val enableIncremental = options["enableIncremental"]?.toBoolean() ?: true
    private val fileDependencies = mutableMapOf<String, Set<String>>()
    private val symbolToFileMap = mutableMapOf<String, String>()
    
    // 新增：文件变更检测
    private val fileTimestamps = mutableMapOf<String, Long>()
    private val fileHashes = mutableMapOf<String, String>()
    private val processedSymbols = mutableSetOf<String>()
    
    // 新增：依赖缓存
    private val symbolDependencyCache = mutableMapOf<String, Set<String>>()
    private val fileSymbolCache = mutableMapOf<String, Set<String>>()
    
    // 配置选项
    private val serviceRegistryPackage = options["serviceRegistryPackage"] ?: "com.flexiblesdk.core.manager"
    private val serviceRegistryClassName = options["serviceRegistryClassName"] ?: "ServiceRegistry"
    private val enableDebugLogging = options["enableDebugLogging"]?.toBoolean() ?: false
    private val logLevel = options["logLevel"] ?: "INFO"
    private val showProgress = options["showProgress"]?.toBoolean() ?: true
    private val showPerformanceStats = options["showPerformanceStats"]?.toBoolean() ?: false
    private val showDetailedValidation = options["showDetailedValidation"]?.toBoolean() ?: false
    private val validateDependencies = options["validateDependencies"]?.toBoolean() ?: true
    private val generateDocumentation = options["generateDocumentation"]?.toBoolean() ?: false
    private val excludePackages = options["excludePackages"]?.split(",")?.toSet() ?: emptySet()
    private val includePackages = options["includePackages"]?.split(",")?.toSet() ?: emptySet()
    
    init {
        // 初始化日志配置
        LoggerUtils.setKSPLogger(logger)
        LoggerUtils.isDebugEnabled = enableDebugLogging
        LoggerUtils.currentLogLevel = when (logLevel.uppercase()) {
            "DEBUG" -> LoggerUtils.LogLevel.DEBUG
            "INFO" -> LoggerUtils.LogLevel.INFO
            "WARN" -> LoggerUtils.LogLevel.WARN
            "ERROR" -> LoggerUtils.LogLevel.ERROR
            else -> LoggerUtils.LogLevel.INFO
        }
    }
    
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val startTime = System.currentTimeMillis()
        
        try {
            LoggerUtils.logProcessStart("ServiceRegistryProcessor", "开始处理服务注册表")
            
            // 验证 resolver 状态
            if (resolver == null) {
                LoggerUtils.logError("Resolver 为 null，无法继续处理")
                return emptyList()
            }
            
            val serviceProviderSymbols = try {
                resolver
                    .getSymbolsWithAnnotation(SERVICE_PROVIDER_ANNOTATION)
                    .filterIsInstance<KSClassDeclaration>()
            } catch (e: Exception) {
                LoggerUtils.logError("获取服务提供者符号时发生错误: ${e.message}")
                return emptyList()
            }
            
            val serviceModuleSymbols = try {
                resolver
                    .getSymbolsWithAnnotation(SERVICE_MODULE_ANNOTATION)
                    .filterIsInstance<KSClassDeclaration>()
            } catch (e: Exception) {
                LoggerUtils.logError("获取服务模块符号时发生错误: ${e.message}")
                return emptyList()
            }
            
            val providerCount = serviceProviderSymbols.count()
            val moduleCount = serviceModuleSymbols.count()
            
            LoggerUtils.logInfo("发现 $providerCount 个服务提供者和 $moduleCount 个服务模块")
            
            // 边界情况检查
            if (providerCount == 0 && moduleCount == 0) {
                LoggerUtils.logInfo("未发现任何服务提供者或模块，跳过处理")
                return emptyList()
            }
            
            if (providerCount > 1000 || moduleCount > 1000) {
                LoggerUtils.logWarn("发现大量服务或模块 (提供者: $providerCount, 模块: $moduleCount)，处理可能需要较长时间")
            }
            
            // 增量编译支持：收集无法处理的符号
            val unableToProcess = mutableListOf<KSAnnotated>()
            var processedProviders = 0
            var processedModules = 0
            var skippedProviders = 0
            var skippedModules = 0
            var errorProviders = 0
            var errorModules = 0
            
            // 处理服务提供者 - 增量编译优化
            if (showProgress) {
                LoggerUtils.logInfo("开始处理服务提供者...")
            }
            
            // 过滤出需要验证的符号
            val validProviderSymbols = serviceProviderSymbols.filter { it.validate() }
            val invalidProviderSymbols = serviceProviderSymbols.filter { !it.validate() }
            
            // 将无法验证的符号添加到无法处理列表中
            unableToProcess.addAll(invalidProviderSymbols)
            
            validProviderSymbols.forEach { symbol ->
                try {
                    if (shouldProcessClass(symbol)) {
                        // 记录文件依赖关系
                        trackFileDependency(symbol)
                        processServiceProvider(symbol)
                        processedProviders++
                        if (showProgress && processedProviders % 10 == 0) {
                            LoggerUtils.logInfo("已处理 $processedProviders 个服务提供者")
                        }
                    } else {
                        skippedProviders++
                        LoggerUtils.logDebug("跳过类 ${symbol.qualifiedName?.asString()} (被包过滤器排除)")
                    }
                } catch (e: Exception) {
                    errorProviders++
                    LoggerUtils.logError("处理服务提供者 ${symbol.qualifiedName?.asString()} 时发生错误: ${e.message}")
                }
            }
            
            // 处理服务模块 - 增量编译优化
            if (showProgress) {
                LoggerUtils.logInfo("开始处理服务模块...")
            }
            
            // 过滤出需要验证的符号
            val validModuleSymbols = serviceModuleSymbols.filter { it.validate() }
            val invalidModuleSymbols = serviceModuleSymbols.filter { !it.validate() }
            
            // 将无法验证的符号添加到无法处理列表中
            unableToProcess.addAll(invalidModuleSymbols)
            
            validModuleSymbols.forEach { symbol ->
                try {
                    if (shouldProcessClass(symbol)) {
                        // 记录文件依赖关系
                        trackFileDependency(symbol)
                        processServiceModule(symbol)
                        processedModules++
                        if (showProgress && processedModules % 10 == 0) {
                            LoggerUtils.logInfo("已处理 $processedModules 个服务模块")
                        }
                    } else {
                        skippedModules++
                        LoggerUtils.logDebug("跳过模块 ${symbol.qualifiedName?.asString()} (被包过滤器排除)")
                    }
                } catch (e: Exception) {
                    errorModules++
                    LoggerUtils.logError("处理服务模块 ${symbol.qualifiedName?.asString()} 时发生错误: ${e.message}")
                }
            }
            
            // 记录处理统计
            LoggerUtils.logStatistics(mapOf(
                "处理的服务提供者" to processedProviders,
                "跳过的服务提供者" to skippedProviders,
                "错误的服务提供者" to errorProviders,
                "处理的服务模块" to processedModules,
                "跳过的服务模块" to skippedModules,
                "错误的服务模块" to errorModules,
                "无法处理的符号" to unableToProcess.size,
                "增量编译" to enableIncremental
            ))
            
            // 增量编译：只有在没有无法处理的符号时才生成注册表
            if (unableToProcess.isEmpty()) {
                // 检查是否有足够的服务进行注册表生成
                if (serviceProviders.isEmpty() && serviceModules.isEmpty()) {
                    LoggerUtils.logWarn("没有成功处理任何服务或模块，跳过注册表生成")
                } else {
                    LoggerUtils.logInfo("开始生成服务注册表...")
                    try {
                        generateServiceRegistry()
                    } catch (e: Exception) {
                        LoggerUtils.logError("生成服务注册表时发生错误: ${e.message}")
                        throw e
                    }
                }
            } else {
                LoggerUtils.logInfo("存在 ${unableToProcess.size} 个无法处理的符号，延迟生成注册表")
            }
            
            if (showPerformanceStats) {
                val duration = System.currentTimeMillis() - startTime
                LoggerUtils.logPerformance("ServiceRegistryProcessor.process", duration)
            }
            
            LoggerUtils.logProcessEnd("ServiceRegistryProcessor", "处理完成")
            
            return unableToProcess
            
        } catch (e: Exception) {
            LoggerUtils.logError("ServiceRegistryProcessor.process 发生未预期的错误: ${e.message}")
            LoggerUtils.logError("错误堆栈: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    private fun shouldProcessClass(symbol: KSClassDeclaration): Boolean {
        val packageName = symbol.packageName.asString()
        
        // 如果设置了包含列表，只处理包含列表中的包
        if (includePackages.isNotEmpty()) {
            return includePackages.any { packageName.startsWith(it) }
        }
        
        // 如果设置了排除列表，排除这些包
        if (excludePackages.isNotEmpty()) {
            return !excludePackages.any { packageName.startsWith(it) }
        }
        
        return true
    }
    
    /**
     * 记录文件依赖关系，用于增量编译支持
     */
    private fun trackFileDependency(symbol: KSClassDeclaration) {
        if (!enableIncremental) return
        
        try {
            val containingFile = symbol.containingFile
            if (containingFile != null) {
                val fileName = containingFile.fileName
                val symbolName = symbol.qualifiedName?.asString() ?: ""
                
                // 检查文件是否发生变更
                if (hasFileChanged(containingFile)) {
                    LoggerUtils.logDebug("检测到文件变更: $fileName")
                    // 清除该文件相关的缓存
                    clearFileCache(fileName)
                } else if (isSymbolCached(symbolName)) {
                    LoggerUtils.logDebug("使用缓存的符号: $symbolName")
                    return // 使用缓存，跳过处理
                }
                
                // 记录符号到文件的映射
                symbolToFileMap[symbolName] = fileName
                
                // 记录文件依赖
                val currentDeps = fileDependencies.getOrPut(fileName) { emptySet() }
                fileDependencies[fileName] = currentDeps + symbolName
                
                // 更新文件符号缓存
                val currentSymbols = fileSymbolCache.getOrPut(fileName) { emptySet() }
                fileSymbolCache[fileName] = currentSymbols + symbolName
                
                // 标记符号已处理
                processedSymbols.add(symbolName)
                
                LoggerUtils.logDebug("记录文件依赖: $fileName -> $symbolName")
            }
        } catch (e: Exception) {
            LoggerUtils.logWarn("记录文件依赖时发生错误: ${e.message}")
        }
    }
    
    /**
     * 检查文件是否发生变更
     */
    private fun hasFileChanged(file: KSFile): Boolean {
        val fileName = file.fileName
        val filePath = file.filePath
        
        try {
            val actualFile = File(filePath)
            if (!actualFile.exists()) {
                LoggerUtils.logWarn("文件不存在: $filePath")
                return true
            }
            
            val currentTimestamp = actualFile.lastModified()
            val cachedTimestamp = fileTimestamps[fileName]
            
            // 如果没有缓存的时间戳，认为是新文件
            if (cachedTimestamp == null) {
                fileTimestamps[fileName] = currentTimestamp
                updateFileHash(fileName, actualFile)
                return true
            }
            
            // 检查时间戳
            if (currentTimestamp != cachedTimestamp) {
                // 时间戳不同，进一步检查内容哈希
                val currentHash = calculateFileHash(actualFile)
                val cachedHash = fileHashes[fileName]
                
                if (currentHash != cachedHash) {
                    // 内容确实发生变更
                    fileTimestamps[fileName] = currentTimestamp
                    fileHashes[fileName] = currentHash
                    LoggerUtils.logDebug("文件内容变更: $fileName")
                    return true
                } else {
                    // 只是时间戳变更，内容未变
                    fileTimestamps[fileName] = currentTimestamp
                    LoggerUtils.logDebug("文件时间戳变更但内容未变: $fileName")
                    return false
                }
            }
            
            return false
        } catch (e: Exception) {
            LoggerUtils.logWarn("检查文件变更时发生错误: ${e.message}")
            return true // 出错时保守处理，认为文件已变更
        }
    }
    
    /**
     * 计算文件内容哈希
     */
    private fun calculateFileHash(file: File): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val content = file.readBytes()
            val hashBytes = digest.digest(content)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            LoggerUtils.logWarn("计算文件哈希时发生错误: ${e.message}")
            System.currentTimeMillis().toString() // 使用时间戳作为备用
        }
    }
    
    /**
     * 更新文件哈希
     */
    private fun updateFileHash(fileName: String, file: File) {
        try {
            val hash = calculateFileHash(file)
            fileHashes[fileName] = hash
        } catch (e: Exception) {
            LoggerUtils.logWarn("更新文件哈希时发生错误: ${e.message}")
        }
    }
    
    /**
     * 检查符号是否已缓存
     */
    private fun isSymbolCached(symbolName: String): Boolean {
        return processedSymbols.contains(symbolName)
    }
    
    /**
     * 清除文件相关的缓存
     */
    private fun clearFileCache(fileName: String) {
        // 清除该文件相关的符号缓存
        val symbols = fileSymbolCache[fileName] ?: emptySet()
        symbols.forEach { symbol ->
            processedSymbols.remove(symbol)
            symbolDependencyCache.remove(symbol)
            symbolToFileMap.remove(symbol)
        }
        
        // 清除文件缓存
        fileSymbolCache.remove(fileName)
        fileDependencies.remove(fileName)
        
        LoggerUtils.logDebug("清除文件缓存: $fileName，影响符号: ${symbols.size}")
    }
    
    /**
     * 缓存符号依赖关系
     */
    private fun cacheSymbolDependencies(symbolName: String, dependencies: Set<String>) {
        symbolDependencyCache[symbolName] = dependencies
    }
    
    /**
     * 获取缓存的符号依赖关系
     */
    private fun getCachedSymbolDependencies(symbolName: String): Set<String>? {
        return symbolDependencyCache[symbolName]
    }
    private fun processServiceProvider(symbol: KSClassDeclaration) {
        val annotation = symbol.annotations.find { 
            it.shortName.asString() == "ServiceProvider" 
        } ?: return
        
        val className = symbol.qualifiedName?.asString() ?: return
        val packageName = symbol.packageName.asString()
        val simpleName = symbol.simpleName.asString()
        
        try {
            LoggerUtils.logDebug("处理服务提供者: $className")
            
            // 验证注解参数
            if (!validateAnnotationParameters(annotation, listOf("interfaces"))) {
                LoggerUtils.logError("服务提供者 $className 的注解参数验证失败")
                return
            }
            
            // 解析注解参数 - 使用类型安全的方法
            val interfaces = getAnnotationArrayValue(annotation, "interfaces")
            val singleton = getAnnotationBooleanValue(annotation, "singleton", true)
            val priority = getAnnotationIntValue(annotation, "priority", 0)
            val lazy = getAnnotationBooleanValue(annotation, "lazy", false)
            val module = getAnnotationStringValue(annotation, "module", "")
            
            // 检查是否可以使用缓存的依赖信息
            val cachedDependencies = getCachedSymbolDependencies(className)
            val dependencies = if (cachedDependencies != null && enableIncremental) {
                LoggerUtils.logDebug("使用缓存的依赖信息: $className")
                cachedDependencies.toList()
            } else {
                // 重新解析依赖
                val parsedDependencies = getAnnotationArrayValue(annotation, "dependencies")
                // 缓存依赖信息
                if (enableIncremental) {
                    cacheSymbolDependencies(className, parsedDependencies.toSet())
                }
                parsedDependencies
            }
        
        // 验证服务提供者
        if (interfaces.isEmpty()) {
            LoggerUtils.logWarn("服务提供者 $className 没有指定接口")
        }
        
        // 验证优先级范围
        if (priority < 0 || priority > 1000) {
            LoggerUtils.logWarn("服务提供者 $className 的优先级 $priority 超出建议范围 [0, 1000]")
        }
        
        // 验证依赖项
        dependencies.forEach { dependency ->
            if (dependency.isBlank()) {
                LoggerUtils.logWarn("服务提供者 $className 包含空的依赖项")
            }
        }
        
        if (showDetailedValidation) {
            val details = mapOf(
                "接口数量" to interfaces.size,
                "是否单例" to singleton,
                "依赖数量" to dependencies.size,
                "优先级" to priority,
                "是否懒加载" to lazy,
                "所属模块" to module.ifEmpty { "默认" }
            )
            LoggerUtils.logValidationResult(
                "ServiceProvider: $className",
                true,
                details.entries.joinToString(", ") { "${it.key}: ${it.value}" }
            )
        }
        
        val serviceInfo = ServiceProviderInfo(
            className = className,
            packageName = packageName,
            simpleName = simpleName,
            interfaces = interfaces,
            singleton = singleton,
            dependencies = dependencies,
            priority = priority,
            lazy = lazy,
            module = module
        )
        
        serviceProviders.add(serviceInfo)
        LoggerUtils.logDebug("已添加服务提供者: $className，接口: ${interfaces.joinToString()}")
        
    } catch (e: Exception) {
        LoggerUtils.logError("处理服务提供者 $className 时发生错误: ${e.message}")
    }
    }
    
    private fun processServiceModule(symbol: KSClassDeclaration) {
        val annotation = symbol.annotations.find { 
            it.annotationType.resolve().declaration.qualifiedName?.asString() == SERVICE_MODULE_ANNOTATION 
        } ?: return
        
        val className = symbol.qualifiedName?.asString() ?: return
        val packageName = symbol.packageName.asString()
        val simpleName = symbol.simpleName.asString()
        
        LoggerUtils.logDebug("正在处理服务模块: $className")
        
        try {
            // 验证注解参数
            if (!validateAnnotationParameters(annotation, listOf("name"))) {
                LoggerUtils.logError("服务模块 $className 的注解参数验证失败")
                return
            }
            
            // 使用类型安全的注解解析方法
            val name = getAnnotationStringValue(annotation, "name", simpleName)
            val description = getAnnotationStringValue(annotation, "description", "")
            val version = getAnnotationStringValue(annotation, "version", "1.0.0")
            val dependencies = getAnnotationArrayValue(annotation, "dependencies")
            val autoLoad = getAnnotationBooleanValue(annotation, "autoLoad", true)
            val priority = getAnnotationIntValue(annotation, "priority", 0)
            
            // 验证模块信息
            if (name.isBlank()) {
                LoggerUtils.logWarn("服务模块 $className 的名称为空，使用类名作为默认名称")
            }
            
            // 验证版本格式
            if (!version.matches(Regex("^\\d+\\.\\d+\\.\\d+.*$"))) {
                LoggerUtils.logWarn("服务模块 $className 的版本格式不规范: $version")
            }
            
            // 验证优先级范围
            if (priority < 0 || priority > 100) {
                LoggerUtils.logWarn("服务模块 $className 的优先级超出推荐范围 [0-100]: $priority")
            }
            
            // 验证依赖项
            val validDependencies = dependencies.filter { dep ->
                if (dep.isBlank()) {
                    LoggerUtils.logWarn("服务模块 $className 包含空白依赖项")
                    false
                } else {
                    true
                }
            }
            
            if (showDetailedValidation) {
                val details = mapOf(
                    "模块名称" to name,
                    "版本" to version,
                    "描述" to description.ifEmpty { "无" },
                    "依赖数量" to validDependencies.size,
                    "自动加载" to autoLoad,
                    "优先级" to priority
                )
                LoggerUtils.logValidationResult(
                    "ServiceModule: $className",
                    true,
                    details.entries.joinToString(", ") { "${it.key}: ${it.value}" }
                )
            }
            
            val moduleInfo = ServiceModuleInfo(
                className = className,
                packageName = packageName,
                simpleName = simpleName,
                name = name,
                description = description,
                version = version,
                dependencies = validDependencies,
                autoLoad = autoLoad,
                priority = priority
            )
            
            serviceModules.add(moduleInfo)
            LoggerUtils.logDebug("已添加服务模块: $name (v$version)")
            LoggerUtils.logDebug("Added service module: $name")
            
        } catch (e: Exception) {
            LoggerUtils.logError("处理服务模块 $className 时发生错误: ${e.message}")
        }
    }
    
    private fun generateServiceRegistry() {
        val startTime = System.currentTimeMillis()
        
        try {
            // 边界情况检查
            if (serviceProviders.isEmpty() && serviceModules.isEmpty()) {
                LoggerUtils.logInfo("未发现服务或模块，跳过注册表生成")
                return
            }
            
            // 验证包名和类名
            if (serviceRegistryPackage.isBlank()) {
                LoggerUtils.logError("服务注册表包名不能为空")
                throw IllegalArgumentException("服务注册表包名不能为空")
            }
            
            if (serviceRegistryClassName.isBlank()) {
                LoggerUtils.logError("服务注册表类名不能为空")
                throw IllegalArgumentException("服务注册表类名不能为空")
            }
            
            // 验证包名格式
            if (!serviceRegistryPackage.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$"))) {
                LoggerUtils.logWarn("服务注册表包名格式可能不正确: $serviceRegistryPackage")
            }
            
            // 验证类名格式
            if (!serviceRegistryClassName.matches(Regex("^[A-Z][a-zA-Z0-9_]*$"))) {
                LoggerUtils.logWarn("服务注册表类名格式可能不正确: $serviceRegistryClassName")
            }
            
            LoggerUtils.logInfo("开始生成服务注册表：${serviceProviders.size} 个服务，${serviceModules.size} 个模块")
            
            // 验证依赖关系
            if (validateDependencies) {
                LoggerUtils.logInfo("开始验证服务依赖关系...")
                try {
                    validateServiceDependencies()
                } catch (e: Exception) {
                    LoggerUtils.logError("依赖关系验证失败: ${e.message}")
                    throw e
                }
            }
            
            // 生成代码
            LoggerUtils.logInfo("开始生成代码...")
            val serviceRegistryCodeGenerator = try {
                ServiceRegistryCodeGenerator(
                    packageName = serviceRegistryPackage,
                    className = serviceRegistryClassName,
                    serviceProviders = serviceProviders,
                    serviceModules = serviceModules,
                    generateDocumentation = generateDocumentation
                )
            } catch (e: Exception) {
                LoggerUtils.logError("创建代码生成器失败: ${e.message}")
                throw e
            }
            
            val fileSpec = try {
                serviceRegistryCodeGenerator.generate()
            } catch (e: Exception) {
                LoggerUtils.logError("生成代码失败: ${e.message}")
                throw e
            }
            
            // 验证生成的代码
            if (fileSpec == null) {
                LoggerUtils.logError("生成的文件规范为 null")
                throw IllegalStateException("生成的文件规范为 null")
            }
            
            // 写入文件
            LoggerUtils.logFileOperation("写入", "$serviceRegistryPackage.$serviceRegistryClassName.kt")
            
            val file = try {
                this.codeGenerator.createNewFile(
                    dependencies = Dependencies(false),
                    packageName = serviceRegistryPackage,
                    fileName = serviceRegistryClassName
                )
            } catch (e: Exception) {
                LoggerUtils.logError("创建输出文件失败: ${e.message}")
                throw e
            }
            
            try {
                file.use { outputStream ->
                    outputStream.writer().use { writer ->
                        fileSpec.writeTo(writer)
                    }
                }
            } catch (e: Exception) {
                LoggerUtils.logError("写入文件失败: ${e.message}")
                throw e
            }
            
            if (showPerformanceStats) {
                val duration = System.currentTimeMillis() - startTime
                LoggerUtils.logPerformance("generateServiceRegistry", duration)
            }
            
            LoggerUtils.logGenerationResult(
                "ServiceRegistry",
                "$serviceRegistryPackage.$serviceRegistryClassName",
                mapOf(
                    "服务数量" to serviceProviders.size,
                    "模块数量" to serviceModules.size,
                    "生成文档" to generateDocumentation,
                    "包名" to serviceRegistryPackage,
                    "类名" to serviceRegistryClassName
                )
            )
            
        } catch (e: Exception) {
            LoggerUtils.logError("生成服务注册表时发生错误: ${e.message}")
            LoggerUtils.logError("错误堆栈: ${e.stackTraceToString()}")
            throw e
        }
    }
    
    private fun validateServiceDependencies() {
        val startTime = System.currentTimeMillis()
        val availableInterfaces = serviceProviders.flatMap { it.interfaces }.toSet()
        
        LoggerUtils.logInfo("验证 ${serviceProviders.size} 个服务的依赖关系...")
        LoggerUtils.logDebug("可用接口: ${availableInterfaces.joinToString(", ")}")
        
        var missingDependencies = 0
        serviceProviders.forEach { service ->
            service.dependencies.forEach { dependency ->
                if (dependency !in availableInterfaces) {
                    LoggerUtils.logWarn("服务 ${service.className} 依赖 $dependency，但未找到提供者")
                    missingDependencies++
                }
            }
        }
        
        if (missingDependencies > 0) {
            LoggerUtils.logWarn("发现 $missingDependencies 个缺失的依赖")
        } else {
            LoggerUtils.logInfo("所有服务依赖验证通过")
        }
        
        // 检查循环依赖
        LoggerUtils.logInfo("检查循环依赖...")
        detectCircularDependencies()
        
        if (showPerformanceStats) {
            val duration = System.currentTimeMillis() - startTime
            LoggerUtils.logPerformance("validateServiceDependencies", duration)
        }
    }
    
    private fun detectCircularDependencies() {
        val dependencyGraph = buildDependencyGraph()
        val visited = mutableSetOf<String>()
        val recursionStack = mutableSetOf<String>()
        val circularDependencies = mutableListOf<List<String>>()
        
        // 对每个服务进行深度优先搜索
        for (service in dependencyGraph.keys) {
            if (service !in visited) {
                val path = mutableListOf<String>()
                if (detectCircularDependencyDFS(service, dependencyGraph, visited, recursionStack, path, circularDependencies)) {
                    LoggerUtils.logWarn("检测到循环依赖: ${path.joinToString(" -> ")}")
                }
            }
        }
        
        if (circularDependencies.isNotEmpty()) {
            LoggerUtils.logError("发现 ${circularDependencies.size} 个循环依赖:")
            circularDependencies.forEach { cycle ->
                LoggerUtils.logError("  循环: ${cycle.joinToString(" -> ")} -> ${cycle.first()}")
            }
            throw IllegalStateException("存在循环依赖，无法继续处理")
        } else {
            LoggerUtils.logInfo("未发现循环依赖")
        }
    }
    
    private fun buildDependencyGraph(): Map<String, Set<String>> {
        val graph = mutableMapOf<String, MutableSet<String>>()
        
        // 构建服务提供者的依赖图
        serviceProviders.forEach { service ->
            service.interfaces.forEach { interfaceName ->
                graph.getOrPut(interfaceName) { mutableSetOf() }.addAll(service.dependencies)
            }
            // 如果服务本身也作为依赖项，添加到图中
            if (service.className !in graph) {
                graph[service.className] = service.dependencies.toMutableSet()
            }
        }
        
        // 构建模块的依赖图
        serviceModules.forEach { module ->
            graph.getOrPut(module.className) { mutableSetOf() }.addAll(module.dependencies)
        }
        
        return graph
    }
    
    private fun detectCircularDependencyDFS(
        current: String,
        graph: Map<String, Set<String>>,
        visited: MutableSet<String>,
        recursionStack: MutableSet<String>,
        path: MutableList<String>,
        circularDependencies: MutableList<List<String>>
    ): Boolean {
        visited.add(current)
        recursionStack.add(current)
        path.add(current)
        
        val dependencies = graph[current] ?: emptySet()
        
        for (dependency in dependencies) {
            if (dependency in recursionStack) {
                // 找到循环依赖
                val cycleStartIndex = path.indexOf(dependency)
                if (cycleStartIndex >= 0) {
                    val cycle = path.subList(cycleStartIndex, path.size).toList()
                    circularDependencies.add(cycle)
                    return true
                }
            } else if (dependency !in visited) {
                if (detectCircularDependencyDFS(dependency, graph, visited, recursionStack, path, circularDependencies)) {
                    return true
                }
            }
        }
        
        recursionStack.remove(current)
        path.removeAt(path.size - 1)
        return false
    }
    
    private fun getAnnotationValue(annotation: KSAnnotation, name: String): Any? {
        return try {
            annotation.arguments.find { it.name?.asString() == name }?.value
        } catch (e: Exception) {
            LoggerUtils.logError("获取注解参数 '$name' 时发生错误: ${e.message}")
            null
        }
    }
    
    private fun getAnnotationStringValue(annotation: KSAnnotation, name: String, defaultValue: String = ""): String {
        return try {
            val value = getAnnotationValue(annotation, name)
            when (value) {
                is String -> value
                null -> defaultValue
                else -> {
                    LoggerUtils.logWarn("注解参数 '$name' 类型不匹配，期望 String，实际为 ${value::class.simpleName}")
                    value.toString()
                }
            }
        } catch (e: Exception) {
            LoggerUtils.logError("解析注解字符串参数 '$name' 时发生错误: ${e.message}")
            defaultValue
        }
    }
    
    private fun getAnnotationBooleanValue(annotation: KSAnnotation, name: String, defaultValue: Boolean = false): Boolean {
        return try {
            val value = getAnnotationValue(annotation, name)
            when (value) {
                is Boolean -> value
                null -> defaultValue
                else -> {
                    LoggerUtils.logWarn("注解参数 '$name' 类型不匹配，期望 Boolean，实际为 ${value::class.simpleName}")
                    defaultValue
                }
            }
        } catch (e: Exception) {
            LoggerUtils.logError("解析注解布尔参数 '$name' 时发生错误: ${e.message}")
            defaultValue
        }
    }
    
    private fun getAnnotationIntValue(annotation: KSAnnotation, name: String, defaultValue: Int = 0): Int {
        return try {
            val value = getAnnotationValue(annotation, name)
            when (value) {
                is Int -> value
                is Number -> value.toInt()
                null -> defaultValue
                else -> {
                    LoggerUtils.logWarn("注解参数 '$name' 类型不匹配，期望 Int，实际为 ${value::class.simpleName}")
                    defaultValue
                }
            }
        } catch (e: Exception) {
            LoggerUtils.logError("解析注解整数参数 '$name' 时发生错误: ${e.message}")
            defaultValue
        }
    }
    
    private fun getAnnotationArrayValue(annotation: KSAnnotation, name: String): List<String> {
        return try {
            val argument = annotation.arguments.find { it.name?.asString() == name }
            when (val value = argument?.value) {
                is List<*> -> value.mapNotNull { item ->
                    try {
                        when (item) {
                            is KSType -> {
                                val qualifiedName = item.declaration.qualifiedName?.asString()
                                if (qualifiedName.isNullOrBlank()) {
                                    LoggerUtils.logWarn("无法获取类型的限定名: $item")
                                    null
                                } else {
                                    qualifiedName
                                }
                            }
                            is String -> item
                            null -> null
                            else -> {
                                LoggerUtils.logWarn("数组参数 '$name' 中的项类型不支持: ${item::class.simpleName}")
                                item.toString()
                            }
                        }
                    } catch (e: Exception) {
                        LoggerUtils.logError("处理数组参数 '$name' 中的项时发生错误: ${e.message}")
                        null
                    }
                }
                is Array<*> -> value.mapNotNull { item ->
                    try {
                        when (item) {
                            is KSType -> item.declaration.qualifiedName?.asString()
                            is String -> item
                            null -> null
                            else -> item.toString()
                        }
                    } catch (e: Exception) {
                        LoggerUtils.logError("处理数组参数 '$name' 中的项时发生错误: ${e.message}")
                        null
                    }
                }
                null -> emptyList()
                else -> {
                    LoggerUtils.logWarn("注解参数 '$name' 不是数组类型，实际为 ${value::class.simpleName}")
                    emptyList()
                }
            }
        } catch (e: Exception) {
            LoggerUtils.logError("解析注解数组参数 '$name' 时发生错误: ${e.message}")
            emptyList()
        }
    }
    
    private fun validateAnnotationParameters(annotation: KSAnnotation, requiredParams: List<String>): Boolean {
        return try {
            val availableParams = annotation.arguments.mapNotNull { it.name?.asString() }.toSet()
            val missingParams = requiredParams.filter { it !in availableParams }
            
            if (missingParams.isNotEmpty()) {
                LoggerUtils.logWarn("注解缺少必需参数: ${missingParams.joinToString(", ")}")
                return false
            }
            true
        } catch (e: Exception) {
            LoggerUtils.logError("验证注解参数时发生错误: ${e.message}")
            false
        }
    }
}

/**
 * 服务提供者信息
 */
data class ServiceProviderInfo(
    val className: String,
    val packageName: String,
    val simpleName: String,
    val interfaces: List<String>,
    val singleton: Boolean,
    val dependencies: List<String>,
    val priority: Int,
    val lazy: Boolean,
    val module: String
)

/**
 * 服务模块信息
 */
data class ServiceModuleInfo(
    val className: String,
    val packageName: String,
    val simpleName: String,
    val name: String,
    val description: String,
    val version: String,
    val dependencies: List<String>,
    val autoLoad: Boolean,
    val priority: Int
)