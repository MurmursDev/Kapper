package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import dev.murmurs.kapper.config.MultipleTargetConfiguration
import dev.murmurs.kapper.transformer.PropertyConversion


class InstantiationCodeGeneratorImpl(private val propertyConversion: PropertyConversion) : InstantiationCodeGenerator {
    override fun generateCode(
        multipleTargetConfiguration: MultipleTargetConfiguration,
        sourceParameterName: String,
        convertedProperties: List<String>
    ): CodeBlock {
        val caseVariable = propertyConversion.convert(
            sourceParameterName,
            multipleTargetConfiguration.discriminator,
            multipleTargetConfiguration.discriminatorType.declaration as KSClassDeclaration,
            String::class
        )
        val codeBlockBuilder = CodeBlock.builder()
            .addStatement("return when (%L) {", caseVariable)

        multipleTargetConfiguration.targetTypes.forEach { (discriminatorValue, targetType) ->

            codeBlockBuilder.add(
                "%S-> %T(", discriminatorValue, ClassName.bestGuess(targetType.declaration.qualifiedName!!.asString())
            )
            generateInstantiationCode(
                targetType.declaration as KSClassDeclaration, convertedProperties, codeBlockBuilder
            )
            codeBlockBuilder.add(")\n")
        }

        return codeBlockBuilder
            .addStatement("else -> throw IllegalArgumentException(\"Unknown discriminator value\")")
            .addStatement("}")
            .build()
    }


    override fun generateCode(
        returnType: KSClassDeclaration, convertedProperties: List<String>
    ): CodeBlock {
        returnType.primaryConstructor
            ?: throw IllegalArgumentException("The target interface should have a primary constructor")

        val codeBlockBuilder = CodeBlock.builder()
        codeBlockBuilder.add("return %T(", ClassName.bestGuess(returnType.qualifiedName!!.asString()))
        generateInstantiationCode(returnType, convertedProperties, codeBlockBuilder)
        codeBlockBuilder.add(")")

        return codeBlockBuilder.build()
    }

    private fun generateInstantiationCode(
        returnType: KSClassDeclaration, convertedProperties: List<String>, codeBlockBuilder: CodeBlock.Builder
    ) {
        val initializedProperties = mutableListOf<String>()
        returnType.primaryConstructor?.parameters?.forEach {
            val parameterName = it.name!!.asString()
            if (convertedProperties.contains(parameterName)) {
                initializedProperties.add("$parameterName = $parameterName")
            } else {
                if (it.hasDefault) {
                    //skip initializing default value
                } else {
                    if (it.type.resolve().isMarkedNullable) {
                        initializedProperties.add("$parameterName = null")
                    } else {
                        // not nullable, no default value, no source property, throw exception
                        throw IllegalArgumentException("$parameterName has no default value, no source property, and is not nullable, cannot be instantiated")
                    }
                }
            }
        }
        codeBlockBuilder.add(initializedProperties.joinToString(", "))
    }
}
