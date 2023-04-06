package dev.murmur.kapper.transformer

interface PropertyTransformer<I,O> {

    fun transform(input: I): O

}
