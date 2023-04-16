package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import dev.murmurs.kapper.config.MappingConfiguration

interface PropertyCodeGenerator {
    fun generateCode(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSType,
        targetPropertyName: String,
        targetPropertyType: KSType,
        mappingConfiguration: MappingConfiguration?
    ): CodeBlock

    fun generateCode(
        targetPropertyType: KSType,
        mappingConfiguration: MappingConfiguration
    ): CodeBlock
}
