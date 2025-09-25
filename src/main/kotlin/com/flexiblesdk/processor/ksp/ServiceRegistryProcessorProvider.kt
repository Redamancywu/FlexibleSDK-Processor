package com.flexiblesdk.processor.ksp

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

/**
 * FlexibleSDK 服务注册表处理器提供者
 * 
 * KSP 框架通过此类创建 ServiceRegistryProcessor 实例
 */
class ServiceRegistryProcessorProvider : SymbolProcessorProvider {
    
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        return ServiceRegistryProcessor(
            codeGenerator = environment.codeGenerator,
            logger = environment.logger,
            options = environment.options
        )
    }
}