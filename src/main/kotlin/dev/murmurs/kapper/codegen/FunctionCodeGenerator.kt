package dev.murmurs.kapper.codegen

import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.squareup.kotlinpoet.FunSpec

interface FunctionCodeGenerator {

    fun generateCode(mappingFunction: KSFunctionDeclaration): FunSpec

}
