package com.flexiblesdk.processor.utils

/**
 * 类名处理工具类
 */
object ClassNameUtils {
    
    /**
     * 从完整类名中提取包名
     */
    fun getPackageName(fullClassName: String): String {
        val lastDotIndex = fullClassName.lastIndexOf('.')
        return if (lastDotIndex > 0) {
            fullClassName.substring(0, lastDotIndex)
        } else {
            ""
        }
    }
    
    /**
     * 从完整类名中提取简单类名
     */
    fun getSimpleName(fullClassName: String): String {
        val lastDotIndex = fullClassName.lastIndexOf('.')
        return if (lastDotIndex >= 0) {
            fullClassName.substring(lastDotIndex + 1)
        } else {
            fullClassName
        }
    }
    
    /**
     * 验证类名是否有效
     */
    fun isValidClassName(className: String): Boolean {
        if (className.isBlank()) return false
        
        val parts = className.split('.')
        return parts.all { part ->
            part.isNotBlank() && 
            part.first().isJavaIdentifierStart() && 
            part.all { it.isJavaIdentifierPart() }
        }
    }
    
    /**
     * 验证包名是否有效
     */
    fun isValidPackageName(packageName: String): Boolean {
        if (packageName.isBlank()) return true // 空包名是有效的
        
        val parts = packageName.split('.')
        return parts.all { part ->
            part.isNotBlank() && 
            part.first().isJavaIdentifierStart() && 
            part.all { it.isJavaIdentifierPart() } &&
            !isJavaKeyword(part)
        }
    }
    
    /**
     * 将类名转换为文件名
     */
    fun classNameToFileName(className: String): String {
        return "${getSimpleName(className)}.kt"
    }
    
    /**
     * 将包名转换为目录路径
     */
    fun packageNameToPath(packageName: String): String {
        return packageName.replace('.', '/')
    }
    
    /**
     * 生成唯一的类名（避免冲突）
     */
    fun generateUniqueClassName(baseName: String, existingNames: Set<String>): String {
        if (baseName !in existingNames) {
            return baseName
        }
        
        var counter = 1
        var uniqueName: String
        do {
            uniqueName = "${baseName}_$counter"
            counter++
        } while (uniqueName in existingNames)
        
        return uniqueName
    }
    
    /**
     * 标准化类名（确保符合 Java 命名规范）
     */
    fun normalizeClassName(className: String): String {
        return className
            .split('.')
            .joinToString(".") { part ->
                part.replace(Regex("[^a-zA-Z0-9_]"), "_")
                    .let { if (it.first().isDigit()) "_$it" else it }
            }
    }
    
    /**
     * 检查是否为 Java 关键字
     */
    private fun isJavaKeyword(word: String): Boolean {
        return word in setOf(
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char",
            "class", "const", "continue", "default", "do", "double", "else", "enum",
            "extends", "final", "finally", "float", "for", "goto", "if", "implements",
            "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static",
            "strictfp", "super", "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "true", "false", "null"
        )
    }
    
    /**
     * 获取类的内部类名
     */
    fun getInnerClassName(outerClass: String, innerClass: String): String {
        return "$outerClass.$innerClass"
    }
    
    /**
     * 检查是否为内部类
     */
    fun isInnerClass(className: String): Boolean {
        return className.contains('$')
    }
    
    /**
     * 获取外部类名（如果是内部类）
     */
    fun getOuterClassName(innerClassName: String): String? {
        val dollarIndex = innerClassName.indexOf('$')
        return if (dollarIndex > 0) {
            innerClassName.substring(0, dollarIndex)
        } else {
            null
        }
    }
}