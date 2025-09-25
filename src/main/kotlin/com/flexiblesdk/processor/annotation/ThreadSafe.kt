package com.flexiblesdk.processor.annotation

/**
 * 标记一个服务为线程安全
 * 
 * 被此注解标记的服务可以在多线程环境中安全使用。
 * 这是一个文档性注解，主要用于代码文档和静态分析。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ThreadSafe