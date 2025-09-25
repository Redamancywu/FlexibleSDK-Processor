package com.flexiblesdk.processor.utils

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*

/**
 * ClassNameUtils 单元测试
 */
class ClassNameUtilsTest {
    
    @Test
    fun `test getPackageName with valid class name`() {
        assertEquals("com.example.test", ClassNameUtils.getPackageName("com.example.test.MyClass"))
        assertEquals("com.example", ClassNameUtils.getPackageName("com.example.MyClass"))
        assertEquals("", ClassNameUtils.getPackageName("MyClass"))
    }
    
    @Test
    fun `test getSimpleName with valid class name`() {
        assertEquals("MyClass", ClassNameUtils.getSimpleName("com.example.test.MyClass"))
        assertEquals("MyClass", ClassNameUtils.getSimpleName("com.example.MyClass"))
        assertEquals("MyClass", ClassNameUtils.getSimpleName("MyClass"))
    }
    
    @Test
    fun `test isValidClassName`() {
        assertTrue(ClassNameUtils.isValidClassName("com.example.MyClass"))
        assertTrue(ClassNameUtils.isValidClassName("MyClass"))
        assertTrue(ClassNameUtils.isValidClassName("com.example.test.MyClass123"))
        
        assertFalse(ClassNameUtils.isValidClassName(""))
        assertFalse(ClassNameUtils.isValidClassName("com.example.123Class"))
        assertFalse(ClassNameUtils.isValidClassName("com..example.MyClass"))
    }
    
    @Test
    fun `test isValidPackageName`() {
        assertTrue(ClassNameUtils.isValidPackageName("com.example.test"))
        assertTrue(ClassNameUtils.isValidPackageName("com.example"))
        assertTrue(ClassNameUtils.isValidPackageName(""))
        
        assertFalse(ClassNameUtils.isValidPackageName("com.example.class"))
        assertFalse(ClassNameUtils.isValidPackageName("com..example"))
        assertFalse(ClassNameUtils.isValidPackageName("123.example"))
    }
    
    @Test
    fun `test classNameToFileName`() {
        assertEquals("MyClass.kt", ClassNameUtils.classNameToFileName("com.example.MyClass"))
        assertEquals("MyClass.kt", ClassNameUtils.classNameToFileName("MyClass"))
    }
    
    @Test
    fun `test packageNameToPath`() {
        assertEquals("com/example/test", ClassNameUtils.packageNameToPath("com.example.test"))
        assertEquals("com/example", ClassNameUtils.packageNameToPath("com.example"))
        assertEquals("", ClassNameUtils.packageNameToPath(""))
    }
    
    @Test
    fun `test generateUniqueClassName`() {
        val existingNames = setOf("MyClass", "MyClass_1", "MyClass_2")
        
        assertEquals("NewClass", ClassNameUtils.generateUniqueClassName("NewClass", existingNames))
        assertEquals("MyClass_3", ClassNameUtils.generateUniqueClassName("MyClass", existingNames))
    }
    
    @Test
    fun `test normalizeClassName`() {
        assertEquals("com.example.MyClass", ClassNameUtils.normalizeClassName("com.example.MyClass"))
        assertEquals("com.example._123Class", ClassNameUtils.normalizeClassName("com.example.123Class"))
        assertEquals("com.example.My_Class", ClassNameUtils.normalizeClassName("com.example.My-Class"))
    }
    
    @Test
    fun `test inner class methods`() {
        assertTrue(ClassNameUtils.isInnerClass("com.example.OuterClass\$InnerClass"))
        assertFalse(ClassNameUtils.isInnerClass("com.example.MyClass"))
        
        assertEquals("com.example.OuterClass", ClassNameUtils.getOuterClassName("com.example.OuterClass\$InnerClass"))
        assertNull(ClassNameUtils.getOuterClassName("com.example.MyClass"))
        
        assertEquals("OuterClass.InnerClass", ClassNameUtils.getInnerClassName("OuterClass", "InnerClass"))
    }
}