package com.flexiblesdk.processor.annotation

/**
 * 标记一个 API 为内部使用
 * 
 * 被此注解标记的类、方法或字段仅供 SDK 内部使用，
 * 不应该被外部开发者直接调用。
 * 
 * @param reason 标记为内部 API 的原因
 */
@Target(
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
    AnnotationTarget.FIELD,
    AnnotationTarget.CONSTRUCTOR
)
@Retention(AnnotationRetention.SOURCE)
annotation class Internal(
    /**
     * 标记为内部 API 的原因
     */
    val reason: String = "This API is for internal use only and may change without notice"
)