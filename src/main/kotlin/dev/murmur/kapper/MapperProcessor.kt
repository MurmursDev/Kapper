package dev.murmur.kapper

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import dev.murmur.kapper.config.Mapper

class MapperProcessor(
    private val logger: KSPLogger,
    private val mapperVisitor: MapperVisitor
) : SymbolProcessor {

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(Mapper::class.qualifiedName!!)
        symbols.forEach { symbol ->
            if (symbol is KSClassDeclaration) {
                symbol.accept(mapperVisitor, Unit)
            } else {
                logger.error("config.Mapper can only be applied to interfaces", symbol)
            }
        }
        return emptyList()
    }

}
