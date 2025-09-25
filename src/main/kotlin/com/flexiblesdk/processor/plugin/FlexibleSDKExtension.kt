package com.flexiblesdk.processor.plugin

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import javax.inject.Inject

/**
 * FlexibleSDK 插件扩展配置
 * 
 * 用于在 build.gradle.kts 中配置插件行为
 */
open class FlexibleSDKExtension @Inject constructor(objects: ObjectFactory) {
    
    /**
     * 生成的 ServiceRegistry 类的包名
     * 默认值: "com.flexiblesdk.core.manager"
     */
    val serviceRegistryPackage: Property<String> = objects.property(String::class.java)
    
    /**
     * 生成的 ServiceRegistry 类名
     * 默认值: "ServiceRegistry"
     */
    val serviceRegistryClassName: Property<String> = objects.property(String::class.java)
    
    /**
     * 是否启用调试日志
     * 默认值: false
     */
    val enableDebugLogging: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 日志级别
     * 可选值: DEBUG, INFO, WARN, ERROR
     * 默认值: INFO
     */
    val logLevel: Property<String> = objects.property(String::class.java)
    
    /**
     * 是否显示处理进度
     * 默认值: true
     */
    val showProgress: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 是否显示性能统计
     * 默认值: false
     */
    val showPerformanceStats: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 是否显示详细的验证信息
     * 默认值: false
     */
    val showDetailedValidation: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 是否启用增量编译
     * 默认值: true
     */
    val enableIncremental: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 是否验证服务依赖关系
     * 默认值: true
     */
    val validateDependencies: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 是否生成服务文档
     * 默认值: false
     */
    val generateDocumentation: Property<Boolean> = objects.property(Boolean::class.java)
    
    /**
     * 输出目录
     * 默认值: "generated/source/ksp"
     */
    val outputDirectory: Property<String> = objects.property(String::class.java)
    
    /**
     * 排除的包名列表
     * 这些包下的类不会被扫描
     */
    val excludePackages: SetProperty<String> = objects.setProperty(String::class.java)
    
    /**
     * 包含的包名列表
     * 只有这些包下的类会被扫描（如果设置了此选项）
     */
    val includePackages: SetProperty<String> = objects.setProperty(String::class.java)
    
    init {
        // 设置默认值
        serviceRegistryPackage.convention("com.flexiblesdk.core.manager")
        serviceRegistryClassName.convention("ServiceRegistry")
        enableDebugLogging.convention(false)
        logLevel.convention("INFO")
        showProgress.convention(true)
        showPerformanceStats.convention(false)
        showDetailedValidation.convention(false)
        enableIncremental.convention(true)
        validateDependencies.convention(true)
        generateDocumentation.convention(false)
        outputDirectory.convention("generated/source/ksp")
        excludePackages.convention(emptySet())
        includePackages.convention(emptySet())
    }
}