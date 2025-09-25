package com.flexiblesdk.processor.ksp

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.flexiblesdk.processor.utils.LoggerUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 服务注册表代码生成器
 */
class ServiceRegistryCodeGenerator(
    private val packageName: String,
    private val className: String,
    private val serviceProviders: List<ServiceProviderInfo>,
    private val serviceModules: List<ServiceModuleInfo>,
    private val generateDocumentation: Boolean = false
) {
    
    init {
        validateInputParameters()
    }
    
    companion object {
        private const val SERVICE_INFO_CLASS = "ServiceInfo"
        private const val MODULE_INFO_CLASS = "ModuleInfo"
    }
    
    /**
     * 验证输入参数的有效性
     */
    private fun validateInputParameters() {
        if (packageName.isBlank()) {
            throw IllegalArgumentException("包名不能为空。请提供有效的包名，例如：'com.example.sdk'")
        }
        if (!packageName.matches(Regex("^[a-z][a-z0-9_]*(\\.[a-z][a-z0-9_]*)*$"))) {
            throw IllegalArgumentException("包名格式无效：'$packageName'。包名应该符合Java包命名规范，例如：'com.example.sdk'")
        }
        if (className.isBlank()) {
            throw IllegalArgumentException("类名不能为空。请提供有效的类名，例如：'ServiceRegistry'")
        }
        if (!className.matches(Regex("^[A-Z][a-zA-Z0-9_]*$"))) {
            throw IllegalArgumentException("类名格式无效：'$className'。类名应该以大写字母开头，例如：'ServiceRegistry'")
        }
        
        // 注意：允许创建空的注册表，这在测试场景中很有用
        // 如果两者都为空，只记录警告而不抛出异常
        if (serviceProviders.isEmpty() && serviceModules.isEmpty()) {
            LoggerUtils.warn("创建空的服务注册表：没有服务提供者和服务模块。这通常用于测试场景。")
        }
        
        serviceProviders.forEachIndexed { index, provider ->
            try {
                validateServiceProvider(provider)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("服务提供者 #${index + 1} 验证失败: ${e.message}")
            }
        }
        
        serviceModules.forEachIndexed { index, module ->
            try {
                validateServiceModule(module)
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("服务模块 #${index + 1} 验证失败: ${e.message}")
            }
        }
        
        // 检查重复的服务类名
        val duplicateServices = serviceProviders.groupBy { it.className }
            .filter { it.value.size > 1 }
            .keys
        if (duplicateServices.isNotEmpty()) {
            throw IllegalArgumentException(
                "发现重复的服务类名：${duplicateServices.joinToString()}。\n" +
                "每个服务类名必须唯一。请检查并修改重复的服务类名。"
            )
        }
        
        // 检查重复的模块名称
        val duplicateModules = serviceModules.groupBy { it.name }
            .filter { it.value.size > 1 }
            .keys
        if (duplicateModules.isNotEmpty()) {
            throw IllegalArgumentException(
                "发现重复的模块名称：${duplicateModules.joinToString()}。\n" +
                "每个模块名称必须唯一。请检查并修改重复的模块名称。"
            )
        }
    }
    
    private fun validateServiceProvider(provider: ServiceProviderInfo) {
        if (provider.className.isBlank()) {
            throw IllegalArgumentException("服务类名不能为空。请在 @ServiceProvider 注解中指定 className 参数")
        }
        if (!provider.className.matches(Regex("^[A-Za-z][a-zA-Z0-9_.]*$"))) {
            throw IllegalArgumentException(
                "服务类名格式无效：'${provider.className}'。\n" +
                "类名应该符合Java类命名规范，例如：'com.example.MyService' 或 'MyService'"
            )
        }
        // 模块名是可选的，可以为空
        if (provider.module.isNotBlank() && !provider.module.matches(Regex("^[A-Za-z][a-zA-Z0-9_-]*$"))) {
            throw IllegalArgumentException(
                "服务模块名格式无效：'${provider.module}'。\n" +
                "模块名应该符合标识符命名规范，例如：'core'、'userModule' 或 'test-module'"
            )
        }
        if (provider.priority < 0) {
            throw IllegalArgumentException(
                "服务优先级不能为负数：${provider.priority}。请设置为 0 或正整数，数值越大优先级越高"
            )
        }
        provider.interfaces.forEach { interfaceName ->
            if (interfaceName.isBlank()) {
                throw IllegalArgumentException(
                    "接口名不能为空。请检查 @ServiceProvider 注解中的 interfaces 参数"
                )
            }
            if (!interfaceName.matches(Regex("^[A-Za-z][a-zA-Z0-9_.]*$"))) {
                throw IllegalArgumentException(
                    "接口名格式无效：'$interfaceName'。\n" +
                    "接口名应该符合Java接口命名规范，例如：'com.example.IMyService' 或 'IMyService'"
                )
            }
        }
        provider.dependencies.forEach { dependency ->
            if (dependency.isBlank()) {
                throw IllegalArgumentException(
                    "依赖项不能为空。请检查 @ServiceProvider 注解中的 dependencies 参数"
                )
            }
        }
    }
    
    private fun validateServiceModule(module: ServiceModuleInfo) {
        if (module.name.isBlank()) {
            throw IllegalArgumentException("模块名称不能为空。请在 @ServiceModule 注解中指定 name 参数")
        }
        if (module.className.isBlank()) {
            throw IllegalArgumentException("模块类名不能为空。请在 @ServiceModule 注解中指定 className 参数")
        }
        if (!module.className.matches(Regex("^[A-Za-z][a-zA-Z0-9_.]*$"))) {
            throw IllegalArgumentException(
                "模块类名格式无效：'${module.className}'。\n" +
                "类名应该符合Java类命名规范，例如：'com.example.MyModule' 或 'MyModule'"
            )
        }
        if (module.version.isBlank()) {
            throw IllegalArgumentException(
                "模块版本不能为空。请在 @ServiceModule 注解中指定 version 参数，例如：version = \"1.0.0\""
            )
        }
        if (module.priority < 0) {
            throw IllegalArgumentException(
                "模块优先级不能为负数：${module.priority}。请设置为 0 或正整数，数值越大优先级越高"
            )
        }
        module.dependencies.forEach { dependency ->
            if (dependency.isBlank()) {
                throw IllegalArgumentException(
                    "模块依赖项不能为空。请检查 @ServiceModule 注解中的 dependencies 参数"
                )
            }
        }
    }

    // 缓存机制
    private val typeCache = mutableMapOf<String, ClassName>()
    private val parameterizedTypeCache = mutableMapOf<String, ParameterizedTypeName>()
    private val codeBlockCache = mutableMapOf<String, CodeBlock>()
    private val serviceInfoClassName by lazy { getCachedClassName(SERVICE_INFO_CLASS) }
    private val moduleInfoClassName by lazy { getCachedClassName(MODULE_INFO_CLASS) }
    private val stringClassName by lazy { String::class.asClassName() }
    private val booleanClassName by lazy { Boolean::class.asClassName() }
    private val intClassName by lazy { Int::class.asClassName() }
    private val listClassName by lazy { List::class.asClassName() }
    
    // 延迟生成的组件
    private val serviceInfoClassSpec by lazy { generateServiceInfoClass() }
    private val moduleInfoClassSpec by lazy { generateModuleInfoClass() }
    private val serviceRegistryClassSpec by lazy { generateServiceRegistryClass() }
    
    // 延迟生成的映射表
    private val servicesMapCode by lazy { generateServicesMap() }
    private val modulesMapCode by lazy { generateModulesMap() }
    
    // 延迟生成的函数
    private val getServiceFunctionSpec by lazy { generateGetServiceFunction() }
    private val getModuleFunctionSpec by lazy { generateGetModuleFunction() }
    private val getServicesByInterfaceFunctionSpec by lazy { generateGetServicesByInterfaceFunction() }
    private val getServicesByModuleFunctionSpec by lazy { generateGetServicesByModuleFunction() }
    private val getAllServicesFunctionSpec by lazy { generateGetAllServicesFunction() }
    private val getAllModulesFunctionSpec by lazy { generateGetAllModulesFunction() }
    
    /**
     * 获取缓存的 ClassName
     */
    private fun getCachedClassName(simpleName: String): ClassName {
        val key = "$packageName.$simpleName"
        return typeCache.getOrPut(key) { ClassName(packageName, simpleName) }
    }
    
    /**
     * 获取缓存的 ParameterizedTypeName
     */
    private fun getCachedParameterizedType(baseType: ClassName, vararg typeArguments: TypeName): ParameterizedTypeName {
        val key = "$baseType<${typeArguments.joinToString(",")}>"
        return parameterizedTypeCache.getOrPut(key) { baseType.parameterizedBy(*typeArguments) }
    }
    
    /**
     * 获取缓存的 CodeBlock
     */
    private fun getCachedCodeBlock(key: String, generator: () -> CodeBlock): CodeBlock {
        return codeBlockCache.getOrPut(key, generator)
    }

    fun generate(): FileSpec {
        LoggerUtils.info("开始生成服务注册表代码: 包名='$packageName', 类名='$className'")
        LoggerUtils.debug("服务提供者数量: ${serviceProviders.size}, 服务模块数量: ${serviceModules.size}")
        
        try {
            LoggerUtils.debug("生成文件头部信息...")
            val fileHeader = generateFileHeader()
            val fileSpecBuilder = FileSpec.builder(packageName, className)
                .addFileComment(fileHeader)
            
            if (generateDocumentation) {
                LoggerUtils.debug("添加文档注释...")
                fileSpecBuilder.addFileComment("生成文档: 已启用")
            }
            
            // 条件性添加 ServiceInfo 类
            if (serviceProviders.isNotEmpty()) {
                LoggerUtils.debug("生成 ServiceInfo 类...")
                try {
                    fileSpecBuilder.addType(serviceInfoClassSpec)
                    LoggerUtils.debug("ServiceInfo 类生成成功")
                } catch (e: Exception) {
                    LoggerUtils.error("生成 ServiceInfo 类失败: ${e.message}", throwable = e)
                    throw CodeGenerationException("生成 ServiceInfo 类失败", e)
                }
            } else {
                LoggerUtils.debug("跳过 ServiceInfo 类生成（无服务提供者）")
            }
            
            // 条件性添加 ModuleInfo 类
            if (serviceModules.isNotEmpty()) {
                LoggerUtils.debug("生成 ModuleInfo 类...")
                try {
                    fileSpecBuilder.addType(moduleInfoClassSpec)
                    LoggerUtils.debug("ModuleInfo 类生成成功")
                } catch (e: Exception) {
                    LoggerUtils.error("生成 ModuleInfo 类失败: ${e.message}", throwable = e)
                    throw CodeGenerationException("生成 ModuleInfo 类失败", e)
                }
            } else {
                LoggerUtils.debug("跳过 ModuleInfo 类生成（无服务模块）")
            }
            
            // 添加主注册表类
            LoggerUtils.debug("生成主服务注册表类...")
            try {
                fileSpecBuilder.addType(serviceRegistryClassSpec)
                LoggerUtils.debug("主服务注册表类生成成功")
            } catch (e: Exception) {
                LoggerUtils.error("生成主服务注册表类失败: ${e.message}", throwable = e)
                throw CodeGenerationException("生成主服务注册表类失败", e)
            }
            
            LoggerUtils.debug("构建最终文件规范...")
            val fileSpec = try {
                fileSpecBuilder.build()
            } catch (e: Exception) {
                LoggerUtils.error("构建文件规范失败: ${e.message}", throwable = e)
                throw CodeGenerationException("构建文件规范失败", e)
            }
            
            LoggerUtils.info("服务注册表代码生成完成: ${fileSpec.name}.kt")
            LoggerUtils.debug("生成的文件包含 ${fileSpec.members.size} 个顶级成员")
            
            return fileSpec
            
        } catch (e: CodeGenerationException) {
            LoggerUtils.error("代码生成过程中发生已知错误: ${e.message}", throwable = e)
            throw e
        } catch (e: Exception) {
            LoggerUtils.error("代码生成过程中发生未知错误: ${e.message}", throwable = e)
            throw CodeGenerationException("代码生成过程中发生未知错误", e)
        }
    }
    
    /**
     * 代码生成异常类
     */
    class CodeGenerationException(
        message: String,
        cause: Throwable? = null
    ) : RuntimeException("代码生成错误: $message", cause)

    private fun generateFileHeader(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        return """
            Generated by FlexibleSDK Processor
            Generated at: $timestamp
            
            This file contains the service registry for FlexibleSDK.
            Do not modify this file manually as it will be overwritten.
            
            Services: ${serviceProviders.size}
            Modules: ${serviceModules.size}
        """.trimIndent()
    }
    
    private fun generateServiceInfoClass(): TypeSpec {
        val stringListType = getCachedParameterizedType(listClassName, stringClassName)
        
        return TypeSpec.classBuilder(SERVICE_INFO_CLASS)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("className", stringClassName)
                    .addParameter("interfaces", stringListType)
                    .addParameter("singleton", booleanClassName)
                    .addParameter("dependencies", stringListType)
                    .addParameter("priority", intClassName)
                    .addParameter("lazy", booleanClassName)
                    .addParameter("module", stringClassName)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("className", stringClassName)
                    .initializer("className")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("interfaces", stringListType)
                    .initializer("interfaces")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("singleton", booleanClassName)
                    .initializer("singleton")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("dependencies", stringListType)
                    .initializer("dependencies")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("priority", intClassName)
                    .initializer("priority")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("lazy", booleanClassName)
                    .initializer("lazy")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("module", stringClassName)
                    .initializer("module")
                    .build()
            )
            .apply {
                if (generateDocumentation) {
                    addKdoc("""
                        服务信息数据类
                        
                        @property className 服务类的完整类名
                        @property interfaces 服务实现的接口列表
                        @property singleton 是否为单例模式
                        @property dependencies 服务依赖的其他服务列表
                        @property priority 服务优先级
                        @property lazy 是否延迟初始化
                        @property module 所属模块名称
                    """.trimIndent())
                }
            }
            .build()
    }
    
    private fun generateModuleInfoClass(): TypeSpec {
        val stringListType = getCachedParameterizedType(listClassName, stringClassName)
        
        return TypeSpec.classBuilder(MODULE_INFO_CLASS)
            .addModifiers(KModifier.DATA)
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("className", stringClassName)
                    .addParameter("name", stringClassName)
                    .addParameter("description", stringClassName)
                    .addParameter("version", stringClassName)
                    .addParameter("dependencies", stringListType)
                    .addParameter("autoLoad", booleanClassName)
                    .addParameter("priority", intClassName)
                    .build()
            )
            .addProperty(
                PropertySpec.builder("className", stringClassName)
                    .initializer("className")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("name", stringClassName)
                    .initializer("name")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("description", stringClassName)
                    .initializer("description")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("version", stringClassName)
                    .initializer("version")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("dependencies", stringListType)
                    .initializer("dependencies")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("autoLoad", booleanClassName)
                    .initializer("autoLoad")
                    .build()
            )
            .addProperty(
                PropertySpec.builder("priority", intClassName)
                    .initializer("priority")
                    .build()
            )
            .apply {
                if (generateDocumentation) {
                    addKdoc("""
                        模块信息数据类
                        
                        @property className 模块类的完整类名
                        @property name 模块名称
                        @property description 模块描述
                        @property version 模块版本
                        @property dependencies 模块依赖的其他模块列表
                        @property autoLoad 是否自动加载
                        @property priority 模块优先级
                    """.trimIndent())
                }
            }
            .build()
    }
    
    private fun generateServiceRegistryClass(): TypeSpec {
        val servicesMapType = getCachedParameterizedType(
            Map::class.asClassName(),
            stringClassName,
            serviceInfoClassName
        )
        
        val modulesMapType = getCachedParameterizedType(
            Map::class.asClassName(),
            stringClassName,
            moduleInfoClassName
        )
        
        val builder = TypeSpec.objectBuilder(className)
            .apply {
                if (generateDocumentation) {
                    addKdoc("""
                        FlexibleSDK 服务注册表
                        
                        此类包含所有已注册的服务和模块信息，由 KSP 处理器自动生成。
                        
                        @property services 所有已注册的服务映射表
                        @property modules 所有已注册的模块映射表
                    """.trimIndent())
                }
            }
        
        // 只有在有服务提供者时才添加 services 属性和相关函数
        if (serviceProviders.isNotEmpty()) {
            builder.addProperty(
                PropertySpec.builder("services", servicesMapType)
                    .initializer(servicesMapCode)
                    .build()
            )
            .addFunction(getServiceFunctionSpec)
            .addFunction(getServicesByInterfaceFunctionSpec)
            .addFunction(getServicesByModuleFunctionSpec)
            .addFunction(getAllServicesFunctionSpec)
        } else {
            // 如果没有服务，提供空的 services 属性
            builder.addProperty(
                PropertySpec.builder("services", servicesMapType)
                    .initializer("emptyMap()")
                    .build()
            )
        }
        
        // 只有在有服务模块时才添加 modules 属性和相关函数
        if (serviceModules.isNotEmpty()) {
            builder.addProperty(
                PropertySpec.builder("modules", modulesMapType)
                    .initializer(modulesMapCode)
                    .build()
            )
            .addFunction(getModuleFunctionSpec)
            .addFunction(getAllModulesFunctionSpec)
        } else {
            // 如果没有模块，提供空的 modules 属性
            builder.addProperty(
                PropertySpec.builder("modules", modulesMapType)
                    .initializer("emptyMap()")
                    .build()
            )
        }
        
        return builder.build()
    }
    
    private fun generateServicesMap(): CodeBlock {
        return try {
            LoggerUtils.logDebug("开始生成服务映射表，包含 ${serviceProviders.size} 个服务")
            
            if (serviceProviders.isEmpty()) {
                LoggerUtils.logDebug("没有服务提供者，返回空映射表")
                return CodeBlock.of("emptyMap()")
            }
            
            // 预计算类型引用以避免重复创建
            val serviceInfoClassName = SERVICE_INFO_CLASS.asClassName(packageName)
            
            // 使用批量操作构建映射表
            val entries = serviceProviders.mapIndexed { index, service ->
                try {
                    LoggerUtils.logDebug("处理服务 ${index + 1}/${serviceProviders.size}: ${service.className}")
                    
                    // 验证服务信息的完整性
                    if (service.className.isBlank()) {
                        throw CodeGenerationException("服务 #${index + 1} 的类名不能为空")
                    }
                    
                    val serviceInfo = CodeBlock.of(
                        "%T(\n" +
                        "  className = %S,\n" +
                        "  interfaces = listOf(%L),\n" +
                        "  singleton = %L,\n" +
                        "  dependencies = listOf(%L),\n" +
                        "  priority = %L,\n" +
                        "  lazy = %L,\n" +
                        "  module = %S\n" +
                        ")",
                        serviceInfoClassName,
                        service.className,
                        service.interfaces.joinToString { "\"$it\"" },
                        service.singleton,
                        service.dependencies.joinToString { "\"$it\"" },
                        service.priority,
                        service.lazy,
                        service.module
                    )
                    
                    LoggerUtils.logDebug("成功生成服务信息: ${service.className}")
                    CodeBlock.of("%S to %L", service.className, serviceInfo)
                } catch (e: Exception) {
                    throw CodeGenerationException(
                        "生成服务 '${service.className}' 的映射信息时失败: ${e.message}",
                        e
                    )
                }
            }
            
            LoggerUtils.logDebug("所有服务映射信息生成完成，开始构建最终的映射表")
            
            val result = CodeBlock.builder()
                .add("mapOf(\n")
                .indent()
                .add(entries.joinToCode(",\n"))
                .unindent()
                .add("\n)")
                .build()
                
            LoggerUtils.logDebug("服务映射表生成完成")
            result
        } catch (e: CodeGenerationException) {
            throw e
        } catch (e: Exception) {
            throw CodeGenerationException(
                "生成服务映射表时发生未预期的错误: ${e.message}",
                e
            )
        }
    }
    
    private fun generateModulesMap(): CodeBlock {
        return try {
            LoggerUtils.logDebug("开始生成模块映射表，包含 ${serviceModules.size} 个模块")
            
            if (serviceModules.isEmpty()) {
                LoggerUtils.logDebug("没有服务模块，返回空映射表")
                return CodeBlock.of("emptyMap()")
            }
            
            // 使用批量操作构建映射表
            val entries = serviceModules.mapIndexed { index, module ->
                try {
                    LoggerUtils.logDebug("处理模块 ${index + 1}/${serviceModules.size}: ${module.name} (${module.className})")
                    
                    // 验证模块信息的完整性
                    if (module.name.isBlank()) {
                        throw CodeGenerationException("模块 #${index + 1} 的名称不能为空")
                    }
                    if (module.className.isBlank()) {
                        throw CodeGenerationException("模块 '${module.name}' 的类名不能为空")
                    }
                    
                    val moduleInfo = CodeBlock.of(
                        "%T(\n" +
                        "  className = %S,\n" +
                        "  name = %S,\n" +
                        "  description = %S,\n" +
                        "  version = %S,\n" +
                        "  dependencies = listOf(%L),\n" +
                        "  autoLoad = %L,\n" +
                        "  priority = %L\n" +
                        ")",
                        moduleInfoClassName,
                        module.className,
                        module.name,
                        module.description,
                        module.version,
                        module.dependencies.joinToString { "\"$it\"" },
                        module.autoLoad,
                        module.priority
                    )
                    
                    LoggerUtils.logDebug("成功生成模块信息: ${module.name}")
                    CodeBlock.of("%S to %L", module.name, moduleInfo)
                } catch (e: Exception) {
                    throw CodeGenerationException(
                        "生成模块 '${module.name}' (${module.className}) 的映射信息时失败: ${e.message}",
                        e
                    )
                }
            }
            
            LoggerUtils.logDebug("所有模块映射信息生成完成，开始构建最终的映射表")
            
            val result = CodeBlock.builder()
                .add("mapOf(\n")
                .indent()
                .add(entries.joinToCode(",\n"))
                .unindent()
                .add("\n)")
                .build()
                
            LoggerUtils.logDebug("模块映射表生成完成")
            result
        } catch (e: CodeGenerationException) {
            throw e
        } catch (e: Exception) {
            throw CodeGenerationException(
                "生成模块映射表时发生未预期的错误: ${e.message}",
                e
            )
        }
    }
    
    private fun generateGetServiceFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getService 函数")
            
            FunSpec.builder("getService")
                .addParameter("className", stringClassName)
                .returns(serviceInfoClassName.copy(nullable = true))
                .addStatement("return services[className]")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("根据类名获取服务信息")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getService 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getService 函数时失败: ${e.message}", e)
        }
    }
    
    private fun generateGetModuleFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getModule 函数")
            
            FunSpec.builder("getModule")
                .addParameter("name", stringClassName)
                .returns(moduleInfoClassName.copy(nullable = true))
                .addStatement("return modules[name]")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("根据名称获取模块信息")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getModule 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getModule 函数时失败: ${e.message}", e)
        }
    }
    
    private fun generateGetServicesByInterfaceFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getServicesByInterface 函数")
            
            val returnType = getCachedParameterizedType(listClassName, serviceInfoClassName)
            
            FunSpec.builder("getServicesByInterface")
                .addParameter("interfaceName", stringClassName)
                .returns(returnType)
                .addStatement("return services.values.filter { it.interfaces.contains(interfaceName) }")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("根据接口名获取实现该接口的所有服务")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getServicesByInterface 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getServicesByInterface 函数时失败: ${e.message}", e)
        }
    }
    
    private fun generateGetServicesByModuleFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getServicesByModule 函数")
            
            val returnType = getCachedParameterizedType(listClassName, serviceInfoClassName)
            
            FunSpec.builder("getServicesByModule")
                .addParameter("moduleName", stringClassName)
                .returns(returnType)
                .addStatement("return services.values.filter { it.module == moduleName }")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("根据模块名获取该模块下的所有服务")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getServicesByModule 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getServicesByModule 函数时失败: ${e.message}", e)
        }
    }
    
    private fun generateGetAllServicesFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getAllServices 函数")
            
            val returnType = getCachedParameterizedType(listClassName, serviceInfoClassName)
            
            FunSpec.builder("getAllServices")
                .returns(returnType)
                .addStatement("return services.values.toList()")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("获取所有已注册的服务")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getAllServices 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getAllServices 函数时失败: ${e.message}", e)
        }
    }
    
    private fun generateGetAllModulesFunction(): FunSpec {
        return try {
            LoggerUtils.logDebug("生成 getAllModules 函数")
            
            val returnType = getCachedParameterizedType(listClassName, moduleInfoClassName)
            
            FunSpec.builder("getAllModules")
                .returns(returnType)
                .addStatement("return modules.values.toList()")
                .apply {
                    if (generateDocumentation) {
                        addKdoc("获取所有已注册的模块")
                    }
                }
                .build()
                .also { LoggerUtils.logDebug("getAllModules 函数生成完成") }
        } catch (e: Exception) {
            throw CodeGenerationException("生成 getAllModules 函数时失败: ${e.message}", e)
        }
    }
    
    private fun String.asClassName(packageName: String): ClassName {
        return ClassName(packageName, this)
    }
    
    /**
     * 将 CodeBlock 列表连接为单个 CodeBlock
     */
    private fun List<CodeBlock>.joinToCode(separator: String = ", "): CodeBlock {
        if (isEmpty()) return CodeBlock.of("")
        if (size == 1) return first()
        
        val builder = CodeBlock.builder()
        forEachIndexed { index, codeBlock ->
            builder.add(codeBlock)
            if (index < size - 1) {
                builder.add(separator)
            }
        }
        return builder.build()
    }
}