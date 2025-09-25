package com.flexiblesdk.processor.annotation

/**
 * 标记一个服务为单例模式
 * 
 * 被此注解标记的服务在整个应用生命周期中只会创建一个实例。
 * 这是一个简化的注解，等同于在 @ServiceProvider 中设置 singleton = true。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class Singleton