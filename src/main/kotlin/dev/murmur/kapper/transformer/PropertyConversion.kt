package dev.murmur.kapper.transformer

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import kotlin.reflect.KClass

interface PropertyConversion {

    fun convert(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSClassDeclaration,
        targetPropertyType: KSClassDeclaration
    ): CodeBlock

    fun convert(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSClassDeclaration,
        targetPropertyType: KClass<*>
    ): CodeBlock

    fun registerImplementedMapper(mapper: KSFunctionDeclaration)
}
