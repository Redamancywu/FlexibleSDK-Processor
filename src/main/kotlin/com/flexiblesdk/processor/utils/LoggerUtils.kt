package com.flexiblesdk.processor.utils

import com.google.devtools.ksp.processing.KSPLogger
import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging

/**
 * 日志工具类
 * 提供统一的日志输出功能，支持不同的日志级别和格式化输出
 */
object LoggerUtils {
    
    private const val TAG = "[FlexibleSDK]"
    
    /**
     * 日志级别枚举
     */
    enum class LogLevel(val value: Int) {
        DEBUG(0),
        INFO(1),
        WARN(2),
        ERROR(3)
    }
    
    /**
     * 当前日志级别，默认为 INFO
     */
    var currentLogLevel: LogLevel = LogLevel.INFO
    
    /**
     * 是否启用调试日志
     */
    var isDebugEnabled: Boolean = false
        set(value) {
            field = value
            if (value) {
                currentLogLevel = LogLevel.DEBUG
            }
        }
    
    /**
     * Gradle Logger 实例
     */
    private val gradleLogger: Logger = Logging.getLogger(LoggerUtils::class.java)
    
    /**
     * KSP Logger 实例（可选）
     */
    private var kspLogger: KSPLogger? = null
    
    /**
     * 设置 KSP Logger
     */
    fun setKSPLogger(logger: KSPLogger) {
        kspLogger = logger
    }
    
    /**
     * 格式化日志消息
     */
    private fun formatMessage(level: String, message: String, tag: String? = null): String {
        val timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
        )
        val tagStr = if (tag != null) "[$tag]" else ""
        return "$TAG $tagStr [$level] [$timestamp] $message"
    }
    
    /**
     * 检查是否应该输出指定级别的日志
     */
    private fun shouldLog(level: LogLevel): Boolean {
        return level.value >= currentLogLevel.value
    }
    
    /**
     * 输出调试日志
     */
    fun debug(message: String, tag: String? = null) {
        if (!shouldLog(LogLevel.DEBUG)) return
        
        val formattedMessage = formatMessage("DEBUG", message, tag)
        kspLogger?.info(formattedMessage) ?: gradleLogger.debug(formattedMessage)
    }
    
    /**
     * 输出信息日志
     */
    fun info(message: String, tag: String? = null) {
        if (!shouldLog(LogLevel.INFO)) return
        
        val formattedMessage = formatMessage("INFO", message, tag)
        kspLogger?.info(formattedMessage) ?: gradleLogger.info(formattedMessage)
    }
    
    /**
     * 输出警告日志
     */
    fun warn(message: String, tag: String? = null, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.WARN)) return
        
        val formattedMessage = formatMessage("WARN", message, tag)
        if (throwable != null) {
            kspLogger?.warn("$formattedMessage\n${throwable.stackTraceToString()}") 
                ?: gradleLogger.warn(formattedMessage, throwable)
        } else {
            kspLogger?.warn(formattedMessage) ?: gradleLogger.warn(formattedMessage)
        }
    }
    
    /**
     * 输出错误日志
     */
    fun error(message: String, tag: String? = null, throwable: Throwable? = null) {
        if (!shouldLog(LogLevel.ERROR)) return
        
        val formattedMessage = formatMessage("ERROR", message, tag)
        if (throwable != null) {
            kspLogger?.error("$formattedMessage\n${throwable.stackTraceToString()}") 
                ?: gradleLogger.error(formattedMessage, throwable)
        } else {
            kspLogger?.error(formattedMessage) ?: gradleLogger.error(formattedMessage)
        }
    }
    
    /**
     * 输出处理步骤日志
     */
    fun step(stepNumber: Int, totalSteps: Int, description: String, tag: String? = null) {
        info("[$stepNumber/$totalSteps] $description", tag)
    }
    
    /**
     * 输出处理开始日志
     */
    fun startProcess(processName: String, tag: String? = null) {
        info("========== 开始 $processName ==========", tag)
    }
    
    /**
     * 输出处理结束日志
     */
    fun endProcess(processName: String, duration: Long? = null, tag: String? = null) {
        val durationStr = if (duration != null) " (耗时: ${duration}ms)" else ""
        info("========== 完成 $processName$durationStr ==========", tag)
    }
    
    /**
     * 输出统计信息
     */
    fun statistics(stats: Map<String, Any>, tag: String? = null) {
        info("========== 统计信息 ==========", tag)
        stats.forEach { (key, value) ->
            info("  $key: $value", tag)
        }
        info("==============================", tag)
    }
    
    /**
     * 输出分隔线
     */
    fun separator(tag: String? = null) {
        info("----------------------------------------", tag)
    }
    
    /**
     * 输出配置信息
     */
    fun config(configName: String, configValue: Any?, tag: String? = null) {
        debug("配置 $configName = $configValue", tag)
    }
    
    /**
     * 输出文件操作日志
     */
    fun fileOperation(operation: String, filePath: String, tag: String? = null) {
        debug("文件操作: $operation -> $filePath", tag)
    }
    
    /**
     * 输出处理进度
     */
    fun progress(current: Int, total: Int, item: String, tag: String? = null) {
        val percentage = if (total > 0) (current * 100 / total) else 0
        info("处理进度: [$current/$total] ($percentage%) - $item", tag)
    }
    
    /**
     * 输出性能统计
     */
    fun performance(operation: String, duration: Long, tag: String? = null) {
        debug("性能统计: $operation 耗时 ${duration}ms", tag)
    }
    
    /**
     * 输出验证结果
     */
    fun validation(item: String, isValid: Boolean, message: String? = null, tag: String? = null) {
        val status = if (isValid) "✓" else "✗"
        val msg = if (message != null) " - $message" else ""
        info("验证结果: $status $item$msg", tag)
    }
    
    /**
     * 输出生成结果
     */
    fun generation(item: String, success: Boolean, details: String? = null, tag: String? = null) {
        val status = if (success) "✓" else "✗"
        val msg = if (details != null) " - $details" else ""
        info("生成结果: $status $item$msg", tag)
    }
    
    // 兼容性方法 - 为了与现有代码保持兼容
    fun logDebug(message: String, tag: String? = null) = debug(message, tag)
    fun logInfo(message: String, tag: String? = null) = info(message, tag)
    fun logWarn(message: String, tag: String? = null, throwable: Throwable? = null) = warn(message, tag, throwable)
    fun logError(message: String, tag: String? = null, throwable: Throwable? = null) = error(message, tag, throwable)
    
    fun logProcessStart(processName: String, description: String, tag: String? = null) {
        startProcess("$processName: $description", tag)
    }
    
    fun logProcessEnd(processName: String, description: String, tag: String? = null) {
        endProcess("$processName: $description", null, tag)
    }
    
    fun logStatistics(stats: Map<String, Any>, tag: String? = null) = statistics(stats, tag)
    
    fun logPerformance(operation: String, duration: Long, tag: String? = null) = performance(operation, duration, tag)
    
    fun logValidationResult(item: String, isValid: Boolean, message: String? = null, tag: String? = null) {
        validation(item, isValid, message, tag)
    }
    
    fun logGenerationResult(item: String, filePath: String, details: Map<String, Any>? = null, tag: String? = null) {
        val detailsStr = details?.entries?.joinToString(", ") { "${it.key}: ${it.value}" }
        generation("$item -> $filePath", true, detailsStr, tag)
    }
    
    fun logFileOperation(operation: String, filePath: String, tag: String? = null) = fileOperation(operation, filePath, tag)
}