package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.*
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import dev.murmurs.kapper.config.*

class FunctionCodeGeneratorImpl(
    private val logger: KSPLogger,
    private val propertyCodeGenerator: PropertyCodeGenerator,
    private val instantiationCodeGenerator: InstantiationCodeGenerator
) : FunctionCodeGenerator {

    override fun generateCode(mappingFunction: KSFunctionDeclaration): FunSpec {
        // check function is not abstract

        val returnType = mappingFunction.returnType?.resolve()
            ?: throw IllegalArgumentException("Return type of the mapping function should be an interface")
        val returnDeclaration = returnType.declaration
        if (returnDeclaration !is KSClassDeclaration) {
            logger.error("Return type of the mapping function should be an interface", mappingFunction)
            throw IllegalArgumentException("Return class of the mapping function should be an interface")
        }

        mappingFunction.parameters.size == 1 || throw IllegalArgumentException("The mapping function must have a source parameter")

        val sourceParameter = mappingFunction.parameters[0]
        val sourceParameterName = sourceParameter.name?.asString()
            ?: throw IllegalArgumentException("The mapping function must have a source parameter")

        val sourceType = sourceParameter.type.resolve()
        val sourceDeclaration = sourceType.declaration
        if (sourceDeclaration !is KSClassDeclaration || sourceDeclaration.classKind != ClassKind.CLASS) {
            logger.error("Source parameter of the mapping function should be a class", mappingFunction)
            throw IllegalArgumentException("Source parameter of the mapping function should be a class")
        }

        val sourceProperties = sourceDeclaration.getAllProperties()
        logger.info("return type is ${returnDeclaration.qualifiedName?.asString()}")
        logger.info("the primary constructor of return type is ${returnDeclaration.primaryConstructor}")

        var multipleTargetConfiguration: MultipleTargetConfiguration? = null

        val mappings = hashMapOf<String, MappingConfiguration>()
        mappingFunction.annotations.forEach {
            val annotationName = it.annotationType.resolve().declaration.qualifiedName?.asString()
            if (annotationName == Mapping::class.qualifiedName) {
                val mappingConfiguration = extractMappingAnnotation(it)
                mappings[mappingConfiguration.target] = mappingConfiguration
            } else if (annotationName == MultipleTarget::class.qualifiedName && multipleTargetConfiguration == null) {
                multipleTargetConfiguration = convertMappingAnnotation(it, sourceProperties)
            }
        }
        MappingsConfiguration(mappings.toMap())

        val targetProperties: List<Pair<String, KSType>> = if (multipleTargetConfiguration == null) {
            val returnTypePrimaryConstructor = returnDeclaration.primaryConstructor
                ?: throw IllegalArgumentException("The target interface should have a primary constructor")
            returnTypePrimaryConstructor.parameters.map { it.name!!.asString() to it.type.resolve() }
        } else {
            returnDeclaration.getAllProperties().map {
                it.simpleName.asString() to it.type.resolve()
            }.toList()
        }

        val methodName = mappingFunction.simpleName.asString()
        val funSpecBuilder = FunSpec.builder(methodName)
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(sourceParameterName, ClassName.bestGuess(sourceDeclaration.qualifiedName!!.asString()))
            .returns(ClassName.bestGuess(returnDeclaration.qualifiedName!!.asString()))

        //create method to handle nullable input case
        handleNullableInput(sourceType, returnType)


        val convertedProperties = mutableListOf<String>()
        /*
         *  convert all targetProperties to code block
         */
        targetProperties.forEach { targetProperty ->
            val mappingConfiguration = mappings[targetProperty.first]
            if (mappingConfiguration?.ignore == true) {
                return@forEach
            }

            val sourceProperty =
                if (mappingConfiguration == null) {
                    sourceProperties.filter { it.simpleName.getShortName() == targetProperty.first }
                        .firstOrNull()
                } else {
                    sourceProperties.filter { it.simpleName.getShortName() == mappingConfiguration.source }
                        .firstOrNull()
                }

            if (sourceProperty != null) {
                logger.info("source property: ${sourceProperty.qualifiedName?.asString()}")
                val propertyConversionCode = propertyCodeGenerator.generateCode(
                    sourceParameterName,
                    sourceProperty.simpleName.asString(),
                    sourceProperty.type.resolve(),
                    targetProperty.first,
                    targetProperty.second,
                    mappingConfiguration
                )
                convertedProperties.add(targetProperty.first)
                funSpecBuilder.addCode(propertyConversionCode)
            } else if (mappingConfiguration?.defaultValue != null) {
                val propertyConversionCode = propertyCodeGenerator.generateCode(
                    targetProperty.second,
                    mappingConfiguration
                )
                convertedProperties.add(mappingConfiguration.target)
                funSpecBuilder.addCode(propertyConversionCode)
            }
        }

        val instantiation = if (multipleTargetConfiguration != null) {
            instantiationCodeGenerator.generateCode(
                multipleTargetConfiguration!!,
                sourceParameterName,
                convertedProperties
            )
        } else {
            instantiationCodeGenerator.generateCode(returnDeclaration, convertedProperties)
        }
        funSpecBuilder.addCode(instantiation)

        return funSpecBuilder.build()
    }

    private fun handleNullableInput(sourceType: KSType, returnType: KSType): CodeBlock {
        return if (sourceType.nullability == Nullability.NULLABLE && returnType.nullability == Nullability.NOT_NULL) {
            throw IllegalArgumentException("The source type is nullable but the return type is not nullable")
        } else if (sourceType.nullability == Nullability.NULLABLE && returnType.nullability == Nullability.NULLABLE) {
            val sourceParameterName = sourceType.declaration.simpleName.asString()
            CodeBlock.builder().addStatement("if (%L == null) return null", sourceParameterName).build()
        } else {
            CodeBlock.builder().build()
        }
    }


    /**
     * convert config.Mapping annotation to config.MappingConfiguration
     */
    private fun convertMappingAnnotation(
        mappingAnnotation: KSAnnotation,
        sourceProperties: Sequence<KSPropertyDeclaration>
    ): MultipleTargetConfiguration {
        val discriminator =
            mappingAnnotation
                .arguments
                .find {
                    it.name?.asString() == MultipleTarget::discriminator.name
                }?.value as? String
                ?: throw IllegalArgumentException("config.Mapping annotation should have a discriminator property")
        val discriminatorType = sourceProperties.find { it.simpleName.asString() == discriminator }
            ?.type?.resolve()
            ?: throw IllegalArgumentException("discriminator property should be a property of the source class")

        val targetTypeList =
            mappingAnnotation
                .arguments
                .find { it.name?.asString() == MultipleTarget::targetTypes.name }?.value as? List<*>
                ?: throw IllegalArgumentException("config.Mapping annotation should have a targetTypes property")
        val targetTypes = targetTypeList
            .mapNotNull { it as? KSAnnotation }
            .mapNotNull { targetTypeAnnotation ->
                val targetType =
                    targetTypeAnnotation.arguments.find { it.name?.asString() == TargetType::targetType.name }?.value
                val discriminatorValue =
                    targetTypeAnnotation.arguments.find { it.name?.asString() == TargetType::discriminatorValue.name }?.value
                if (targetType != null && discriminatorValue != null) {
                    TargetTypeConfiguration(
                        discriminatorValue as String,
                        targetType as KSType
                    )
                } else {
                    null
                }
            }

        return MultipleTargetConfiguration(discriminator, discriminatorType, targetTypes)
    }

    private fun extractMappingAnnotation(it: KSAnnotation): MappingConfiguration {
        val target = it.arguments.find { it.name?.asString() == Mapping::target.name }
            ?: throw IllegalArgumentException("The mapping function must have a target parameter")
        val source = it.arguments.find { it.name?.asString() == Mapping::source.name }
            ?: throw IllegalArgumentException("The mapping function must have a source parameter")
        val ignore = it.arguments.find { it.name?.asString() == Mapping::ignore.name }
            ?: throw IllegalArgumentException("The mapping function must have a ignore parameter")
        val defaultValue = it.arguments.find { it.name?.asString() == Mapping::defaultValue.name }
            ?: throw IllegalArgumentException("The mapping function must have a defaultValue parameter")
        return MappingConfiguration(
            target = target.value?.toString()
                ?: throw IllegalArgumentException("The mapping function must have a target parameter"),
            source = source.value as? String ?: "",
            ignore = ignore.value as? Boolean ?: false,
            defaultValue = defaultValue.value as? String ?: ""
        )
    }
}
