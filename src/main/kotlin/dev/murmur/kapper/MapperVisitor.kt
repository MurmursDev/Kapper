package dev.murmur.kapper

import dev.murmur.kapper.codegen.FunctionCodeGenerator
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import dev.murmur.kapper.transformer.PropertyConversion

class MapperVisitor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
    private val functionCodeGenerator: FunctionCodeGenerator,
    private val propertyConversion: PropertyConversion
) : KSVisitorVoid() {
    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        val packageName = classDeclaration.packageName.asString()
        val className = "${classDeclaration.simpleName.asString()}Impl"
        val file = codeGenerator.createNewFile(Dependencies(true), packageName, className)

        logger.info("classDeclaration: ${classDeclaration.qualifiedName?.asString()}")
        val typeSpecBuilder = TypeSpec.classBuilder(className)
            .addSuperinterface(ClassName.bestGuess(classDeclaration.qualifiedName!!.asString()))

        val ksFunctionDeclarationSequence = classDeclaration.getDeclaredFunctions()
        ksFunctionDeclarationSequence.forEach {
            logger.info("registering mapper: ${it.qualifiedName?.asString()}")
            propertyConversion.registerImplementedMapper(it)
        }

        ksFunctionDeclarationSequence.filter {
            logger.info("function: ${it.qualifiedName?.asString()} is abstract: ${it.isAbstract}")
            it.isAbstract
        }.map {
            functionCodeGenerator.generateCode(it)
        }.forEach {
            typeSpecBuilder.addFunction(it)
        }

        val fileSpec = FileSpec.get(packageName, typeSpecBuilder.build())
        file.bufferedWriter().use { it.write(fileSpec.toString()) }
    }
}
