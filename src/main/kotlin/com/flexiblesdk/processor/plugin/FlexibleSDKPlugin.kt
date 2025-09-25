package com.flexiblesdk.processor.plugin

import com.flexiblesdk.processor.utils.LoggerUtils
import com.google.devtools.ksp.gradle.KspExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.register

/**
 * FlexibleSDK Gradle 插件
 * 
 * 提供自动化的 KSP 配置和服务发现功能
 */
class FlexibleSDKPlugin : Plugin<Project> {
    
    companion object {
        const val EXTENSION_NAME = "flexibleSDK"
        const val KSP_PLUGIN_ID = "com.google.devtools.ksp"
        const val PROCESSOR_GROUP_ID = "com.github.Redamancywu"
        const val PROCESSOR_ARTIFACT_ID = "FlexibleSDK-Processor-Standalone"
    }
    
    override fun apply(project: Project) {
        // 创建插件扩展
        val extension = project.extensions.create<FlexibleSDKExtension>(EXTENSION_NAME)
        
        // 确保 KSP 插件已应用
        project.pluginManager.apply(KSP_PLUGIN_ID)
        
        // 配置项目
        configureProject(project, extension)
        
        // 配置 KSP
        configureKSP(project, extension)
        
        // 配置依赖
        configureDependencies(project)
        
        // 配置任务
        configureTasks(project, extension)
    }
    
    private fun configureProject(project: Project, extension: FlexibleSDKExtension) {
        project.afterEvaluate {
            // 初始化日志配置
            LoggerUtils.isDebugEnabled = extension.enableDebugLogging.get()
            LoggerUtils.currentLogLevel = when (extension.logLevel.get().uppercase()) {
                "DEBUG" -> LoggerUtils.LogLevel.DEBUG
                "INFO" -> LoggerUtils.LogLevel.INFO
                "WARN" -> LoggerUtils.LogLevel.WARN
                "ERROR" -> LoggerUtils.LogLevel.ERROR
                else -> LoggerUtils.LogLevel.INFO
            }
            
            LoggerUtils.startProcess("FlexibleSDK 插件配置", "Plugin")
            LoggerUtils.info("应用 FlexibleSDK 插件到项目: ${project.name}", "Plugin")
            
            // 输出配置信息
            LoggerUtils.config("serviceRegistryPackage", extension.serviceRegistryPackage.get(), "Plugin")
            LoggerUtils.config("serviceRegistryClassName", extension.serviceRegistryClassName.get(), "Plugin")
            LoggerUtils.config("enableDebugLogging", extension.enableDebugLogging.get(), "Plugin")
            LoggerUtils.config("logLevel", extension.logLevel.get(), "Plugin")
            LoggerUtils.config("showProgress", extension.showProgress.get(), "Plugin")
            LoggerUtils.config("showPerformanceStats", extension.showPerformanceStats.get(), "Plugin")
            LoggerUtils.config("showDetailedValidation", extension.showDetailedValidation.get(), "Plugin")
        }
        
        project.logger.info("Applying FlexibleSDK plugin to project: ${project.name}")
        
        // 配置 Android 项目（如果适用）
        project.plugins.withId("com.android.library") {
            configureAndroidLibrary(project, extension)
        }
        
        project.plugins.withId("com.android.application") {
            configureAndroidApplication(project, extension)
        }
    }
    
    private fun configureAndroidLibrary(project: Project, extension: FlexibleSDKExtension) {
        project.logger.info("Configuring FlexibleSDK for Android library")
        
        // 可以在这里添加 Android 库特定的配置
        project.afterEvaluate {
            if (extension.enableDebugLogging.get()) {
                project.logger.lifecycle("FlexibleSDK: Debug logging enabled for ${project.name}")
            }
        }
    }
    
    private fun configureAndroidApplication(project: Project, extension: FlexibleSDKExtension) {
        project.logger.info("Configuring FlexibleSDK for Android application")
        
        // 可以在这里添加 Android 应用特定的配置
    }
    
    private fun configureKSP(project: Project, extension: FlexibleSDKExtension) {
        project.afterEvaluate {
            // 配置 KSP 参数
            val kspExtension = project.extensions.getByType<KspExtension>()
            
            // 传递配置参数给 KSP 处理器
            kspExtension.arg("serviceRegistryPackage", extension.serviceRegistryPackage.get())
            kspExtension.arg("serviceRegistryClassName", extension.serviceRegistryClassName.get())
            kspExtension.arg("enableDebugLogging", extension.enableDebugLogging.get().toString())
            kspExtension.arg("logLevel", extension.logLevel.get())
            kspExtension.arg("showProgress", extension.showProgress.get().toString())
            kspExtension.arg("showPerformanceStats", extension.showPerformanceStats.get().toString())
            kspExtension.arg("showDetailedValidation", extension.showDetailedValidation.get().toString())
            kspExtension.arg("enableIncremental", extension.enableIncremental.get().toString())
            kspExtension.arg("validateDependencies", extension.validateDependencies.get().toString())
            kspExtension.arg("generateDocumentation", extension.generateDocumentation.get().toString())
            kspExtension.arg("outputDirectory", extension.outputDirectory.get())
            
            // 排除和包含的包
            if (extension.excludePackages.get().isNotEmpty()) {
                kspExtension.arg("excludePackages", extension.excludePackages.get().joinToString(","))
            }
            
            if (extension.includePackages.get().isNotEmpty()) {
                kspExtension.arg("includePackages", extension.includePackages.get().joinToString(","))
            }
            
            project.logger.info("KSP configured with FlexibleSDK parameters")
        }
    }
    
    private fun configureDependencies(project: Project) {
        project.dependencies {
            val processorVersion = getProcessorVersion(project)
            val processorArtifact = "$PROCESSOR_GROUP_ID:$PROCESSOR_ARTIFACT_ID:$processorVersion"
            
            // 自动添加处理器依赖
            add("ksp", processorArtifact)
            
            // 添加注解依赖（编译时）
            add("compileOnly", processorArtifact)
        }
    }
    
    private fun configureTasks(project: Project, extension: FlexibleSDKExtension) {
        // 暂时简化任务配置，避免编译错误
        project.logger.info("FlexibleSDK tasks configuration completed")
    }
    
    private fun validateConfiguration(extension: FlexibleSDKExtension) {
        val packageName = extension.serviceRegistryPackage.get()
        val className = extension.serviceRegistryClassName.get()
        
        require(packageName.isNotBlank()) {
            "serviceRegistryPackage cannot be blank"
        }
        
        require(className.isNotBlank()) {
            "serviceRegistryClassName cannot be blank"
        }
        
        require(packageName.matches(Regex("^[a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*$"))) {
            "serviceRegistryPackage must be a valid Java package name: $packageName"
        }
        
        require(className.matches(Regex("^[A-Z][a-zA-Z0-9_]*$"))) {
            "serviceRegistryClassName must be a valid Java class name: $className"
        }
    }
    
    private fun printConfigurationInfo(project: Project, extension: FlexibleSDKExtension) {
        project.logger.lifecycle("=== FlexibleSDK Configuration ===")
        project.logger.lifecycle("Service Registry Package: ${extension.serviceRegistryPackage.get()}")
        project.logger.lifecycle("Service Registry Class: ${extension.serviceRegistryClassName.get()}")
        project.logger.lifecycle("Debug Logging: ${extension.enableDebugLogging.get()}")
        project.logger.lifecycle("Incremental: ${extension.enableIncremental.get()}")
        project.logger.lifecycle("Validate Dependencies: ${extension.validateDependencies.get()}")
        project.logger.lifecycle("Generate Documentation: ${extension.generateDocumentation.get()}")
        project.logger.lifecycle("Output Directory: ${extension.outputDirectory.get()}")
        
        if (extension.excludePackages.get().isNotEmpty()) {
            project.logger.lifecycle("Exclude Packages: ${extension.excludePackages.get()}")
        }
        
        if (extension.includePackages.get().isNotEmpty()) {
            project.logger.lifecycle("Include Packages: ${extension.includePackages.get()}")
        }
        
        project.logger.lifecycle("================================")
    }
    
    private fun getProcessorVersion(project: Project): String {
        // 尝试从项目属性获取版本
        project.findProperty("flexibleSDK.processorVersion")?.toString()?.let { return it }
        
        // 尝试从gradle.properties获取版本
        project.findProperty("flexibleSDKProcessorVersion")?.toString()?.let { return it }
        
        // 尝试从项目版本获取（如果是同一个项目）
        if (project.version != "unspecified") {
            return project.version.toString()
        }
        
        // 默认版本（建议在实际使用中配置具体版本）
        project.logger.warn("FlexibleSDK: 未找到处理器版本配置，使用默认版本。建议在gradle.properties中设置flexibleSDKProcessorVersion属性。")
        return "1.0.0"
    }
}