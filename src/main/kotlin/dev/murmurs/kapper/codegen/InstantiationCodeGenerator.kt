package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.CodeBlock
import dev.murmurs.kapper.config.MultipleTargetConfiguration

interface InstantiationCodeGenerator {
    fun generateCode(
        multipleTargetConfiguration: MultipleTargetConfiguration,
        sourceParameterName: String,
        convertedProperties: List<String>
    ): CodeBlock

    fun generateCode(returnType: KSClassDeclaration, convertedProperties: List<String>): CodeBlock
}
