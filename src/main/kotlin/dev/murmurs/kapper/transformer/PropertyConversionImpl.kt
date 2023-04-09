package dev.murmurs.kapper.transformer

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.FunctionKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.CodeBlock
import java.time.Instant
import java.time.OffsetDateTime
import kotlin.reflect.KClass

class PropertyConversionImpl(private val logger: KSPLogger) : PropertyConversion {
    companion object {
        private const val GENERIC_ENUM_TYPE_NAME = "ENUM"
    }

    private val doubleMatchStringMapper = mutableMapOf<MapperMapKey, MapperMapValue>()
    private val fromAnyToTargetMap = mutableMapOf<String, MapperMapValue>()


    override fun convert(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSClassDeclaration,
        targetPropertyType: KSClassDeclaration
    ): CodeBlock {
        return if (sourcePropertyType.classKind == ClassKind.ENUM_CLASS && targetPropertyType.qualifiedName!!.asString() == String::class.qualifiedName) {
            convert(
                "${sourceParameterName}.${sourcePropertyName}",
                GENERIC_ENUM_TYPE_NAME,
                targetPropertyType.qualifiedName!!.asString()
            )
        } else {
            convert(
                "${sourceParameterName}.${sourcePropertyName}",
                sourcePropertyType.qualifiedName!!.asString(),
                targetPropertyType.qualifiedName!!.asString()
            )
        }
    }

    override fun convert(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSClassDeclaration,
        targetPropertyType: KClass<*>
    ): CodeBlock {
        return if (sourcePropertyType.classKind == ClassKind.ENUM_CLASS && targetPropertyType == String::class) {
            convert(
                "${sourceParameterName}.${sourcePropertyName}",
                GENERIC_ENUM_TYPE_NAME,
                targetPropertyType.qualifiedName!!
            )
        } else {
            convert(
                "${sourceParameterName}.${sourcePropertyName}",
                sourcePropertyType.qualifiedName!!.asString(),
                targetPropertyType.qualifiedName!!
            )
        }
    }

    private fun convert(
        sourceName: String,
        sourcePropertyType: String,
        targetPropertyType: String
    ): CodeBlock {
        logger.info("retrieving mapper for $sourcePropertyType to $targetPropertyType")
        val mapperMapKey = MapperMapKey(sourcePropertyType, targetPropertyType)
        logger.info("hash code for mapper map key is ${mapperMapKey.hashCode()}")
        var mapper = doubleMatchStringMapper[mapperMapKey]
        logger.info("current map entries")
        for (mutableEntry in doubleMatchStringMapper) {
            logger.info("key is ${mutableEntry.key}")
            logger.info("value is ${mutableEntry.value}")
        }

        if (mapper == null) {
            mapper = fromAnyToTargetMap[targetPropertyType]
        }

        if (mapper == null) {
            throw IllegalArgumentException("No mapper found for $sourcePropertyType to $targetPropertyType")
        }

        return if (mapper.ksFunctionDeclaration != null) {
            CodeBlock.of(
                "%N(%L)",
                mapper.ksFunctionDeclaration!!.simpleName.asString(),
                sourceName
            )
        } else if (mapper.codeBlockGenerator != null) {
            mapper.codeBlockGenerator!!.generate(sourceName)
        } else {
            throw IllegalArgumentException("No mapper found for $sourcePropertyType to $targetPropertyType")
        }
    }

    override fun registerImplementedMapper(mapper: KSFunctionDeclaration) {
        logger.info("register mapper ${mapper.simpleName.asString()}")
        // only support member function and not abstract
        if (mapper.functionKind != FunctionKind.MEMBER) {
            logger.info("not member function")
            return
        }

        //only has one parameter
        if (mapper.parameters.size != 1) {
            logger.info("not one parameter")
            return
        }

        val returnTypeDeclaration = mapper.returnType?.resolve()?.declaration
        logger.info("return type is $returnTypeDeclaration")
        if (returnTypeDeclaration == null || returnTypeDeclaration !is KSClassDeclaration) {
            logger.info("return type is not class")
            return
        }
        val returnTypeName = returnTypeDeclaration.qualifiedName!!.asString()

        // check first parameter is kotlin Any type
        val sourceTypeDeclaration = mapper.parameters.first().type.resolve().declaration
        logger.info("first parameter is $sourceTypeDeclaration")
        if (sourceTypeDeclaration !is KSClassDeclaration) {
            logger.info("first parameter is not class")
            return
        }

        sourceTypeDeclaration.let {
            val sourceTypeName = it.qualifiedName!!.asString()
            if (sourceTypeName == Any::class.qualifiedName) {
                if (fromAnyToTargetMap[returnTypeName] == null || fromAnyToTargetMap[returnTypeName]!!.ksFunctionDeclaration == null) {
                    fromAnyToTargetMap[sourceTypeName] =
                        fromAnyToTargetMap[sourceTypeName]?.copy(ksFunctionDeclaration = mapper)
                            ?: MapperMapValue(ksFunctionDeclaration = mapper)
                }

            } else {
                val mapperMapKey = MapperMapKey(sourceTypeName, returnTypeName)
                if (doubleMatchStringMapper[mapperMapKey] == null || doubleMatchStringMapper[mapperMapKey]!!.ksFunctionDeclaration == null) {
                    doubleMatchStringMapper[mapperMapKey] =
                        doubleMatchStringMapper[mapperMapKey]?.copy(ksFunctionDeclaration = mapper)
                            ?: MapperMapValue(ksFunctionDeclaration = mapper)
                }
            }
        }
    }


    private fun registerBlockCodeGenerator(
        sourceClass: KClass<*>,
        targetClass: KClass<*>,
        codeBlockGenerator: CodeBlockGenerator
    ) {
        registerBlockCodeGenerator(sourceClass.qualifiedName!!, targetClass.qualifiedName!!, codeBlockGenerator)
    }

    private fun registerBlockCodeGenerator(
        sourceClass: String,
        targetClass: String,
        codeBlockGenerator: CodeBlockGenerator
    ) {
        val mapperMapKey = MapperMapKey(sourceClass, targetClass)
        if (doubleMatchStringMapper[mapperMapKey] == null || doubleMatchStringMapper[mapperMapKey]!!.codeBlockGenerator == null) {
            doubleMatchStringMapper[mapperMapKey] =
                doubleMatchStringMapper[mapperMapKey]?.copy(codeBlockGenerator = codeBlockGenerator)
                    ?: MapperMapValue(codeBlockGenerator = codeBlockGenerator)
        }
    }


    init {
        registerBlockCodeGenerator(
            String::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Int::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toInt()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Long::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toLong()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Float::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toFloat()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Double::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toDouble()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Boolean::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toBoolean()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Byte::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toByte()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Short::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toShort()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            String::class,
            Char::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L[0]", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Int::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Long::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Float::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Double::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Boolean::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Byte::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Short::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            Char::class,
            String::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toString()", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            GENERIC_ENUM_TYPE_NAME,
            String::class.qualifiedName!!,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.name", inputName)
                }
            }
        )

        registerBlockCodeGenerator(
            OffsetDateTime::class,
            Instant::class,
            object : CodeBlockGenerator {
                override fun generate(inputName: String): CodeBlock {
                    return CodeBlock.of("%L.toInstant()", inputName)
                }
            }
        )
    }


    internal data class MapperMapKey(val inputClass: String, val outputClass: String)

    internal data class MapperMapValue(
        val ksFunctionDeclaration: KSFunctionDeclaration? = null,
        val codeBlockGenerator: CodeBlockGenerator? = null
    )

}
