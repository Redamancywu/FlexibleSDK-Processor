package com.flexiblesdk.processor.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.OutputStream
import java.io.OutputStreamWriter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ServiceRegistryProcessorTest {

    private lateinit var processor: ServiceRegistryProcessor
    private lateinit var mockCodeGenerator: CodeGenerator
    private lateinit var mockLogger: KSPLogger
    private lateinit var mockResolver: Resolver

    @BeforeEach
    fun setup() {
        mockCodeGenerator = mockk()
        mockLogger = mockk(relaxed = true)
        mockResolver = mockk()
        
        // 配置 CodeGenerator mock
        val mockOutputStream = mockk<OutputStream>(relaxed = true)
        every { mockCodeGenerator.createNewFile(any(), any(), any(), any()) } returns mockOutputStream
        
        processor = ServiceRegistryProcessor(
            codeGenerator = mockCodeGenerator,
            logger = mockLogger,
            options = mapOf(
                "serviceRegistryPackage" to "com.test.generated",
                "serviceRegistryClassName" to "TestServiceRegistry",
                "validateDependencies" to "true",
                "generateDocumentation" to "true",
                "showPerformanceStats" to "true"
            )
        )
    }

    @Test
    fun `test processor initialization with default options`() {
        val processorWithDefaults = ServiceRegistryProcessor(
            codeGenerator = mockCodeGenerator,
            logger = mockLogger,
            options = emptyMap()
        )
        
        // 处理器应该能够正常初始化
        assertTrue(processorWithDefaults != null)
    }

    @Test
    fun `test processor initialization with custom options`() {
        // 处理器应该能够正常初始化
        assertTrue(processor != null)
    }

    @Test
    fun `test process with empty resolver`() {
        every { mockResolver.getSymbolsWithAnnotation(any<String>()) } returns emptySequence()
        
        val result = processor.process(mockResolver)
        
        // 应该返回空列表，因为没有符号需要处理
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test process with service provider symbols`() {
        val mockServiceProvider = mockk<KSClassDeclaration>()
        val mockAnnotation = mockk<KSAnnotation>()
        val mockQualifiedName = mockk<KSName>()
        
        // 设置 mock 行为
        every { mockServiceProvider.qualifiedName } returns mockQualifiedName
        every { mockQualifiedName.asString() } returns "com.test.TestService"
        every { mockServiceProvider.packageName.asString() } returns "com.test"
        every { mockServiceProvider.simpleName.asString() } returns "TestService"
        every { mockServiceProvider.annotations } returns sequenceOf(mockAnnotation)
        every { mockServiceProvider.validate() } returns true
        every { mockServiceProvider.containingFile } returns null
        every { mockServiceProvider.accept(any<KSVisitor<Any, Any>>(), any<Any>()) } returns true

        
        // 设置注解参数
        every { mockAnnotation.shortName.asString() } returns "ServiceProvider"
        every { mockAnnotation.arguments } returns listOf(
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "interfaces"
                every { value } returns arrayOf("com.test.ITestService")
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "singleton"
                every { value } returns true
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "priority"
                every { value } returns 0
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "lazy"
                every { value } returns false
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "dependencies"
                every { value } returns emptyArray<String>()
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "module"
                every { value } returns ""
            }
        )
        
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceProvider") } returns 
            sequenceOf(mockServiceProvider)
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceModule") } returns 
            emptySequence()
        
        val result = processor.process(mockResolver)
        
        // 应该返回空列表，表示所有符号都已处理
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test process with service module symbols`() {
        val mockServiceModule = mockk<KSClassDeclaration>()
        val mockAnnotation = mockk<KSAnnotation>()
        val mockQualifiedName = mockk<KSName>()
        
        // 设置 mock 行为
        every { mockServiceModule.qualifiedName } returns mockQualifiedName
        every { mockQualifiedName.asString() } returns "com.test.TestModule"
        every { mockServiceModule.packageName.asString() } returns "com.test"
        every { mockServiceModule.simpleName.asString() } returns "TestModule"
        every { mockServiceModule.annotations } returns sequenceOf(mockAnnotation)
        every { mockServiceModule.validate() } returns true
        every { mockServiceModule.containingFile } returns null
        every { mockServiceModule.accept(any<KSVisitor<Any, Any>>(), any<Any>()) } returns true

        
        // 设置注解参数
        every { mockAnnotation.shortName.asString() } returns "ServiceModule"
        every { mockAnnotation.arguments } returns listOf(
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "name"
                every { value } returns "Test Module"
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "description"
                every { value } returns "A test module"
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "version"
                every { value } returns "1.0.0"
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "autoLoad"
                every { value } returns true
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "priority"
                every { value } returns 0
            },
            mockk<KSValueArgument>().apply {
                every { name?.asString() } returns "dependencies"
                every { value } returns emptyArray<String>()
            }
        )
        
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceProvider") } returns 
            emptySequence()
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceModule") } returns 
            sequenceOf(mockServiceModule)
        
        val result = processor.process(mockResolver)
        
        // 应该返回空列表，表示所有符号都已处理
        assertTrue(result.isEmpty())
    }

    @Test
    fun `test process with no annotations`() {
        // 创建一个没有 ServiceProvider 注解的符号
        val mockSymbolWithoutAnnotation = mockk<KSClassDeclaration>()
        every { mockSymbolWithoutAnnotation.qualifiedName } returns mockk<KSName> {
            every { asString() } returns "com.test.PlainService"
        }
        every { mockSymbolWithoutAnnotation.packageName } returns mockk<KSName> {
            every { asString() } returns "com.test"
        }
        every { mockSymbolWithoutAnnotation.simpleName } returns mockk<KSName> {
            every { asString() } returns "PlainService"
        }
        // 没有 ServiceProvider 注解
        every { mockSymbolWithoutAnnotation.annotations } returns emptySequence()
        every { mockSymbolWithoutAnnotation.validate() } returns true
        every { mockSymbolWithoutAnnotation.containingFile } returns null
        every { mockSymbolWithoutAnnotation.accept(any<KSVisitor<Any, Any>>(), any<Any>()) } returns true
        
        // 设置 resolver 返回包含该符号的序列
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceProvider") } returns sequenceOf(mockSymbolWithoutAnnotation)
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceModule") } returns emptySequence()
        
        val result = processor.process(mockResolver)
        
        // 由于符号没有 ServiceProvider 注解，processServiceProvider 会直接返回
        // 符号不会被添加到 unableToProcess，因为它通过了 validate() 检查
        assertEquals(0, result.size)
    }

    @Test
    fun `test process with resolver exception`() {
        // 模拟 resolver 抛出异常
        every { mockResolver.getSymbolsWithAnnotation(any<String>()) } throws RuntimeException("Test exception")
        
        val result = processor.process(mockResolver)
        
        // 应该返回空列表，因为异常被捕获了
        assertTrue(result.isEmpty())
        
        // 验证错误被记录到日志中
        verify { mockLogger.error(any<String>()) }
    }

    @Test
    fun `test ServiceProviderInfo data class`() {
        val serviceProvider = ServiceProviderInfo(
            className = "com.test.TestService",
            packageName = "com.test",
            simpleName = "TestService",
            interfaces = listOf("com.test.ITestService"),
            dependencies = listOf("com.test.IDependency"),
            singleton = true,
            priority = 10,
            lazy = false,
            module = "TestModule"
        )
        
        assertEquals("com.test.TestService", serviceProvider.className)
        assertEquals("com.test", serviceProvider.packageName)
        assertEquals("TestService", serviceProvider.simpleName)
        assertEquals(listOf("com.test.ITestService"), serviceProvider.interfaces)
        assertEquals(listOf("com.test.IDependency"), serviceProvider.dependencies)
        assertTrue(serviceProvider.singleton)
        assertEquals(10, serviceProvider.priority)
        assertFalse(serviceProvider.lazy)
        assertEquals("TestModule", serviceProvider.module)
    }

    @Test
    fun `test ServiceModuleInfo data class`() {
        val serviceModule = ServiceModuleInfo(
            className = "com.test.TestModule",
            packageName = "com.test",
            simpleName = "TestModule",
            name = "Test Module",
            description = "A test module",
            version = "1.0.0",
            dependencies = listOf("com.test.IDependency"),
            autoLoad = true,
            priority = 5
        )
        
        assertEquals("com.test.TestModule", serviceModule.className)
        assertEquals("com.test", serviceModule.packageName)
        assertEquals("TestModule", serviceModule.simpleName)
        assertEquals("Test Module", serviceModule.name)
        assertEquals("A test module", serviceModule.description)
        assertEquals("1.0.0", serviceModule.version)
        assertEquals(listOf("com.test.IDependency"), serviceModule.dependencies)
        assertTrue(serviceModule.autoLoad)
        assertEquals(5, serviceModule.priority)
    }

    @Test
    fun `test process with both providers and modules`() {
        val mockProvider = mockk<KSClassDeclaration>()
        val mockModule = mockk<KSClassDeclaration>()
        val mockProviderAnnotation = mockk<KSAnnotation>()
        val mockModuleAnnotation = mockk<KSAnnotation>()
        
        // 设置 provider mock
        every { mockProvider.qualifiedName?.asString() } returns "com.test.TestService"
        every { mockProvider.packageName.asString() } returns "com.test"
        every { mockProvider.simpleName.asString() } returns "TestService"
        every { mockProvider.annotations } returns sequenceOf(mockProviderAnnotation)
        every { mockProvider.validate() } returns true
        every { mockProvider.containingFile } returns null
        every { mockProvider.accept(any<KSVisitor<Any, Any>>(), any<Any>()) } returns true
        
        // 设置 module mock
        every { mockModule.qualifiedName?.asString() } returns "com.test.TestModule"
        every { mockModule.packageName.asString() } returns "com.test"
        every { mockModule.simpleName.asString() } returns "TestModule"
        every { mockModule.annotations } returns sequenceOf(mockModuleAnnotation)
        every { mockModule.validate() } returns true
        every { mockModule.containingFile } returns null
        every { mockModule.accept(any<KSVisitor<Any, Any>>(), any<Any>()) } returns true
        
        // 设置注解
        every { mockProviderAnnotation.shortName.asString() } returns "ServiceProvider"
        every { mockProviderAnnotation.arguments } returns emptyList()
        
        every { mockModuleAnnotation.shortName.asString() } returns "ServiceModule"
        every { mockModuleAnnotation.arguments } returns emptyList()
        
        // 设置 resolver
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceProvider") } returns 
            sequenceOf(mockProvider)
        every { mockResolver.getSymbolsWithAnnotation("com.flexiblesdk.processor.annotation.ServiceModule") } returns 
            sequenceOf(mockModule)
        
        val result = processor.process(mockResolver)
        
        // 应该返回空列表，表示所有符号都已处理
        assertTrue(result.isEmpty())
    }
}