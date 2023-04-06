package dev.murmur.kapper.transformer

import com.squareup.kotlinpoet.CodeBlock

interface CodeBlockGenerator {

    fun generate(inputName: String): CodeBlock
}
