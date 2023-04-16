package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.Nullability
import com.squareup.kotlinpoet.CodeBlock
import dev.murmurs.kapper.config.MappingConfiguration
import dev.murmurs.kapper.transformer.PropertyConversion

class PropertyCodeGeneratorImpl(private val logger: KSPLogger, private val propertyConversion: PropertyConversion) :
    PropertyCodeGenerator {
    override fun generateCode(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSType,
        targetPropertyName: String,
        targetPropertyType: KSType,
        mappingConfiguration: MappingConfiguration?
    ): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.add("val %L = ", targetPropertyName)
        val conversionCode = propertyConversion.convert(
            sourceParameterName,
            sourcePropertyName,
            sourcePropertyType.declaration as KSClassDeclaration,
            targetPropertyType.declaration as KSClassDeclaration
        )

        if (sourcePropertyType.nullability == Nullability.NULLABLE) {
            codeBlockBuilder.beginControlFlow("if (%L.%L == null)", sourceParameterName, sourcePropertyName)
            if (mappingConfiguration != null && mappingConfiguration.defaultValue != "") {
                val defaultConversionCode = propertyConversion.convert(
                    "\"${mappingConfiguration.defaultValue}\"",
                    sourcePropertyName,
                    String::class,
                    targetPropertyType.declaration as KSClassDeclaration
                )
                codeBlockBuilder.add(defaultConversionCode)
            } else if (targetPropertyType.nullability == Nullability.NULLABLE) {
                codeBlockBuilder.add("null")
            } else {
                codeBlockBuilder.addStatement(
                    "throw IllegalArgumentException(\"%L.%L is null\")",
                    sourceParameterName,
                    sourcePropertyName
                )
            }
            codeBlockBuilder.nextControlFlow("else")
                .add(conversionCode)
                .add("\n")
                .endControlFlow()
        } else {
            codeBlockBuilder.add(conversionCode).add("\n")
        }

        return codeBlockBuilder.build()
    }

    override fun generateCode(targetPropertyType: KSType, mappingConfiguration: MappingConfiguration): CodeBlock {
        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.add("val %L = ", mappingConfiguration.target)
        val defaultConversionCode = propertyConversion.convert(
            "\"${mappingConfiguration.defaultValue}\"",
            String::class.qualifiedName!!,
            targetPropertyType.declaration.qualifiedName!!.asString()
        )
        return codeBlockBuilder.add(defaultConversionCode).add("\n").build()
    }
}
