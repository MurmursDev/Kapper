package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock

interface PropertyCodeGenerator {
    fun generateCode(
        sourceParameterName: String,
        sourcePropertyName: String,
        sourcePropertyType: KSType,
        targetPropertyName: String,
        targetPropertyType: KSType
    ): CodeBlock
}
