package dev.murmurs.kapper

import dev.murmurs.kapper.codegen.FunctionCodeGeneratorImpl
import dev.murmurs.kapper.codegen.InstantiationCodeGeneratorImpl
import dev.murmurs.kapper.codegen.PropertyCodeGeneratorImpl
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dev.murmurs.kapper.transformer.PropertyConversionImpl

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


        return dev.murmurs.kapper.MapperProcessor(environment.logger, mapperVisitor)
    }
}
