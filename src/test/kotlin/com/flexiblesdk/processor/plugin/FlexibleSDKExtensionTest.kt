package com.flexiblesdk.processor.plugin

import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * FlexibleSDKExtension 单元测试
 */
class FlexibleSDKExtensionTest {
    
    @Test
    fun `test default values`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        assertEquals("com.flexiblesdk.core.manager", extension.serviceRegistryPackage.get())
        assertEquals("ServiceRegistry", extension.serviceRegistryClassName.get())
        assertFalse(extension.enableDebugLogging.get())
        assertEquals("INFO", extension.logLevel.get())
        assertTrue(extension.showProgress.get())
        assertFalse(extension.showPerformanceStats.get())
        assertFalse(extension.showDetailedValidation.get())
        assertTrue(extension.enableIncremental.get())
        assertTrue(extension.validateDependencies.get())
        assertFalse(extension.generateDocumentation.get())
        assertEquals("generated/source/ksp", extension.outputDirectory.get())
        assertTrue(extension.includePackages.get().isEmpty())
        assertTrue(extension.excludePackages.get().isEmpty())
    }
    
    @Test
    fun `test custom values`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        extension.serviceRegistryPackage.set("com.custom.registry")
        extension.serviceRegistryClassName.set("CustomRegistry")
        extension.enableDebugLogging.set(true)
        extension.logLevel.set("DEBUG")
        extension.showProgress.set(false)
        extension.showPerformanceStats.set(true)
        extension.showDetailedValidation.set(true)
        extension.enableIncremental.set(false)
        extension.validateDependencies.set(false)
        extension.generateDocumentation.set(true)
        extension.outputDirectory.set("custom/output")
        extension.includePackages.set(setOf("com.include"))
        extension.excludePackages.set(setOf("com.exclude"))
        
        assertEquals("com.custom.registry", extension.serviceRegistryPackage.get())
        assertEquals("CustomRegistry", extension.serviceRegistryClassName.get())
        assertTrue(extension.enableDebugLogging.get())
        assertEquals("DEBUG", extension.logLevel.get())
        assertFalse(extension.showProgress.get())
        assertTrue(extension.showPerformanceStats.get())
        assertTrue(extension.showDetailedValidation.get())
        assertFalse(extension.enableIncremental.get())
        assertFalse(extension.validateDependencies.get())
        assertTrue(extension.generateDocumentation.get())
        assertEquals("custom/output", extension.outputDirectory.get())
        assertEquals(setOf("com.include"), extension.includePackages.get())
        assertEquals(setOf("com.exclude"), extension.excludePackages.get())
    }
    
    @Test
    fun `test package filters`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        // 测试包含包设置
        extension.includePackages.set(setOf("com.flexiblesdk", "com.app"))
        assertEquals(2, extension.includePackages.get().size)
        assertTrue(extension.includePackages.get().contains("com.flexiblesdk"))
        assertTrue(extension.includePackages.get().contains("com.app"))
        
        // 测试排除包设置
        extension.excludePackages.set(setOf("com.test", "com.debug"))
        assertEquals(2, extension.excludePackages.get().size)
        assertTrue(extension.excludePackages.get().contains("com.test"))
        assertTrue(extension.excludePackages.get().contains("com.debug"))
    }
    
    @Test
    fun `test registry configuration`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        // 测试注册表包名和类名的组合
        extension.serviceRegistryPackage.set("com.myapp.core")
        extension.serviceRegistryClassName.set("MyServiceRegistry")
        
        assertEquals("com.myapp.core", extension.serviceRegistryPackage.get())
        assertEquals("MyServiceRegistry", extension.serviceRegistryClassName.get())
        
        // 验证完整类名
        val fullClassName = "${extension.serviceRegistryPackage.get()}.${extension.serviceRegistryClassName.get()}"
        assertEquals("com.myapp.core.MyServiceRegistry", fullClassName)
    }
    
    @Test
    fun `test boolean flags`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        // 测试调试日志
        assertFalse(extension.enableDebugLogging.get())
        extension.enableDebugLogging.set(true)
        assertTrue(extension.enableDebugLogging.get())
        
        // 测试增量编译
        assertTrue(extension.enableIncremental.get())
        extension.enableIncremental.set(false)
        assertFalse(extension.enableIncremental.get())
        
        // 测试依赖验证
        assertTrue(extension.validateDependencies.get())
        extension.validateDependencies.set(false)
        assertFalse(extension.validateDependencies.get())
        
        // 测试文档生成
        assertFalse(extension.generateDocumentation.get())
        extension.generateDocumentation.set(true)
        assertTrue(extension.generateDocumentation.get())
    }
    
    @Test
    fun `test log level configuration`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        // 默认日志级别
        assertEquals("INFO", extension.logLevel.get())
        
        // 设置不同的日志级别
        extension.logLevel.set("DEBUG")
        assertEquals("DEBUG", extension.logLevel.get())
        
        extension.logLevel.set("WARN")
        assertEquals("WARN", extension.logLevel.get())
        
        extension.logLevel.set("ERROR")
        assertEquals("ERROR", extension.logLevel.get())
    }
    
    @Test
    fun `test output directory configuration`() {
        val project = ProjectBuilder.builder().build()
        val extension = FlexibleSDKExtension(project.objects)
        
        // 默认值
        assertEquals("generated/source/ksp", extension.outputDirectory.get())
        
        // 设置自定义输出目录
        extension.outputDirectory.set("build/generated/flexiblesdk")
        assertEquals("build/generated/flexiblesdk", extension.outputDirectory.get())
        
        // 设置为空字符串
        extension.outputDirectory.set("")
        assertEquals("", extension.outputDirectory.get())
    }
}