package dev.murmur.kapper

import dev.murmur.kapper.codegen.FunctionCodeGeneratorImpl
import dev.murmur.kapper.codegen.InstantiationCodeGeneratorImpl
import dev.murmur.kapper.codegen.PropertyCodeGeneratorImpl
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dev.murmur.kapper.transformer.PropertyConversionImpl

class MapperProcessorProvider : SymbolProcessorProvider {
    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {

        //instantiate the code generators
        val propertyConversion = PropertyConversionImpl(environment.logger)
        val instantiationCodeGenerator = InstantiationCodeGeneratorImpl(propertyConversion)
        val propertyCodeGenerator = PropertyCodeGeneratorImpl(environment.logger, propertyConversion)
        val functionCodeGenerator =
            FunctionCodeGeneratorImpl(environment.logger, propertyCodeGenerator, instantiationCodeGenerator)
        val mapperVisitor =
            MapperVisitor(environment.logger, environment.codeGenerator, functionCodeGenerator, propertyConversion)


        return MapperProcessor(environment.logger, mapperVisitor)
    }
}
