package com.flexiblesdk.processor.utils

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

/**
 * 文件处理工具类
 */
object FileUtils {
    
    /**
     * 确保目录存在，如果不存在则创建
     */
    fun ensureDirectoryExists(directoryPath: String): Boolean {
        return try {
            val path = Paths.get(directoryPath)
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 确保目录存在（Path 版本）
     */
    fun ensureDirectoryExists(path: Path): Boolean {
        return try {
            if (!Files.exists(path)) {
                Files.createDirectories(path)
            }
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 安全地写入文件内容
     */
    fun writeFile(filePath: String, content: String): Boolean {
        return try {
            val path = Paths.get(filePath)
            ensureDirectoryExists(path.parent)
            Files.write(path, content.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 安全地写入文件内容（Path 版本）
     */
    fun writeFile(path: Path, content: String): Boolean {
        return try {
            ensureDirectoryExists(path.parent)
            Files.write(path, content.toByteArray(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 读取文件内容
     */
    fun readFile(filePath: String): String? {
        return try {
            val path = Paths.get(filePath)
            if (Files.exists(path)) {
                Files.readString(path)
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * 读取文件内容（Path 版本）
     */
    fun readFile(path: Path): String? {
        return try {
            if (Files.exists(path)) {
                Files.readString(path)
            } else {
                null
            }
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * 检查文件是否存在
     */
    fun fileExists(filePath: String): Boolean {
        return Files.exists(Paths.get(filePath))
    }
    
    /**
     * 检查文件是否存在（Path 版本）
     */
    fun fileExists(path: Path): Boolean {
        return Files.exists(path)
    }
    
    /**
     * 删除文件
     */
    fun deleteFile(filePath: String): Boolean {
        return try {
            Files.deleteIfExists(Paths.get(filePath))
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 删除文件（Path 版本）
     */
    fun deleteFile(path: Path): Boolean {
        return try {
            Files.deleteIfExists(path)
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 获取文件扩展名
     */
    fun getFileExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0 && lastDotIndex < fileName.length - 1) {
            fileName.substring(lastDotIndex + 1)
        } else {
            ""
        }
    }
    
    /**
     * 获取不带扩展名的文件名
     */
    fun getFileNameWithoutExtension(fileName: String): String {
        val lastDotIndex = fileName.lastIndexOf('.')
        return if (lastDotIndex > 0) {
            fileName.substring(0, lastDotIndex)
        } else {
            fileName
        }
    }
    
    /**
     * 规范化文件路径
     */
    fun normalizePath(path: String): String {
        return Paths.get(path).normalize().toString()
    }
    
    /**
     * 连接路径
     */
    fun joinPaths(vararg paths: String): String {
        return Paths.get(paths.first(), *paths.drop(1).toTypedArray()).toString()
    }
    
    /**
     * 获取相对路径
     */
    fun getRelativePath(basePath: String, targetPath: String): String {
        val base = Paths.get(basePath).normalize()
        val target = Paths.get(targetPath).normalize()
        return base.relativize(target).toString()
    }
    
    /**
     * 复制文件
     */
    fun copyFile(sourcePath: String, targetPath: String): Boolean {
        return try {
            val source = Paths.get(sourcePath)
            val target = Paths.get(targetPath)
            ensureDirectoryExists(target.parent)
            Files.copy(source, target)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 移动文件
     */
    fun moveFile(sourcePath: String, targetPath: String): Boolean {
        return try {
            val source = Paths.get(sourcePath)
            val target = Paths.get(targetPath)
            ensureDirectoryExists(target.parent)
            Files.move(source, target)
            true
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 获取文件大小
     */
    fun getFileSize(filePath: String): Long {
        return try {
            Files.size(Paths.get(filePath))
        } catch (e: IOException) {
            -1L
        }
    }
    
    /**
     * 列出目录中的文件
     */
    fun listFiles(directoryPath: String, extension: String? = null): List<String> {
        return try {
            val path = Paths.get(directoryPath)
            if (Files.isDirectory(path)) {
                Files.list(path).use { stream ->
                    stream
                        .filter { Files.isRegularFile(it) }
                        .filter { extension == null || getFileExtension(it.fileName.toString()) == extension }
                        .map { it.toString() }
                        .collect(java.util.stream.Collectors.toList())
                }
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        }
    }
    
    /**
     * 递归列出目录中的所有文件
     */
    fun listFilesRecursively(directoryPath: String, extension: String? = null): List<String> {
        return try {
            val path = Paths.get(directoryPath)
            if (Files.isDirectory(path)) {
                Files.walk(path).use { stream ->
                    stream
                        .filter { Files.isRegularFile(it) }
                        .filter { extension == null || getFileExtension(it.fileName.toString()) == extension }
                        .map { it.toString() }
                        .collect(java.util.stream.Collectors.toList())
                }
            } else {
                emptyList()
            }
        } catch (e: IOException) {
            emptyList()
        }
    }
    
    /**
     * 清空目录
     */
    fun clearDirectory(directoryPath: String): Boolean {
        return try {
            val path = Paths.get(directoryPath)
            if (Files.isDirectory(path)) {
                Files.walk(path)
                    .sorted(Comparator.reverseOrder())
                    .filter { it != path }
                    .forEach { Files.deleteIfExists(it) }
                true
            } else {
                false
            }
        } catch (e: IOException) {
            false
        }
    }
    
    /**
     * 创建临时文件
     */
    fun createTempFile(prefix: String, suffix: String): String? {
        return try {
            Files.createTempFile(prefix, suffix).toString()
        } catch (e: IOException) {
            null
        }
    }
    
    /**
     * 创建临时目录
     */
    fun createTempDirectory(prefix: String): String? {
        return try {
            Files.createTempDirectory(prefix).toString()
        } catch (e: IOException) {
            null
        }
    }
}