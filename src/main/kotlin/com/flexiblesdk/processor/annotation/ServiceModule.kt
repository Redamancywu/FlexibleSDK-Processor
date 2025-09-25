package com.flexiblesdk.processor.annotation

/**
 * 标记一个类作为服务模块
 * 
 * 服务模块用于组织和管理相关的服务，提供模块级别的配置和初始化逻辑。
 * 
 * @param name 模块名称，必须唯一
 * @param description 模块描述
 * @param version 模块版本
 * @param dependencies 依赖的其他模块名称列表
 * @param autoLoad 是否自动加载，默认为 true
 * @param priority 模块加载优先级，数值越小优先级越高
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
annotation class ServiceModule(
    /**
     * 模块名称
     * 必须唯一，用于模块识别和依赖管理
     */
    val name: String,
    
    /**
     * 模块描述
     * 用于文档生成和调试信息
     */
    val description: String = "",
    
    /**
     * 模块版本
     * 用于版本管理和兼容性检查
     */
    val version: String = "1.0.0",
    
    /**
     * 依赖的其他模块名称列表
     * 在加载该模块前，会先确保依赖的模块已经加载
     */
    val dependencies: Array<String> = [],
    
    /**
     * 是否自动加载
     * true: SDK 初始化时自动加载
     * false: 需要手动加载
     */
    val autoLoad: Boolean = true,
    
    /**
     * 模块加载优先级
     * 数值越小优先级越高，优先级高的模块会先加载
     */
    val priority: Int = 0
)