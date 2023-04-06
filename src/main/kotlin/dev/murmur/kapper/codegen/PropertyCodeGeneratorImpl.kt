package dev.murmur.kapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import dev.murmur.kapper.transformer.PropertyConversion

class PropertyCodeGeneratorImpl(private val logger: KSPLogger, private val propertyConversion: PropertyConversion) :
    PropertyCodeGenerator {
    override fun generateCode(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSType,
        targetPropertyName: String,
        targetPropertyType: KSType
    ): CodeBlock {

        return if (sourcePropertyType.nullability == Nullability.NULLABLE && targetPropertyType.nullability == Nullability.NOT_NULL) {
            throw IllegalArgumentException("The source type is nullable but the return type is not nullable")
        } else {
            val codeBlockBuilder = CodeBlock.builder()
            codeBlockBuilder.add("val %L = ", targetPropertyName)
            val needHandlerNullable =
                sourcePropertyType.nullability == Nullability.NULLABLE && targetPropertyType.nullability == Nullability.NULLABLE
            val conversionCode = propertyConversion.convert(
                sourceParameterName,
                sourcePropertyName,
                sourcePropertyType.declaration as KSClassDeclaration,
                targetPropertyType.declaration as KSClassDeclaration
            )
            if (needHandlerNullable) {
                codeBlockBuilder.beginControlFlow("if (%L.%L == null) ", sourceParameterName, sourcePropertyName)
                    .addStatement("null")
                    .nextControlFlow("else")
                    .add(conversionCode)
                    .add("\n")
                    .endControlFlow()
            } else {
                codeBlockBuilder.add(conversionCode).add("\n")
            }

            codeBlockBuilder.build()
        }
    }

}
