package dev.murmurs.kapper.transformer

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

    fun convert(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KClass<*>,
        targetPropertyType: KSClassDeclaration
    ): CodeBlock


    fun registerImplementedMapper(mapper: KSFunctionDeclaration)
    fun convert(sourceName: String, sourcePropertyType: String, targetPropertyType: String): CodeBlock
}
