package com.example

import com.flexiblesdk.core.manager.ServiceRegistry
import com.example.services.UserService

fun main() {
    println("FlexibleSDK Example Project")
    println("==========================")
    
    // 测试服务注册表
    println("所有注册的服务:")
    val allServices = ServiceRegistry.getAllServices()
    for (service in allServices) {
        println("- ${service.className}")
        println("  接口: ${service.interfaces.joinToString(", ")}")
        println("  模块: ${service.module}")
        println("  优先级: ${service.priority}")
        println("  单例: ${service.singleton}")
        println()
    }
    
    // 测试服务获取
    println("获取UserService实例:")
    val userService = ServiceRegistry.getService<UserService>(UserService::class)
    if (userService != null) {
        println("成功获取UserService实例: ${userService::class.simpleName}")
        val user = userService.getUserById("1")
        println("用户信息: $user")
    } else {
        println("无法获取UserService实例")
    }
    
    // 测试按接口查找服务
    println("\n按接口查找服务:")
    val userServices = ServiceRegistry.getServicesByInterface("com.example.services.UserService")
    println("找到 ${userServices.size} 个UserService实现:")
    for (service in userServices) {
        println("- ${service.className}")
    }
    
    // 测试按模块查找服务
    println("\n按模块查找服务:")
    val userModuleServices = ServiceRegistry.getServicesByModule("user")
    println("user模块中有 ${userModuleServices.size} 个服务:")
    for (service in userModuleServices) {
        println("- ${service.className}")
    }
}