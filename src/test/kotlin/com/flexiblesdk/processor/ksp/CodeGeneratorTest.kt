package com.flexiblesdk.processor.ksp

import com.squareup.kotlinpoet.TypeSpec
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * CodeGenerator 单元测试
 */
class CodeGeneratorTest {
    
    @Test
    fun `test generate empty registry`() {
        val generator = ServiceRegistryCodeGenerator(
            packageName = "com.test",
            className = "TestRegistry",
            serviceProviders = emptyList(),
            serviceModules = emptyList(),
            generateDocumentation = false
        )
        
        val fileSpec = generator.generate()
        
        assertEquals("com.test", fileSpec.packageName)
        assertEquals("TestRegistry", fileSpec.name)
        assertTrue(fileSpec.members.isNotEmpty()) // 应该包含 ServiceInfo, ModuleInfo, TestRegistry
    }
    
    @Test
    fun `test generate with services`() {
        val serviceProvider = ServiceProviderInfo(
            className = "com.test.MyService",
            packageName = "com.test",
            simpleName = "MyService",
            interfaces = listOf("com.test.IMyService"),
            singleton = true,
            dependencies = emptyList(),
            priority = 0,
            lazy = false,
            module = "test-module"
        )
        
        val generator = ServiceRegistryCodeGenerator(
            packageName = "com.test",
            className = "TestRegistry",
            serviceProviders = listOf(serviceProvider),
            serviceModules = emptyList(),
            generateDocumentation = true
        )
        
        val fileSpec = generator.generate()
        val registryClass = fileSpec.members.filterIsInstance<TypeSpec>().find { it.name == "TestRegistry" }
        
        assertNotNull(registryClass)
        assertEquals("com.test", fileSpec.packageName)
        assertEquals("TestRegistry", fileSpec.name)
    }
    
    @Test
    fun `test generate with modules`() {
        val serviceModule = ServiceModuleInfo(
            className = "com.test.MyModule",
            packageName = "com.test",
            simpleName = "MyModule",
            name = "test-module",
            description = "Test module",
            version = "1.0.0",
            dependencies = emptyList(),
            autoLoad = true,
            priority = 0
        )
        
        val generator = ServiceRegistryCodeGenerator(
            packageName = "com.test",
            className = "TestRegistry",
            serviceProviders = emptyList(),
            serviceModules = listOf(serviceModule),
            generateDocumentation = false
        )
        
        val fileSpec = generator.generate()
        val registryClass = fileSpec.members.filterIsInstance<TypeSpec>().find { it.name == "TestRegistry" }
        
        assertNotNull(registryClass)
    }
    
    @Test
    fun `test generate with both services and modules`() {
        val serviceProvider = ServiceProviderInfo(
            className = "com.test.MyService",
            packageName = "com.test",
            simpleName = "MyService",
            interfaces = listOf("com.test.IMyService"),
            singleton = true,
            dependencies = listOf("com.test.IDependency"),
            priority = 1,
            lazy = true,
            module = "test-module"
        )
        
        val serviceModule = ServiceModuleInfo(
            className = "com.test.MyModule",
            packageName = "com.test",
            simpleName = "MyModule",
            name = "test-module",
            description = "Test module",
            version = "1.0.0",
            dependencies = listOf("core-module"),
            autoLoad = true,
            priority = 1
        )
        
        val generator = ServiceRegistryCodeGenerator(
            packageName = "com.test",
            className = "TestRegistry",
            serviceProviders = listOf(serviceProvider),
            serviceModules = listOf(serviceModule),
            generateDocumentation = true
        )
        
        val fileSpec = generator.generate()
        
        assertEquals("com.test", fileSpec.packageName)
        assertEquals("TestRegistry", fileSpec.name)
        
        // 验证生成的类包含必要的成员
        val serviceInfoClass = fileSpec.members.filterIsInstance<TypeSpec>().find { it.name == "ServiceInfo" }
        val moduleInfoClass = fileSpec.members.filterIsInstance<TypeSpec>().find { it.name == "ModuleInfo" }
        val registryClass = fileSpec.members.filterIsInstance<TypeSpec>().find { it.name == "TestRegistry" }
        
        assertNotNull(serviceInfoClass)
        assertNotNull(moduleInfoClass)
        assertNotNull(registryClass)
    }
    
    @Test
    fun `test file header generation`() {
        val generator = ServiceRegistryCodeGenerator(
            packageName = "com.test",
            className = "TestRegistry",
            serviceProviders = emptyList(),
            serviceModules = emptyList(),
            generateDocumentation = false
        )
        
        val fileSpec = generator.generate()
        
        // 验证文件头注释存在
        val commentString = fileSpec.comment.toString()
        assertTrue(commentString.isNotEmpty())
        assertTrue(commentString.contains("Generated by FlexibleSDK Processor"))
        assertTrue(commentString.contains("Services: 0"))
        assertTrue(commentString.contains("Modules: 0"))
    }
}