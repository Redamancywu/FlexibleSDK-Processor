package com.flexiblesdk.processor.annotation

import kotlin.reflect.KClass

/**
 * 标记一个类作为服务提供者
 * 
 * 被此注解标记的类将被 KSP 处理器扫描，并自动注册到服务注册表中。
 * 
 * @param interfaces 该服务实现的接口列表
 * @param singleton 是否为单例模式，默认为 true
 * @param dependencies 该服务依赖的其他服务接口列表
 * @param priority 服务优先级，数值越小优先级越高，默认为 0
 * @param lazy 是否延迟初始化，默认为 false
 * @param module 所属模块名称，用于模块化管理
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceProvider(
    /**
     * 该服务实现的接口列表
     * 必须指定至少一个接口
     */
    val interfaces: Array<KClass<*>>,
    
    /**
     * 是否为单例模式
     * true: 全局唯一实例
     * false: 每次获取都创建新实例
     */
    val singleton: Boolean = true,
    
    /**
     * 该服务依赖的其他服务接口列表
     * 在创建该服务实例时，会先确保依赖的服务已经可用
     */
    val dependencies: Array<KClass<*>> = [],
    
    /**
     * 服务优先级
     * 当多个服务实现同一接口时，优先级高的服务会被优先选择
     * 数值越小优先级越高
     */
    val priority: Int = 0,
    
    /**
     * 是否延迟初始化
     * true: 在首次使用时才创建实例
     * false: 在 SDK 初始化时就创建实例
     */
    val lazy: Boolean = false,
    
    /**
     * 所属模块名称
     * 用于模块化管理和依赖分析
     */
    val module: String = ""
)